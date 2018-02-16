package com.github.davidecavestro.jdbsee.jdbcli;

import javax.inject.Inject;
import java.io.File;

import static picocli.CommandLine.*;

@Command(name = "driver")
public class DriverCommand implements Runnable {
  @ParentCommand
  private MainCommand mainCommand;

  @Inject
  DriverService driverService;

  DriverCommand (){}

  @Override
  public void run () {

  }

  @Command(name = "add")
  static class AddCommand implements Runnable {

    @ParentCommand
    private DriverCommand driverCommand;

    private final DriverService driverService;

    @Parameters(arity = "1", paramLabel = "NAME", description = "The name of the driver.")
    private String name;

    @Parameters(arity = "1", paramLabel = "DRIVER_CLASS", description = "The fully qualified name of the driver class.")
    private String driverClass;

    @Parameters(arity = "0..*", paramLabel = "JAR", description = "Jars(s) to search for driver.")
    private File[] jars;

    @Option(names = {"-u", "--uri"}, description = "Example URI")
    private String uri;

    @Inject
    public AddCommand (final DriverService driverService){
      this.driverService = driverService;
    }

    @Override
    public void run () {
      driverService.insert (name, driverClass, uri, jars);
    }
  }


  @Command(name = "delete")
  static class DeleteCommand implements Runnable {
    @ParentCommand
    private DriverCommand driverCommand;

    @Inject
    DriverService driverService;

    @Parameters(arity = "1..*", paramLabel = "NAME", description = "The name of the driver.")
    private String[] names;

    public DeleteCommand () {}

    @Override
    public void run () {
      driverService.delete (names);
    }
  }
}
