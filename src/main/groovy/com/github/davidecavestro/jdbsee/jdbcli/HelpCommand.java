package com.github.davidecavestro.jdbsee.jdbcli;

import picocli.CommandLine;

import javax.inject.Inject;
import java.io.PrintStream;

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
  public void run () {
    final PrintStream sysOut = consoleService.getSysOutStream ();
    try {
      CommandLine.usage (parent, sysOut);
    } finally {
      sysOut.flush ();
    }
  }

  @Inject
  public ConsoleService getConsoleService () {
    return consoleService;
  }

  @Inject
  public void setConsoleService (final ConsoleService consoleService) {
    this.consoleService = consoleService;
  }

}
