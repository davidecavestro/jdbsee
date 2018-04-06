package com.github.davidecavestro.jdbsee.jdbcli

import org.jline.builtins.Completers
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine
import org.jline.reader.UserInterruptException
import org.jline.reader.impl.DefaultParser
import org.jline.reader.impl.history.DefaultHistory
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.jline.utils.AttributedString
import org.jline.utils.ShutdownHooks
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine

import javax.inject.Inject
import javax.sql.DataSource
import java.security.MessageDigest
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.sql.Statement

import static org.jline.builtins.Completers.TreeCompleter.node

//@CompileStatic
class ConnectCommandService extends AbstractDbCommandService{

  private final static Logger LOG = LoggerFactory.getLogger (ConnectCommandService.class)

  @Inject//public for dagger
  public ConfigService configService
  @Inject//public for dagger
  public ShellService shellService

  @Inject
  public ConnectCommandService(){}

  void run (final ConnectCommand cmd) {
    shellService.withSysOutStream { final PrintStream sysOut->
      try {
        final TerminalBuilder builder = TerminalBuilder.builder ()

        final Terminal terminal = builder.build ()

        shellService.setSysStreams (terminal.writer ())

        doRun (cmd) { DataSource dataSource ->
          final Connection connection = dataSource.getConnection()
          try {
            def sessionName // display name
            def sessionUID // db config identifier
            connection.metaData.with { DatabaseMetaData dbMeta->
              sessionName = "${dbMeta.userName}@${dbMeta.databaseProductName}"
              sessionUID = "${dbMeta.getURL()}".toString().with {String s->
                MessageDigest.getInstance("MD5").digest(s.bytes).encodeHex().toString()
              }
            }

            final AppComponent appComponent = cmd.parent.getAppComponent ()
            final CommandLine.IFactory daggerFactory = new AppIFactory (appComponent)
            final File historyFile = new File (new File (configService.getUserDataDir (), 'connections'), "${sessionUID}-connection-history")

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
                .parser(parser)
                .variable (LineReader.HISTORY_FILE, historyFile)
                .history (history)
                .build ()

//            //autocommit false by default
//            connection.autoCommit = false

            boolean quit = false

            while (true) {//main loop
              if (quit) {
                terminal.writer ().println (AttributedString.fromAnsi ("\u001B[33mSession closed\u001B[0m").toAnsi (terminal))
                terminal.writer ().flush ()
                break
              }
              String line = null
              try {
                line = shellService.readLine ("${sessionName}>", reader)
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
                    switch (words[0]) {
                      case 'quit':
                      case 'exit':
                        quit = true
                        break
                      default:
                        final Statement stmt = connection.createStatement()
                        try {
                          if (line.trim().startsWith('SELECT')) {
                            final ResultSet rs = stmt.executeQuery(line)
                            try {
                              shellService.printResultSet(rs)
                            } finally {//TODO use withCloseable with groovy 2.5
                              rs.close()
                            }
                          } else {
                            final int numRows = stmt.executeUpdate(line)
                            shellService.printRowsAffected(numRows)
                          }
                        } finally {//TODO use withCloseable with groovy 2.5
                          stmt.close()
                        }
                    }
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
          } finally {//TODO switch to withCloseable when groovy 2.5+ is available
            connection.close()
          }
        }
      } catch (Exception e) {
        throw new RuntimeException (e)
      }
    }
    //    CommandLine commandLine = new CommandLine (parent, daggerFactory);
  }



  protected CommandLine createCommandLine (final CommandLine.IFactory daggerFactory) {
    final CommandLine commandLine = new CommandLine (this, daggerFactory)

    //register quit command at top level, only within shell
    final ExitCommand exitCommand = new ExitCommand ()
    exitCommand.setParent (this)
    commandLine.addSubcommand ("exit", exitCommand)

    final QuitCommand quitCommand = new QuitCommand ()
    quitCommand.setParent (this)
    commandLine.addSubcommand ("quit", quitCommand)

    return commandLine
  }

  protected List<Completers.TreeCompleter.Node> retrieveNodes (final CommandLine commandLine) {
    final List<Completers.TreeCompleter.Node> result = new ArrayList<> ()

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

  @CommandLine.Command(name = "quit", description = "Quit session")
  static class QuitCommand implements Runnable {
    private ConnectCommand parent

    QuitCommand () {}

    @Override
    void run () {
      //no-op, kept only for completion and help
    }

    ConnectCommand getParent () {
      return parent
    }

    void setParent (final ConnectCommand parent) {
      this.parent = parent
    }
  }

  void quit () {
    quit = true
  }

  @CommandLine.Command(name = "exit", description = "Quit session")
  static class ExitCommand implements Runnable {
    private ConnectCommand parent

    ExitCommand () {}

    @Override
    void run () {
      //no-op, kept only for completion and help
    }

    ConnectCommand getParent () {
      return parent
    }

    void setParent (final ConnectCommand parent) {
      this.parent = parent
    }
  }

}
