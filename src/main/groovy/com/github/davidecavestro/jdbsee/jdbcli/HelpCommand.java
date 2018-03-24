package com.github.davidecavestro.jdbsee.jdbcli;

import com.google.common.base.Function;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.IOException;
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

  @Inject
  public ConsoleService getConsoleService () {
    return consoleService;
  }

  @Inject
  public void setConsoleService (final ConsoleService consoleService) {
    this.consoleService = consoleService;
  }

}
