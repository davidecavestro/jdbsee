package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsAliasDao
import picocli.CommandLine

import javax.inject.Inject

import static picocli.CommandLine.*

@Command(name = "alias", description = "Provides persistent settings db configuration",
    subcommands = [
        AliasCreateCommand, AliasDeleteCommand,
        AliasListCommand, AliasShowCommand
    ]
)
class AliasCommand implements Runnable {
  @ParentCommand
  private MainCommand mainCommand

  @Inject//public for dagger
  public JdbsAliasDao jdbsAliasDao

  @Inject
  AliasCommand(){}

  @Override
  void run () {CommandLine.usage(this, System.out)}

  @Command(name = "create", description = "Create a new persistent alias")
  static class AliasCreateCommand implements Runnable {

    @ParentCommand
    private AliasCommand aliasCommand

    @Parameters(index = "0", arity = "1", paramLabel = "DRIVER", description = "The name or id of the driver.")
    String driver

    @Parameters(index = "1", arity = "1", paramLabel = "NAME", description = "The name of the alias.")
    String name

    @Parameters(index = "2", paramLabel = "URL", description = "The JDBC url.")
    String url

    @Option(names = ["-u", "--user"], description = "The username")
    String username = ""

    @Option(names = ["-p", "--password"], description = "The password")
    String password = ""

    @Option(names = ["-V", "--version"], versionHelp = true, description = "display version info")
    boolean versionInfoRequested;

    @Option(names = ["-h", "--help"], usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @Inject//dagger
    AliasCreateCommand (){}

    @Override
    void run () {
      aliasCommand.jdbsAliasDao.insert (driver, name, url, username, password)
    }
  }


  @Command(name = "delete", description = "Delete aliases")
  static class AliasDeleteCommand implements Runnable {
    @ParentCommand
    AliasCommand aliasCommand

    @Parameters(index = "0..*", arity = "1", paramLabel = "ALIAS_ID", description = "The ID of the alias.")
    List<Long> aliasIds

    @Option(names = ["-V", "--version"], versionHelp = true, description = "display version info")
    boolean versionInfoRequested;

    @Option(names = ["-h", "--help"], usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @Inject//dagger
    AliasDeleteCommand () {}

    @Override
    void run () {
      aliasCommand.jdbsAliasDao.delete (aliasIds as Long[])
    }
  }

  @Command(name = "list", description = "List registered aliases")
  static class AliasListCommand implements Runnable {
    @ParentCommand
    AliasCommand aliasCommand

    @Inject
    public AliasService aliasCommandService

    @Option(names = ["-V", "--version"], versionHelp = true, description = "display version info")
    boolean versionInfoRequested;

    @Option(names = ["-h", "--help"], usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @Inject//dagger
    AliasListCommand () {}

    @Override
    void run () {
      aliasCommandService.listAliases()
    }
  }

  @Command(name = "show", description = "Show details about alias settings")
  static class AliasShowCommand implements Runnable {
    @ParentCommand
    private AliasCommand aliasCommand

    @Inject
    public AliasService aliasCommandService

    @Parameters(index = "0", arity = "1", paramLabel = "ALIAS_ID", description = "The ID of the alias.")
    Long aliasId

    @Option(names = ["-V", "--version"], versionHelp = true, description = "display version info")
    boolean versionInfoRequested;

    @Option(names = ["-h", "--help"], usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @Inject//dagger
    AliasShowCommand () {}

    @Override
    void run () {
      aliasCommandService.showAlias(aliasId)
    }
  }

  @Command(name = "help")
  static class HelpCommand implements Runnable {
    @ParentCommand
    private DriverCommand driverCommand

    @Inject//dagger
    HelpCommand (){}

    @Override
    void run () {CommandLine.usage(driverCommand, System.out)}
  }

}
