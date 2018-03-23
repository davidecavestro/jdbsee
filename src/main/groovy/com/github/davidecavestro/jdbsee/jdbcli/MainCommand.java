package com.github.davidecavestro.jdbsee.jdbcli;

import picocli.CommandLine;
import picocli.CommandLine.*;

import javax.inject.Inject;

@Command(
//    header = "@|bold NAME|@",
//    name = "@|bold jdbsee|@",
    synopsisHeading = "@|bold NAME|@",
    customSynopsis = {"\n","jdbsee - the SQL JDBC tool", "\n"},
//    abbreviateSynopsis = true,

    descriptionHeading = "@|bold DESCRIPTION|@",
    description = {
        "\n",
        "Jdbsee is a small command line tool providing utilities to access databases through JDBC drivers."+
        "\n",
        "Drivers can be downloaded on demand, automatically or explicitly loaded from local filesystem."+
        "\n",
        "Both drivers and database connection settings can be persisted for later reuse."
    },
    optionListHeading = "\n@|bold OPTIONS|@\n",
    commandListHeading= "\n@|bold COMMANDS|@\n",
    subcommands = {
        RunCommand.class,
        DriverCommand.class,
        AliasCommand.class,
        DescribeCommand.class,
        ShellCommand.class,
        HelpCommand.class
    },
    versionProvider = PropertiesVersionProvider.class)
public class MainCommand implements Runnable {
  @Option(names = {"-V", "--version"}, versionHelp = true, description = "Print version info")
  boolean versionRequested;

  @Inject
  protected ConsoleService consoleService;

  private AppComponent appComponent;

  @Inject
  public MainCommand(){}

  @Override
  public void run() {
    CommandLine.usage(this, consoleService.getSysOutStream ());
  }

  public AppComponent getAppComponent () {
    return appComponent;
  }

  public void setAppComponent (final AppComponent appComponent) {
    this.appComponent = appComponent;
  }
}
