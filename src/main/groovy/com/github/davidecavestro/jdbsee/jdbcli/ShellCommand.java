package com.github.davidecavestro.jdbsee.jdbcli;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.jline.builtins.Completers.TreeCompleter;
import org.jline.reader.*;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.ShutdownHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.jline.builtins.Completers.TreeCompleter.node;

@CommandLine.Command(
        name = "shell",
        description = "Start the command shell",
        subcommands = {ShellCommand.HelpCommand.class}
)
public class ShellCommand implements Runnable {

    private final static Logger LOG = LoggerFactory.getLogger (ShellCommand.class);

    @CommandLine.ParentCommand
    private MainCommand parent;
    private boolean quit;

    @Inject
    protected ConsoleService consoleService;

    @Inject
    protected ConfigService configService;

    @Inject
    protected PropertiesVersionProvider versionProvider;

    @Inject //dagger
    ShellCommand () {
    }

    @Override
    public void run () {
        try {
            consoleService.withSysOutStream (new Function<PrintStream, Void> () {
                @Override
                public Void apply (final PrintStream input) {
                    try {
                        TerminalBuilder builder = TerminalBuilder.builder ();

                        Terminal terminal = builder.streams (consoleService.getSysInStream (), input).build ();
                        consoleService.setSysErr (terminal.writer ());

                        final AppComponent appComponent = parent.getAppComponent ();
                        final CommandLine.IFactory daggerFactory = new AppIFactory (appComponent);
                        final Completer completer = new TreeCompleter (retrieveNodes (newCommandLine (daggerFactory)));
                        final File historyFile = new File (configService.getUserDataDir (), "shell-history");

                        final DefaultHistory history = new DefaultHistory ();
                        ShutdownHooks.add (new ShutdownHooks.Task () {
                            @Override
                            public void run () throws Exception {
                                history.save ();
                            }
                        });
                        LineReader reader = LineReaderBuilder.builder ()
                                .terminal (terminal)
                                .completer (completer)
                                //          .parser(parser)
                                .variable (LineReader.HISTORY_FILE, historyFile)
                                .history (history)
                                .build ();
                        terminal.writer ().println (AttributedString.fromAnsi (String.format ("\u001B[33m%s\u001B[0m", getVersion ())).toAnsi (terminal));
                        while (true) {
                            if (quit) {
                                terminal.writer ().println (AttributedString.fromAnsi ("\u001B[33mbye\u001B[0m").toAnsi (terminal));
                                terminal.writer ().flush ();
                                break;
                            }
                            String line = null;
                            try {
                                line = reader.readLine (">", null, (MaskingCallback) null, null);
                            } catch (UserInterruptException e) {
                                // Ignore
                            } catch (EndOfFileException e) {
                                return null;
                            }
                            if (line == null) {
                                continue;
                            }

                            line = line.trim ();

                            terminal.flush ();

                            ParsedLine pl = reader.getParser ().parse (line, 0);

                            try {
                                newCommandLine (daggerFactory).parseWithHandler (new CommandLine.RunLast (), System.err, pl.words ().toArray (new String[]{}));
                            } catch (final Exception e) {
                                e.printStackTrace (System.err);
                                terminal.writer ().println ("");
                                terminal.flush ();
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException (e);
                    }

                    return null;
                }
            });
        } catch (final IOException e) {
            throw new RuntimeException (e);
        }
        //    CommandLine commandLine = new CommandLine (parent, daggerFactory);
    }

    protected String getVersion () throws Exception {
        return Arrays.toString (versionProvider.getVersion ());
    }

    private CommandLine newCommandLine (final CommandLine.IFactory daggerFactory) {
        final CommandLine commandLine = new CommandLine (parent, daggerFactory);

        for (final Map.Entry<String, CommandLine> entry : Maps.filterEntries (commandLine.getSubcommands (), new Predicate<Map.Entry<String, CommandLine>> () {
            @Override
            public boolean apply (final Map.Entry<String, CommandLine> input) {
                return !input.getKey ().equals ("shell");//skip shell command
            }
        }).entrySet ()) {
            commandLine.addSubcommand (entry.getKey (), entry.getValue ());
        }
        ;
        //register quit command at top level, only within shell
        final QuitCommand quitCommand = new QuitCommand ();
        quitCommand.setParent (this);
        commandLine.addSubcommand ("quit", quitCommand);

        return commandLine;
    }

    protected List<TreeCompleter.Node> retrieveNodes (final CommandLine commandLine) {
        final List<TreeCompleter.Node> result = new ArrayList<> ();
        final Map<String, CommandLine> subcommands = commandLine.getSubcommands ();
        for (final Map.Entry<String, CommandLine> entry : subcommands.entrySet ()) {
            final String name = entry.getKey ();
            final CommandLine subCommandLine = entry.getValue ();
            final List nodes = new ArrayList<> ();
            nodes.add (name);
            nodes.addAll (retrieveNodes (subCommandLine));
            result.add (node (nodes.toArray ()));
        }

        return result;
    }

    @CommandLine.Command(name = "quit", description = "Quit shell")
    public static class QuitCommand implements Runnable {
        private ShellCommand parent;

        public QuitCommand () {
        }

        @Override
        public void run () {
            parent.quit ();
        }

        public ShellCommand getParent () {
            return parent;
        }

        public void setParent (final ShellCommand parent) {
            this.parent = parent;
        }
    }

    public void quit () {
        quit = true;
    }


    @CommandLine.Command(name = "help", description = "Print this help")
    static class HelpCommand implements Runnable {
        @CommandLine.ParentCommand
        ShellCommand parent;

        @Inject//public for dagger
        public ConsoleService consoleService;

        @Inject//dagger
        public HelpCommand () {
        }

        @Override
        public void run () {
            try {
                consoleService.withSysOutStream (new Function<PrintStream, Void> () {
                    @Override
                    public Void apply (final PrintStream input) {
                        CommandLine.usage (parent, input);
                        return null;
                    }
                });
            } catch (final IOException e) {
                throw new RuntimeException (e);
            }
        }

        public ConsoleService getConsoleService () {
            return consoleService;
        }

        public void setConsoleService (final ConsoleService consoleService) {
            this.consoleService = consoleService;
        }
    }

}
