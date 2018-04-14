package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsAliasDao
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDep
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsJar
import org.jboss.shrinkwrap.resolver.api.maven.Maven
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject
import javax.sql.DataSource
import java.lang.reflect.Method
import java.sql.Driver
import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * Resolves db-access command options providing a connection to the relevant db.
 */
//@CompileStatic
abstract class AbstractDbCommandService {

  private final static Logger LOG = LoggerFactory.getLogger (AbstractDbCommandService.class)
  private static final int DEFAULT_WIDTH = 80;

  @Inject
  public ConfigService configService
  @Inject
  public DriverManagerFacade driverManagerFacade
  @Inject
  public JdbsAliasDao jdbsAliasDao

  public AbstractDbCommandService(){}

  def doRun(final AbstractDbCommand dbCommand, Closure<Void> dbCallback) {
    dbCommand.with {
      String actualpass = askForPassword ? System.console().readPassword() : password

      def aliasDetails = alias != null ? jdbsAliasDao.findAlias(alias).orElse(null) : null

      def _username = { username ?: aliasDetails?.username ?: '' }
      def _password = { actualpass ?: aliasDetails?.password ?: '' } as Closure<String>
      def _driverClassName = { driverClassName ?: aliasDetails?.driverDetails?.driverClass }
      def _url = { url ?: aliasDetails?.url }
      def _driverClassMatches = { driverClassMatches ?: aliasDetails?.driverDetails?.driverClassExpr }

      final Closure<DataSource> createDataSource = { driver ->

        new DriverManagerDataSource(
            username: _username(),
            password: _password(),
            driverClassName: _driverClassName(),
            url: _url(),
            driverManagerFacade: driverManagerFacade,
            driver: driver
        )
      } as Closure<DataSource>

      def alljars = []
      if (jars) {
        alljars.addAll(jars)
      }
      def alldeps = []
      if (deps) {
        alldeps.addAll(deps)
      }

      if (aliasDetails) {
        //consider alias deps and jars for resolution
        aliasDetails.driverDetails?.jars?.each { JdbsJar jar->
          alljars << jar.file
        }
        aliasDetails.driverDetails?.deps?.each { JdbsDep dep->
          alldeps << dep.gav
        }
      }

      configService.getDropinsDirs().each {File dropinsDir->
        LOG.debug('Looking for dropins at {}', dropinsDir)
        if (dropinsDir && dropinsDir.exists()) {
          alljars.addAll(dropinsDir.listFiles({ dir, name -> name.toLowerCase().endsWith('.jar') } as FilenameFilter))
        }
      }
      alljars.flatten()
      if (alljars || alldeps) {
        LOG.debug('Loading jars {} and deps {}', alljars, alldeps)
        withDynamicDataSource(_driverClassName(), _driverClassMatches(), alljars, alldeps, createDataSource, dbCallback)
      } else {//no additional jars or deps... assume the driver manager is able to find the driver
        dbCallback(new DriverManagerDataSource(
            url: _url(),
            username: _username(),
            password: _password(),
            driverManagerFacade: driverManagerFacade
        ))
      }
    }
  }

  protected void withDynamicDataSource(
      final String driverClassName,
      final String driverClassMatches,
      final List<File> jars,
      final List<String> deps,
      final Closure<DataSource> createDataSource,
      final Closure<Void> callback) {
    //creates and test a new datasource opening  a connection
    Closure<DataSource> testDataSource = {Driver driver->
      def tmpDataSource = createDataSource(driver)
      try {
        tmpDataSource.getConnection().close()//tries to acquire a connection and release it

        return tmpDataSource
      } catch (Exception e) {
        throw new IllegalStateException ("Cannot initialize a dataSource for driverClassName: $driverClassName, driverClassMatches: $driverClassMatches, jars: $jars, deps: $deps", e)
      }
    }

    withDeps(driverClassName, driverClassMatches, jars, deps, testDataSource) {DataSource dataSource ->
        callback(dataSource)
    }
  }

  protected void withDeps(
          final String driverClassName,
          final String driverClassMatches,
          final List<File> jars,
          final List<String> deps,
          final Closure<DataSource> testDataSource,
          final Closure callback) {
    final ClassLoader currThreadClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      //chain jars classloader with original one
      final URL[] jarUrls = jars
              .findAll { it.exists() }         // existing file
              .collect { it.toURI().toURL() }.toArray(new URL[0])
      final ClassLoader cl
      if (jars) {
        cl = new URLClassLoader(jarUrls, currThreadClassLoader)
      } else {
        cl = currThreadClassLoader
      }
      GroovyClassLoader groovyClassLoader = new groovy.lang.GroovyClassLoader(cl)

      //resolve deps
      if (deps) {
        Maven.resolver().resolve(deps).withTransitivity().asFile().each {File file->
          groovyClassLoader.addURL file.toURI().toURL()
        }
      }

      LOG.info('Deps resolved {}', groovyClassLoader.getURLs())

      //collect dependency urls
      //since deps are cached, a diff would remove even the relevant ones
//      def urls = (ClasspathHelper.forClassLoader(groovyClassLoader) - ClasspathHelper.forClassLoader(currThreadClassLoader)).toArray() as URL[]
      def urls = ClasspathHelper.forClassLoader(groovyClassLoader)

      //add the URLs to the SystemClassLoader
      LOG.debug ('Determined deps urls {}', urls)
      DataSource dataSource = urls.findResult { URL url ->
        DataSource result
        if (url.file.endsWith('jar')) {
          LOG.debug('Registering {}', url)
          def sysloader = ClassLoader.getSystemClassLoader()


//          if (sysloader instanceof URLClassLoader) {
//            //java < 9, reflection
//            final Class sysclass = URLClassLoader.class
//
//            Method method = sysclass.getDeclaredMethod("addURL", [URL] as Class[])
//            method.setAccessible(true)
//            method.invoke(sysloader, url)
//
//            try {
//              result = testDataSource()
//            } catch (Throwable e) {// seems safe to ignore here :-/
//              LOG.trace('Caught exception {}', e)
//            }
//          } else {//java >= 9
            try {
              result = registerDriverClass(driverClassName, driverClassMatches, new File(url.file), new URLClassLoader([url] as URL[], sysloader), testDataSource);
            } catch (Throwable e) {// seems safe to ignore here :-/
              LOG.trace('Caught exception {}', e)
            }
//          }
        }
        return result
      } as DataSource

      Thread.currentThread().setContextClassLoader(groovyClassLoader)
      callback(dataSource)
    } finally {//restore original classloader
      Thread.currentThread().setContextClassLoader(currThreadClassLoader)
    }
  }

  protected DataSource registerDriverClass (
          final String driverClassName,
          final String driverClassMatches,
          final File jar,
          final ClassLoader driversLoader,
          final Closure<DataSource> testDataSource) {
    final JarFile jarFile = new JarFile(jar)
    for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
      final String name = jarEntry.getName();
      if (name.endsWith(".class")) {
        String classFQN = name.replace("/", ".").replaceAll('\\.class$', '')
        if ((driverClassName && classFQN == driverClassName) ||
                (!driverClassName && driverClassMatches && classFQN.matches(driverClassMatches))) {
          try {
            LOG.trace('Analyzing jar class {}', name)
            Driver driver = MiscTools.loadDriver(classFQN, jar.absolutePath)
            if (driver) {
              def sysprop = System.getProperty("jdbc.drivers")
              System.setProperty("jdbc.drivers", "${sysprop}:${classFQN}")

              return testDataSource(driver)
            }
//            final Class<?> clazz = Class.forName(classFQN, true, driversLoader)
//            if (Driver.class.isAssignableFrom(clazz)) {
//              //found a driver
//              try
//              {
//                MiscTools.loadDriver(classFQN, jar.absolutePath)
////                Class.forName(classFQN, true, MiscTools.addToClasspath(jar.absolutePath))
//              }
//              catch (ClassNotFoundException | IllegalArgumentException | SecurityException e)
//              {
//                LOG.warn("Error loading {}", classFQN, e);
//              }
////              clazz.getDeclaredConstructor().newInstance()
//              LOG.debug('Trying to activate driver class {}', clazz.name)
//              //returning the first configured datasource
//              return testDataSource()
//            }
          } catch (NoClassDefFoundError | Exception ignore) {
            // Safe to ignore, as this is a hack to force loading naive drivers
            LOG.trace('Cannot load class', ignore)
          }
        }
      }
    }
  }

  int getWidth(final AbstractDbCommand abstractDbCommand) {
    def width = abstractDbCommand.width
    return width>0?width:computeWidth()
  }

  protected int computeWidth () {
    try {
      final Terminal term = TerminalBuilder.builder().build();

      return Math.max (DEFAULT_WIDTH, term.getWidth());
    } catch (Exception e) {
      return DEFAULT_WIDTH;
    }
  }


}
