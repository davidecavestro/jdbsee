package com.github.davidecavestro.jdbsee.jdbcli

import groovy.transform.CompileStatic;

import javax.inject.Inject;

import static picocli.CommandLine.*;

@CompileStatic
@Command(name = "run")
public class RunCommand implements Runnable {

  @Inject//public for dagger
  public RunCommandService runCommandService

  @Option(names = ["-h", "--help"], usageHelp = true, description = "display this help message")
  boolean help

  @Parameters(index = "0", paramLabel = "URL", description = "The JDBC url.")
  String url

  @Parameters(index = "1..*", paramLabel = "QUERY", description = "The SQL to run.")
  String sqlText

  @Option(names = ["-u", "--user"], description = "The username")
  String username = ""

  @Option(names = ["-p", "--password"], description = "The password")
  String password = ""

  @Option(names = ["-P", "--ask-for-password"], description = "Ask for database password before connecting")
  boolean askForPassword

//  @Parameters(arity = "0..1", paramLabel = "DATA_SOURCE", description = "The name of the data source.")
//  private String dataSourceName;

//  @Option(names = ["-f", "--file"], description = "File containing the query to run.")
//  File sqlFile;

  @Option(names = ["-x", "--execute"], description = "Additional SQL commands to be executed before the specified QUERY")
  List<String> execute

  @Option(names = ["-j", "--jar"], description = "External jar file to search for the driver classes")
  List<File> jars

  @Option(names = ["-d", "--dependency"], description = "Maven artifact dependency to be resolved for driver class loading")
  List<String> deps

  @Option(names = ["-c", "--driver-class"], description = "External driver class name (detected from URL if not specified)")
  String driverClassName

  @Option(names = ["-m", "--driver-class-match"], description = "Regex used to detect driver class by name. Defaults to '(.*)Driver(.*)'")
  String driverClassMatches = '(.*)Driver(.*)'

  @Option(names = ["-t", "--output-format"], paramLabel = "OUTPUT_FORMAT", description = "Select output format. One between TABLE, CSV, JSON, JSON_PRETTY")
  OutputType outputType = OutputType.TABLE;

  @Option(names = ["-o", "--output-file"], paramLabel = "OUTPUT_FILE", description = "File to use for saving output")
  File outputFile;

  @Inject//needed for dagger
  public RunCommand (){}

  @Override
  public void run () {
    runCommandService.run (this);
  }
}
