package com.github.davidecavestro.jdbsee.jdbcli;

import picocli.CommandLine.*;

import javax.inject.Inject;

@Command(subcommands = {DriverCommand.class, RunCommand.class})
public class MainCommand implements Runnable {
  @Option(names = {"-v", "--version"}, description = "display version info")
  boolean versionRequested;

  @Inject
  public MainCommand(){}

  @Override
  public void run () {
    if (versionRequested) {
      System.out.println ("0.1.0");
    }
  }
}
