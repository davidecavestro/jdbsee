package com.github.davidecavestro.jdbsee.jdbcli

import groovy.transform.CompileStatic;

import javax.inject.Inject;

import static picocli.CommandLine.*;

@CompileStatic
@Command(name = "run")
public class RunCommand implements Runnable {

  @Inject
  public RunCommandService runCommandService;

//  @Parameters(arity = "0..1", paramLabel = "DRIVER_CLASS_NAME", description = "The JDBC driver class name.")
//  private String driverClassName;

  @Parameters(index = "0", paramLabel = "URL", description = "The JDBC url.")
  String url;

  @Parameters(index = "1..*", paramLabel = "QUERY", description = "The SQL to run.")
  String sqlText;

  @Option(names = ["-u", "--user"], description = "The username")
  String username = "";

  @Option(names = ["-p", "--password"], description = "The password")
  String password = "";

//  @Parameters(arity = "0..1", paramLabel = "DATA_SOURCE", description = "The name of the data source.")
//  private String dataSourceName;

//  @Option(names = ["-f", "--file"], description = "File containing the query to run.")
//  File sqlFile;

  @Option(names = ["-x", "--execute"], description = "Additional SQL commands to be executed before the specified QUERY")
  List<String> execute;

  @Option(names = ["-j", "--jar"], description = "External jar file to search for the driver classes")
  List<File> jars;

  @Option(names = ["-d", "--driver-class"], description = "External driver class name (detected from URL if not specified)")
  String driverClassName;

  @Inject
  public RunCommand (
//      final DataSourceService dataSourceService,
//      final ConsoleService consoleService,
//      final QueryService queryService
  ){
//    this.dataSourceService = dataSourceService;
//    this.consoleService = consoleService;
//    this.queryService = queryService;
  }

  @Override
  public void run () {
    runCommandService.run (this);
  }
}
