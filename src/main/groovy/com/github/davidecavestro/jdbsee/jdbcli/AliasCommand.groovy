package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsAliasDao
import groovy.transform.CompileStatic
import picocli.CommandLine

import javax.inject.Inject

import static picocli.CommandLine.*

@CompileStatic
@Command(name = "alias", description = "Provides persistent settings for db connections",
    subcommands = [
        AliasCreateCommand, AliasDeleteCommand,
        AliasListCommand, AliasShowCommand,
        HelpCommand
    ]
)
class AliasCommand implements CliCommand {
  @ParentCommand
  private MainCommand mainCommand

  @Inject//public for dagger
  public JdbsAliasDao jdbsAliasDao

  @Inject//public for dagger
  public ConsoleService consoleService

  @Inject
  AliasCommand(){}

  @Override
  void run () {
    consoleService.usage(this)
  }

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
    public AliasService aliasService

    @Inject//dagger
    AliasListCommand () {}

    @Override
    void run () {
      aliasService.listAliases()
    }
  }

  @Command(name = "show", description = "Show details about alias settings")
  static class AliasShowCommand implements Runnable {
    @ParentCommand
    private AliasCommand aliasCommand

    @Inject
    public AliasService aliasService

    @Parameters(index = "0", arity = "1", paramLabel = "ALIAS_ID", description = "The ID of the alias.")
    Long aliasId

    @Inject//dagger
    AliasShowCommand () {}

    @Override
    void run () {
      aliasService.showAlias(aliasId)
    }
  }

  @Command(name = "help", description = "Print this help")
  static class HelpCommand implements Runnable {
    @ParentCommand
    private AliasCommand aliasCommand

    @Inject//public for dagger
    public ConsoleService consoleService

    @Inject//dagger
    HelpCommand (){}

    @Override
    void run () {
      consoleService.withSysOutStream {PrintStream outStream->
        CommandLine.usage(aliasCommand, outStream)
      }
    }
  }

}
