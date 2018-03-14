package com.github.davidecavestro.jdbsee.jdbcli

import picocli.CommandLine

import javax.inject.Inject
import static picocli.CommandLine.*

@Command(name = "describe", description = "Provides info about database and driver",
    subcommands = [
        DescribeTablesCommand,
        DescribeViewsCommand,
        DescribeFullCommand,
        DescribeDriverCommand,
        HelpCommand
    ]
)
class DescribeCommand implements Runnable {
  @ParentCommand
  private MainCommand mainCommand

  @Inject
//public for dagger
  public DescribeCommandService service

  @Inject
  DescribeCommand() {}

  @Override
  void run() { CommandLine.usage(this, System.out) }

  @Command(name = "tables", description = "List all tables")
  static class DescribeTablesCommand extends AbstractDbCommand {

    @ParentCommand
    private DescribeCommand parentCommand

    @Inject
//dagger
    DescribeTablesCommand() {}

    @Override
    void run() {
      //TODO implement
//      parentCommand.service.doRun(this, (new ConnectionCallback<Void, SQLException>() {
//        @Override
//        Void withConnection(final Connection connection) throws SQLException {
//
//          return null
//        }
//      })
//    }
    }
  }

  @Command(name = "views", description = "List all views")
  static class DescribeViewsCommand extends AbstractDbCommand {

    @ParentCommand
    private DescribeCommand parentCommand

    @Inject
//dagger
    DescribeViewsCommand() {}

    @Override
    void run() {
      //TODO implement
//      parentCommand.service.execute(new ConnectionCallback<Void, SQLException>() {
//        @Override
//        Void withConnection(final Connection connection) throws SQLException {
//
//          return null
//        }
//      }
//          )
    }
  }

  @Command(name = "full", description = "Show available database metadata")
  static class DescribeFullCommand extends AbstractDbCommand {

    @ParentCommand
    private DescribeCommand parentCommand

    @Inject
//dagger
    DescribeFullCommand() {}

    @Override
    void run() {
      //TODO implement
//      parentCommand.service.execute(new ConnectionCallback<Void, SQLException>() {
//        @Override
//        Void withConnection(final Connection connection) throws SQLException {
//
//          return null
//        }
//      }
//          )
    }
  }

  @Command(name = "driver", description = "Show driver info")
  static class DescribeDriverCommand extends AbstractDbCommand {

    @ParentCommand
    private DescribeCommand parentCommand

    @Inject//dagger
    DescribeDriverCommand() {}

    @Override
    void run() {
      parentCommand.service.run(this)
    }
  }

  @Command(name = "help", description = "Print this help")
  static class HelpCommand implements Runnable {
    @ParentCommand
    private DescribeCommand parentCommand

    @Inject//dagger
    HelpCommand() {}

    @Override
    void run() { CommandLine.usage(parentCommand, System.out) }
  }

}
