package com.github.davidecavestro.jdbsee.jdbcli;


import dagger.Component;
import picocli.CommandLine;
import picocli.CommandLine.*;
import dagger.*;

import javax.inject.Singleton;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

@Command(versionProvider = PropertiesVersionProvider.class)
public class JdbCliApplication implements Runnable {

  @Option(names = {"-V", "--version"}, versionHelp = true, description = "Print version info and exit")
  boolean versionRequested;

  public void run() {
    CommandLine.usage(this, System.err);
  }

  public static void main(final String[] args) {
    DaggerAppComponent.Builder builder = DaggerAppComponent.builder ();
    final AppComponent appComponent = builder.build ();
    final MainCommand mainCommand = appComponent.getMainCommand ();
    appComponent.getTomcatJuliHack ().install ();
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
  TomcatJuliHack getTomcatJuliHack();
  void inject (DriverCommand command);
  void inject (RunCommand command);
}

@Module
class AppModule {
  @Provides
  MainCommand mainCommand (){
    return new MainCommand ();
  }
  TomcatJuliHack tomcatJuliHack() {return new TomcatJuliHack();}
}
