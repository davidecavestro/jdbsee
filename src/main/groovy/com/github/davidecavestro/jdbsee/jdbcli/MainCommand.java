package com.github.davidecavestro.jdbsee.jdbcli;

import picocli.CommandLine;
import picocli.CommandLine.*;

import javax.inject.Inject;

@Command(subcommands = {RunCommand.class, DriverCommand.class})
public class MainCommand implements Runnable {
  @Option(names = {"-V", "--version"}, versionHelp = true, description = "Print version info")
  boolean versionRequested;

  @Inject
  public MainCommand(){}

  @Override
  public void run() {
    CommandLine.usage(this, System.out);
  }
}
