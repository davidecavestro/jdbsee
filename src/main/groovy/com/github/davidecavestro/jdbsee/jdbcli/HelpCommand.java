package com.github.davidecavestro.jdbsee.jdbcli;

import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(name = "help", description = "Print this help")
public class HelpCommand implements Runnable {
  @CommandLine.ParentCommand
  private MainCommand parent;
  @Inject
  protected ConsoleService consoleService;

  @Inject
//dagger
  HelpCommand(){}

  @Override
  public void run () {CommandLine.usage(parent, consoleService.getSysOutStream ());}

  public ConsoleService getConsoleService () {
    return consoleService;
  }

  public void setConsoleService (final ConsoleService consoleService) {
    this.consoleService = consoleService;
  }
}
