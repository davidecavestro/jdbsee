package com.github.davidecavestro.jdbsee.jdbcli;


import dagger.Component;
import picocli.CommandLine;
import picocli.CommandLine.*;
import dagger.*;

import javax.inject.Singleton;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@Command(versionProvider = PropertiesVersionProvider.class)
public class CliApplication implements Runnable {

  @Option(names = {"-V", "--version"}, versionHelp = true, description = "Print version info and exit")
  boolean versionRequested;

  public void run() {
    CommandLine.usage(this, System.err);
  }

  public static void main(final String[] args) {
    DaggerAppComponent.Builder builder = DaggerAppComponent.builder ();
    final AppComponent appComponent = builder.build ();
    final MainCommand mainCommand = appComponent.getMainCommand ();
    final IFactory daggerFactory = new AppIFactory (appComponent);
    CommandLine commandLine = new CommandLine (mainCommand, daggerFactory);
    commandLine.parseWithHandler(new RunLast(), System.err, args);
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
