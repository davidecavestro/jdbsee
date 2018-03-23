package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDao
import picocli.CommandLine

import javax.inject.Inject

import static picocli.CommandLine.*

@Command(name = "driver", description = "Provides persistent settings for drivers",
    subcommands = [
        DriverCreateCommand, DriverDeleteCommand,
        DriverListCommand, DriverShowCommand,
        JarAddCommand, JarRemoveCommand,
        DependencyAddCommand, DependencyRemoveCommand,
        HelpCommand
    ]
)
class DriverCommand implements Runnable {
  @ParentCommand
  private MainCommand mainCommand

  @Inject//public for dagger
  public JdbsDriverDao jdbsDriverDao

  @Inject
  DriverCommand (){}

  @Override
  void run () {CommandLine.usage(this, mainCommand.sysOut)}

  @Command(name = "create", description = "Register a new driver")
  static class DriverCreateCommand implements Runnable {

    @ParentCommand
    private DriverCommand driverCommand

    @Parameters(index = "0", arity = "1", paramLabel = "NAME", description = "The name of the driver.")
    String name

    @Option(names = ["-c", "--driver-class"], description = "Fully qualified name of the driver class")
    String driverClassName

    @Option(names = ["-m", "--driver-class-match"], description = "Regex used to detect driver class by name. Defaults to '(.*)Driver(.*)'")
    String driverClassMatches = '(.*)Driver(.*)'

    @Option(names = ["-j", "--jar"], description = "External jar file to search for the driver classes")
    List<File> jars

    @Option(names = ["-d", "--dependency"], description = "Maven artifact dependency to be resolved for driver class loading")
    List<String> deps

//    @Option(names = {"-u", "--uri"}, description = "Example URI")
//    private String uri

    @Inject//dagger
    DriverCreateCommand (){}

    @Override
    void run () {
      driverCommand.jdbsDriverDao.insert (name, driverClassName, driverClassMatches, jars, deps)
    }
  }


  @Command(name = "delete", description = "Delete a driver")
  static class DriverDeleteCommand implements Runnable {
    @ParentCommand
    DriverCommand driverCommand

    @Parameters(index = "0..*", arity = "1", paramLabel = "DRIVER_ID", description = "The ID of the driver.")
    List<Long> driverIds

    @Inject//dagger
    DriverDeleteCommand () {}

    @Override
    void run () {
      driverCommand.jdbsDriverDao.delete (driverIds as Long[])
    }
  }

  @Command(name = "list", description = "List registered drivers")
  static class DriverListCommand implements Runnable {
    @ParentCommand
    DriverCommand driverCommand

    @Inject
    public DriverService driverCommandService

    @Inject//dagger
    DriverListCommand () {}

    @Override
    void run () {
      driverCommandService.listDrivers()
    }
  }

  @Command(name = "show", description = "Show details about driver settings")
  static class DriverShowCommand implements Runnable {
    @ParentCommand
    private DriverCommand driverCommand

    @Inject
    public DriverService driverCommandService

    @Parameters(index = "0", arity = "1", paramLabel = "DRIVER_ID", description = "The ID of the driver.")
    Long driverId

    @Inject//dagger
    DriverShowCommand () {}

    @Override
    void run () {
      driverCommandService.showDriver(driverId)
    }
  }

  @Command(name = "jar-add", description = "Add jars to an existing driver")
  static class JarAddCommand implements Runnable {

    @ParentCommand
    DriverCommand driverCommand

    @Parameters(index = "0", arity = "1", paramLabel = "DRIVER_ID", description = "The ID of the driver.")
    Long driverId

    @Parameters(index = "1..*", arity = "0..*", paramLabel = "JAR", description = "Jars(s) to add.")
    List<File> jars

    @Inject//dagger
    JarAddCommand (){}

    @Override
    void run () {
      driverCommand.jdbsDriverDao.addJar (driverId, jars as File[])
    }
  }


  @Command(name = "jar-remove", description = "Remove jars from driver")
  static class JarRemoveCommand implements Runnable {
    @ParentCommand
    DriverCommand driverCommand

    @Parameters(index = "0..*", arity = "1..*", paramLabel = "JAR_ID", description = "The ID of the jar to remove.")
    private List<Long> ids

    @Inject//dagger
    JarRemoveCommand (){}

    @Override
    void run () {
      driverCommand.jdbsDriverDao.removeJar (ids as Long[])
    }
  }

  @Command(name = "dependency-add", description = "Add automatically resolved dependencies to a registered driver")
  static class DependencyAddCommand implements Runnable {

    @ParentCommand
    private DriverCommand driverCommand

    @Parameters(index = "0", arity = "1", paramLabel = "DRIVER_ID", description = "The ID of the driver.")
    private Long driverId

    @Parameters(index = "1..*", arity = "0..*", paramLabel = "DEPENDENCY", description = "Dependency to add.")
    private List<String> deps

    @Inject//dagger
    DependencyAddCommand (){}

    @Override
    void run () {
      driverCommand.jdbsDriverDao.addDependency(driverId, deps as String[])
    }
  }


  @Command(name = "dependency-remove", description = "Remove dependencies from a driver")
  static class DependencyRemoveCommand implements Runnable {
    @ParentCommand
    private DriverCommand driverCommand

    @Parameters(index = "0..*", arity = "1..*", paramLabel = "DEPENDENCY_ID", description = "The ID of the dependency to remove.")
    private List<Long> ids

    @Inject//dagger
    DependencyRemoveCommand (){}

    @Override
    void run () {
      driverCommand.jdbsDriverDao.removeDependency(ids as Long[])
    }
  }

  @Command(name = "help", description = "Print this help")
  static class HelpCommand implements Runnable {
    @ParentCommand
    private DriverCommand driverCommand

    @Inject//dagger
    HelpCommand (){}

    @Override
    void run () {CommandLine.usage(driverCommand, mainCommand.sysOut)}
  }
}
