package com.github.davidecavestro.jdbsee.jdbcli

import picocli.CommandLine

import javax.inject.Inject
import static picocli.CommandLine.*

@Command(name = "describe", description = "Provides info about database and driver",
    subcommands = [
        DescribeTablesCommand,
        DescribeViewsCommand,
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

    @Parameters(index = "0", arity = "0..1", paramLabel = "PATTERN", description = "A table name pattern; when specified must match the table name as it is stored in the database.")
    String matches

    @Inject//dagger
    DescribeTablesCommand() {}

    @Override
    void run() {
      parentCommand.service.run(this)
    }
  }

  @Command(name = "views", description = "List all views")
  static class DescribeViewsCommand extends AbstractDbCommand {

    @ParentCommand
    private DescribeCommand parentCommand

    @Parameters(index = "0", arity = "0..1", paramLabel = "PATTERN", description = "A table name pattern; when specified must match the table name as it is stored in the database.")
    String matches

    @Inject//dagger
    DescribeViewsCommand() {}

    @Override
    void run() {
      parentCommand.service.run(this)
    }
  }

  @Command(name = "driver", description = "Show driver info")
  static class DescribeDriverCommand extends AbstractDbCommand {

    @ParentCommand
    DescribeCommand parentCommand

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
