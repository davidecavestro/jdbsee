package com.github.davidecavestro.jdbsee.jdbcli

import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine

import javax.inject.Inject

@CompileStatic
@CommandLine.Command(
  name = "connect",
  description = "Open a SQL shell to specific database connection",
  subcommands = [ConnectCommand.HelpCommand.class]
)
class ConnectCommand extends AbstractDbCommand {

  private final static Logger LOG = LoggerFactory.getLogger (ConnectCommand.class)

  @CommandLine.ParentCommand
  MainCommand parent

  @Inject//public for dagger
  public ConnectCommandService connectCommandService

  @Inject //dagger
  ConnectCommand() {
  }

  @Override
  void run () {
    connectCommandService.run(this)
  }

  @CommandLine.Command(name = "help", description = "Print this help")
  static class HelpCommand implements Runnable {
    @CommandLine.ParentCommand
    ConnectCommand parent

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
}
