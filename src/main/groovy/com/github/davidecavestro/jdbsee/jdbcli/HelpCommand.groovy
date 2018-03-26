package com.github.davidecavestro.jdbsee.jdbcli;

import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(name = "help", description = "Print this help")
public class HelpCommand implements CliCommand {
  @CommandLine.ParentCommand
  private MainCommand parent
  @Inject
  protected ConsoleService consoleService

  @Inject
//dagger
  HelpCommand(){}

  @Override
  void run () {
    consoleService.usage (parent)
  }

  @Inject
  ConsoleService getConsoleService () {
    return consoleService
  }

  @Inject
  void setConsoleService (final ConsoleService consoleService) {
    this.consoleService = consoleService
  }

}
