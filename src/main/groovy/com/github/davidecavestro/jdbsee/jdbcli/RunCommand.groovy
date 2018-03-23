package com.github.davidecavestro.jdbsee.jdbcli

import groovy.transform.CompileStatic
import picocli.CommandLine;

import javax.inject.Inject;

import static picocli.CommandLine.*;

@CompileStatic
@Command(name = "run",
    description = "Executes queries passing on-the-fly the db settings (driver, username)",
    showDefaultValues = true,
    subcommands = [HelpCommand]
)
class RunCommand extends AbstractDbCommand {

  @Inject//public for dagger
  public RunCommandService runCommandService

  @Option(names = ["-h", "--help"], usageHelp = true, description = "display this help message")
  boolean help

  @Parameters(arity = "0..1", index = "0..*", paramLabel = "QUERY", description = "The SQL to run.")
  String sqlText

//  @Option(names = ["-f", "--file"], description = "File containing the query to run.")
//  File sqlFile;

  @Option(names = ["-x", "--execute"], description = "Additional SQL commands to be executed before the specified QUERY")
  List<String> execute

  @Option(names = ["-t", "--output-format"], paramLabel = "OUTPUT_FORMAT", description = "Select output format. One between TABLE, CSV, JSON, JSON_PRETTY")
  OutputType outputType = OutputType.TABLE;

  @Option(names = ["-o", "--output-file"], paramLabel = "OUTPUT_FILE", description = "File to use for saving output")
  File outputFile;

  @Inject//needed for dagger
  public RunCommand (){}

  @Override
  void run () {
    runCommandService.run (this)
  }

  @Command(name = "help", description = "Print this help")
  static class HelpCommand implements Runnable {
    @ParentCommand
    private RunCommand runCommand

    @Inject
    public ConsoleService consoleService

    @Inject//dagger
    HelpCommand (){}

    @Override
    void run () {CommandLine.usage(runCommand, consoleService.sysErrStream)}
  }

}
