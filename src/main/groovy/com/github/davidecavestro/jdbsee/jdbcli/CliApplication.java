package com.github.davidecavestro.jdbsee.jdbcli;


import dagger.Component;
import picocli.CommandLine;
import picocli.CommandLine.*;
import dagger.*;

import javax.inject.Singleton;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class CliApplication implements Runnable {

  public void run() {
    CommandLine.usage(this, System.err);
  }

  public static void main(final String[] args) {
    multiCmd(new String[][] {args});
  }

  public static void multiCmd(final String[][] multiArgs) {
    Thread.currentThread().setContextClassLoader(MiscTools.addToClasspath());
    DaggerAppComponent.Builder builder = DaggerAppComponent.builder ();
    final AppComponent appComponent = builder.build ();

    final AsciiTableResultSetScanner ascii = new AsciiTableResultSetScanner();
    appComponent.inject (ascii);
    final ConsoleService consoleService = new ConsoleService (ascii);
    appComponent.inject (consoleService);

    final MainCommand mainCommand = appComponent.getMainCommand ();
    mainCommand.setAppComponent (appComponent);
    mainCommand.setConsoleService (consoleService);

    final IFactory daggerFactory = new AppIFactory (appComponent);
    CommandLine commandLine = new CommandLine (mainCommand, daggerFactory);
    for (final String[] args : multiArgs) {
      commandLine.parseWithHandler(new RunLast(), System.err, args);
    }
  }

}

class AppIFactory implements IFactory {
  private final AppComponent appComponent;

  AppIFactory (final AppComponent appComponent) {
    this.appComponent = appComponent;
  }

  @Override
  public <T> T create(Class<T> cls) throws Exception {
    T instance;
    try {
      instance = cls.newInstance();
    } catch (Exception ex) {
      Constructor<T> constructor = cls.getDeclaredConstructor();
      constructor.setAccessible(true);
      instance = constructor.newInstance();
    }

    final Method method = appComponent.getClass ().getMethod ("inject", new Class[]{cls});
    if (method==null) {
      throw new IllegalArgumentException (String.format ("Cannot create command instance for %s", cls));
    }
    //inject fields
    method.invoke (appComponent, instance);

    return instance;
  }
}

@Singleton
@Component(modules = { AppModule.class})
interface AppComponent {
  MainCommand getMainCommand();
  void inject (PropertiesVersionProvider versionProvider);
  void inject (ConsoleService consoleService);
  void inject (AsciiTableResultSetScanner ascii);

  void inject (HelpCommand command);
  void inject (ShellCommand command);
//  void inject (ShellCommand.QuitCommand command);
  void inject (ShellCommand.HelpCommand command);

  void inject (ConnectCommand command);
  //  void inject (ShellCommand.QuitCommand command);
  void inject (ConnectCommand.HelpCommand command);

  void inject (DriverCommand command);
  void inject (DriverCommand.DriverCreateCommand command);
  void inject (DriverCommand.DriverDeleteCommand command);
  void inject (DriverCommand.DriverListCommand command);
  void inject (DriverCommand.DriverShowCommand command);
  void inject (DriverCommand.JarAddCommand command);
  void inject (DriverCommand.JarRemoveCommand command);
  void inject (DriverCommand.DependencyAddCommand command);
  void inject (DriverCommand.DependencyRemoveCommand command);
  void inject (DriverCommand.HelpCommand command);

  void inject (AliasCommand command);
  void inject (AliasCommand.AliasCreateCommand command);
  void inject (AliasCommand.AliasDeleteCommand command);
  void inject (AliasCommand.AliasListCommand command);
  void inject (AliasCommand.AliasShowCommand command);
  void inject (AliasCommand.HelpCommand command);

  void inject (DescribeCommand command);
  void inject (DescribeCommand.DescribeTablesCommand command);
  void inject (DescribeCommand.DescribeViewsCommand command);
  void inject (DescribeCommand.DescribeDriverCommand command);
  void inject (DescribeCommand.HelpCommand command);

  void inject (RunCommand command);
  void inject (RunCommand.HelpCommand command);
}

@Module
class AppModule {
  @Provides
  MainCommand mainCommand (){
    return new MainCommand ();
  }
}
