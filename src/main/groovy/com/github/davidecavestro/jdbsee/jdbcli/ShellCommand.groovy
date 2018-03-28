package com.github.davidecavestro.jdbsee.jdbcli

import com.google.common.base.Function
import groovy.transform.CompileStatic
import org.jline.builtins.Completers.TreeCompleter
import org.jline.builtins.SortedTreeCompleter
import org.jline.reader.*
import org.jline.reader.impl.DefaultParser
import org.jline.reader.impl.LineReaderImpl
import org.jline.reader.impl.history.DefaultHistory
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.jline.utils.AttributedString
import org.jline.utils.ShutdownHooks
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine

import javax.inject.Inject

import static org.jline.builtins.Completers.TreeCompleter.node

@CompileStatic
@CommandLine.Command(
  name = "shell",
  description = "Start the command shell",
  subcommands = [ShellCommand.HelpCommand.class]
)
class ShellCommand implements CliCommand {

  private final static Logger LOG = LoggerFactory.getLogger (ShellCommand.class)

  @CommandLine.ParentCommand
  private MainCommand parent
  private boolean quit

  @CommandLine.Option(names = ["-b", "--no-banner"], description = "Don't show banner")
  boolean noBanner
  @CommandLine.Option(names = ["-m", "--monochrome"], description = "Don't produce coloured output")
  boolean monochrome
//  @CommandLine.Option(names = ["-t", "--theme"], description = "Theme for Ansi coloured output. On between [dark, mono]. Choose mono for monochromatic output.")
//  Theme theme = Theme.dark

  @Inject
  protected ConsoleService consoleService

  @Inject
  protected ConfigService configService

  @Inject
  protected PropertiesVersionProvider versionProvider

  @Inject //dagger
  ShellCommand () {
  }

  @Override
  void run () {
    try {
      consoleService.withSysOutStream (new Function<PrintStream, Void> () {
        @Override
        Void apply (final PrintStream sysOut) {
          try {
            final TerminalBuilder builder = TerminalBuilder.builder ()

            final Terminal terminal = builder.build ()

            consoleService.setSysErr (terminal.writer ())
            consoleService.setSysOut (terminal.writer ())

            final AppComponent appComponent = parent.getAppComponent ()
            final CommandLine.IFactory daggerFactory = new AppIFactory (appComponent)
            final Completer completer = new SortedTreeCompleter (retrieveNodes (createCommandLine (daggerFactory)))
            final File historyFile = new File (configService.getUserDataDir (), "shell-history")

            final DefaultHistory history = new DefaultHistory ()
            ShutdownHooks.add (new ShutdownHooks.Task () {
              @Override
              void run () throws Exception {
                history.save ()
              }
            })
            final DefaultParser parser = new DefaultParser(eofOnEscapedNewLine:true)
            final LineReader reader = LineaReaderAdapter.Builder.builder ()
                .terminal (terminal)
                .completer (completer)
                .parser(parser)
                .variable (LineReader.HISTORY_FILE, historyFile)
                .history (history)
                .build ()
            if (!noBanner) {//banner enabled
              def stream = getClass().getResourceAsStream("/banner_shell.txt")
              String version = getVersion ()
              final String banner = stream.text.with {String bannerTxt->
                String text =
"""
$bannerTxt

    $version

""".toString()

                return ShellCommand.this.coloured(terminal, text)//TODO find why groovyc generates a classcast here without FQN... really 8-)
              }
              terminal.writer().println(banner)
            }
            while (true) {
              if (quit) {
                terminal.writer ().println (AttributedString.fromAnsi ("\u001B[33mbye\u001B[0m").toAnsi (terminal))
                terminal.writer ().flush ()
                break
              }
              String line = null
              try {
                  line = reader.readLine (">", null, (MaskingCallback) null, null)
              } catch (UserInterruptException e) {
                // Ignore
              } catch (EndOfFileException e) {
                return null
              }
              if (line == null) {
                continue
              }

              line = line.trim ()

              terminal.flush ()

              ParsedLine pl = reader.getParser ().parse (line, 0)

              pl.words().with {def words->
                if (words && words.first()) {// possible commands detected
                  try {
                      ShellCommand.this.createCommandLine (daggerFactory).parseWithHandler (new CommandLine.RunLast (), System.err, pl.words ().toArray (new String[0]))
                  } catch (final Exception e) {
                    e.printStackTrace (System.err)
                    terminal.writer ().println ("")
                    terminal.flush ()
                  }
                } else {
                  // simple ENTER, go straight
                }
              }
            }
          } catch (Exception e) {
            throw new RuntimeException (e)
          }

          return null
        }
      })
    } catch (final IOException e) {
      throw new RuntimeException (e)
    }
    //    CommandLine commandLine = new CommandLine (parent, daggerFactory);
  }

  //TODO use picocli styles
  protected String coloured(final Terminal terminal, final String text) {
    return monochrome?
            text:
            AttributedString.fromAnsi(
              """\u001B[32m${text}\u001B[0m""".toString()
            ).toAnsi(terminal)
  }

  protected String getLongVersion () throws Exception {
    return String.format ("Jdbsee CLI shell %s (%s)", versionProvider.getVersionProp(), versionProvider.getBuildtimeProp())
  }

  protected String getVersion () throws Exception {
    return String.format ("%s (%s)", versionProvider.getVersionProp(), versionProvider.getBuildtimeProp())
  }

  protected CommandLine createCommandLine (final CommandLine.IFactory daggerFactory) {
    final CommandLine commandLine = new CommandLine (parent, daggerFactory)

//    commandLine.subcommands.findAll {String k, CommandLine v -> k!='shell'}
//    for (final Map.Entry<String, CommandLine> entry : Maps.filterEntries (commandLine.getSubcommands (), new Predicate<Map.Entry<String, CommandLine>> () {
//        @Override
//        boolean apply (final Map.Entry<String, CommandLine> input) {
//            return !input.getKey ().equals ("shell")//skip shell command
//        }
//    }).entrySet ()) {
//        commandLine.addSubcommand (entry.getKey (), entry.getValue ())
//    }


    //register quit command at top level, only within shell
    final ExitCommand exitCommand = new ExitCommand ()
    exitCommand.setParent (this)
    commandLine.addSubcommand ("exit", exitCommand)

    final QuitCommand quitCommand = new QuitCommand ()
    quitCommand.setParent (this)
    commandLine.addSubcommand ("quit", quitCommand)

    return commandLine
  }

  protected List<TreeCompleter.Node> retrieveNodes (final CommandLine commandLine) {
    final List<TreeCompleter.Node> result = new ArrayList<> ()

    final Map<String, CommandLine> subcommands = commandLine.getSubcommands ()

    final List<String> sortedCmds = new ArrayList<>(subcommands.keySet())
    sortedCmds.sort().each {final String name->
      final CommandLine subCommandLine = subcommands[name]

      final List nodes = new ArrayList<> ()
      nodes.add (name)
      nodes.addAll (retrieveNodes (subCommandLine))

      result.add (node (nodes.toArray ()))
    }

    return result
  }

  @CommandLine.Command(name = "quit", description = "Quit shell")
  static class QuitCommand implements Runnable {
    private ShellCommand parent

    QuitCommand () {}

    @Override
    void run () {
      parent.quit ()
    }

    ShellCommand getParent () {
      return parent
    }

    void setParent (final ShellCommand parent) {
      this.parent = parent
    }
  }

  void quit () {
        quit = true
  }

  @CommandLine.Command(name = "exit", description = "Quit shell")
  static class ExitCommand implements Runnable {
    private ShellCommand parent

    ExitCommand () {}

    @Override
    void run () {
      parent.quit ()
    }

    ShellCommand getParent () {
      return parent
    }

    void setParent (final ShellCommand parent) {
      this.parent = parent
    }
  }

  @CommandLine.Command(name = "help", description = "Print this help")
  static class HelpCommand implements Runnable {
    @CommandLine.ParentCommand
    ShellCommand parent

    @Inject//public for dagger
    public ConsoleService consoleService

    @Inject//dagger
    HelpCommand () {
    }

    @Override
    void run () {consoleService.usage (parent)}

    ConsoleService getConsoleService () {
      return consoleService
    }

    void setConsoleService (final ConsoleService consoleService) {
      this.consoleService = consoleService
    }
  }

//  static enum Theme {
//    dark ([
//          (Style.plain): '37',
//          (Style.strong): '32',
//          (Style.em): '36',
//          (Style.head): '33',
//          (Style.foot): '33'
//      ]),
//    mono ();
//
//    Map<Style,String> styles
//    boolean monochrome
//
//    Theme () {monochrome=true}
//    Theme (Map<Style,String> styles) {this.styles=styles}
//
//    String text (final Terminal terminal, final Style style,  final String text) {
//      if (monochrome) {
//        return text
//      } else {//coloured banner
//        switch (style) {
//          plain:
//            return text
//          default:
//            return AttributedString.fromAnsi(
//                  """\u001B[${styles[style]}m$text\u001B[0m""".toString()
//            ).toAnsi(terminal)
//        }
//      }
//    }
//  }
//  static enum Style {
//    plain, strong, em, head, foot;
//  }
}
