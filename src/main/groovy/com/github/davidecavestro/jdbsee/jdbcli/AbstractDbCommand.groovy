package com.github.davidecavestro.jdbsee.jdbcli

import picocli.CommandLine

abstract class AbstractDbCommand implements CliCommand {
  @CommandLine.Option(names = ["-a", "--alias"], description = "Database alias, to reuse previously persisted settings")
  String alias

  @CommandLine.Option(names = ["-l", "--url"], description = "The JDBC url (if provided along with alias overrides its url)")
  String url

  @CommandLine.Option(names = ["-u", "--user"], description = "The username (if provided along with alias overrides its url)")
  String username = ""

  @CommandLine.Option(names = ["-p", "--password"], description = "The password (if provided along with alias overrides its url)")
  String password = ""

  @CommandLine.Option(names = ["-P", "--ask-for-password"], description = "Ask for database password before connecting")
  boolean askForPassword

  @CommandLine.Option(names = ["-j", "--jar"], description = "External jar file to search for the driver classes")
  List<File> jars

  @CommandLine.Option(names = ["-d", "--dependency"], description = "Maven artifact dependency to be resolved for driver class loading")
  List<String> deps

  @CommandLine.Option(names = ["-c", "--driver-class"], description = "External driver class name (detected from URL if not specified)")
  String driverClassName

  @CommandLine.Option(names = ["-m", "--driver-class-match"], description = "Regex used to detect driver class by name. Defaults to '(.*)Driver(.*)'")
  String driverClassMatches = '(.*)Driver(.*)'

  @CommandLine.Option(names = ["-w", "--output-width"], description = "Output table width")
  int width = -1
}
