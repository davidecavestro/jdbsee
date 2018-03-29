package com.github.davidecavestro.jdbsee.jdbcli

import groovy.transform.CompileStatic
import picocli.CommandLine

import javax.inject.Inject
import static picocli.CommandLine.*

@CompileStatic
@Command(name = "describe", description = "Provides info about database and driver",
    subcommands = [
        DescribeTablesCommand,
        DescribeViewsCommand,
        DescribeDriverCommand,
        HelpCommand
    ]
)
class DescribeCommand implements CliCommand {
  @ParentCommand
  private MainCommand mainCommand

  @Inject
//public for dagger
  public DescribeCommandService service

  @Inject//public for dagger
  public ConsoleService consoleService

  @Inject
  DescribeCommand() {}

  @Override
  void run() { consoleService.usage(this) }


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

    @Parameters(index = "0", arity = "0..1", paramLabel = "PATTERN", description = "A property name pattern; when specified must match the proeprty name as it is exposed by the database metadata.")
    String matches

    @Inject//dagger
    DescribeDriverCommand() {}

    @Override
    void run() {
      parentCommand.service.run(this)
    }
  }

  @Command(name = "help", description = "Print this help")
  static class HelpCommand implements CliCommand {
    @ParentCommand
    private DescribeCommand parentCommand

    @Inject//dagger
    HelpCommand() {}

    @Override
    void run() { parentCommand.consoleService.usage(parentCommand) }
  }

}
