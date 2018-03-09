package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsAliasDao
import groovy.grape.Grape
import groovy.io.FileType
import groovy.transform.CompileStatic
import org.jdbi.v3.core.statement.Query
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject
import javax.sql.DataSource
import java.lang.reflect.Method
import java.sql.Driver
import java.sql.DriverManager
import java.sql.SQLException
import java.util.jar.JarEntry
import java.util.jar.JarFile

//@CompileStatic
public class RunCommandService {

  private final static Logger LOG = LoggerFactory.getLogger (RunCommandService.class)

  @Inject
  public ConfigService configService
  @Inject
  public ConsoleService consoleService
  @Inject
  public QueryService queryService
  @Inject
  public DriverManagerFacade driverManagerFacade
  @Inject
  public JdbsAliasDao jdbsAliasDao

  @Inject
  public RunCommandService (
//      final ConfigService configService,
//      final ConsoleService consoleService,
//      final QueryService queryService,
//      final DriverManagerFacade driverManagerFacade
  ){
//    this.configService = configService
//    this.consoleService = consoleService;
//    this.queryService = queryService
//    this.driverManagerFacade = driverManagerFacade
  }

  public void run (final RunCommand runCommand) {
    runCommand.with {
      if (sqlText != null) {//use the passed query
        final List<String> sql = new ArrayList<>()
        if (execute) {
          sql.addAll execute
        }
        sql.add sqlText

        String[] sqlArray = sql.toArray(new String[sql.size()])

        final QueryCallback<Void, Exception> callback = { Query query ->
          try {
            consoleService.renderResultSet(query, outputType, outputFile)
          } catch (final SQLException e) {
            throw new RuntimeException(e)
          }
          return null
        }

        String actualpass = askForPassword?System.console().readPassword():password

        def aliasDetails = alias!=null?jdbsAliasDao.findAlias (alias).orElse(null):null

        def _username = {username?:aliasDetails?.username?:''}
        def _password = {actualpass?:aliasDetails?.password?:''}
        def _driverClassName = {driverClassName?:aliasDetails?.driverDetails?.driverClass}
        def _url = {url?:aliasDetails?.url}
        def _driverClassMatches = {driverClassMatches?:aliasDetails?.driverDetails?.driverClassExpr}

        final Closure<DataSource> createDataSource = {
//              new BasicDataSource(
//          (DataSource)new org.apache.tomcat.jdbc.pool.DataSource(
          new DriverManagerDataSource (
              username: _username(),
              password: _password(),
//                      driverClassLoader: driversLoader,
              driverClassName: _driverClassName,
              url: _url (),
              driverManagerFacade: driverManagerFacade
          )
        }

        def alljars = []
        if (jars) {
          alljars.addAll (jars)
        }
        def dropinsDir = configService.getDropinsDir()
        LOG.debug('Looking for dropins at {}', dropinsDir)
        if (dropinsDir && dropinsDir.exists()) {
          alljars.addAll (dropinsDir.listFiles ({dir, name -> name.toLowerCase().endsWith('.jar')} as FilenameFilter))
        }
        alljars.flatten()
        if (alljars || deps) {
          LOG.debug('Loading jars {} and deps {}', alljars, deps)
          withDynamicDataSource (_driverClassName(), _driverClassMatches(), alljars, deps, createDataSource) {dataSource->
            queryService.execute(dataSource, callback, sqlArray);
          }
        } else {//no additional jars or deps... assume the driver manager is able to find the driver
          queryService.execute(_url(), _username(), _password(), callback, sqlArray);
        }
      } else {//FIXME
//        consoleService.renderResultSet (queryService.execute (dataSource, sqlFile, callback));
      }
    }
  }

  def withDynamicDataSource(
      final String driverClassName,
      final String driverClassMatches,
      final List<File> jars,
      final List<String> deps,
      final Closure<DataSource> createDataSource,
      final Closure<Void> callback) {
    //creates and test a new datasource opening  a connection
    Closure<DataSource> testDataSource = {recover->
      def tmpDataSource = createDataSource()
      try {
        tmpDataSource.getConnection().close()//tries to acquire a connection and release it
        return tmpDataSource
      } catch (Exception e) {
        if (recover) {
            return recover()
        }
        throw new IllegalStateException ("Cannot initialize a dataSource for driverClassName: $driverClassName, driverClassMatches: $driverClassMatches, jars: $jars, deps: $deps", e)
      }
    }

    withDeps(driverClassName, driverClassMatches, jars, deps, testDataSource) {ClassLoader driversLoader ->
        callback(testDataSource ())
    }
  }

  void withDeps(
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
      final ClassLoader cl;
      if (jars) {
        cl = new URLClassLoader(jarUrls, currThreadClassLoader);
      } else {
        cl = currThreadClassLoader
      }
      ClassLoader groovyClassLoader = new groovy.lang.GroovyClassLoader(cl)

      //resolve deps
      Map[] grapez = deps.collect {
        def depParts = it.split('\\:')
        if (!depParts || depParts.length<3) {
          throw new IllegalArgumentException("Invalid dependency reference: $it")
        }
        [groupId: depParts[0], artifactId: depParts[1], version: depParts[2], objectRef: DriverManager]
        .with{Map map->
          if (depParts.size()>3) {
            scope = depParts[3]
          }
          map
        } as Map<String, Object>
      }
      LOG.info('Resolving {}', grapez)
      Grape.enableGrapes = true

      Grape.getInstance().grab([classLoader: groovyClassLoader] as Map<String, Object>, grapez)
      LOG.info('Deps resolved {}', Grape.getInstance().enumerateGrapes())

      //collect dependency urls
      def urls = (ClasspathHelper.forClassLoader(groovyClassLoader) - ClasspathHelper.forClassLoader(currThreadClassLoader)).toArray() as URL[]

      //add the URLs to the SystemClassLoader
      LOG.debug ('Determined deps urls {}', urls)
      urls.each { URL url ->
        if (url.file.endsWith('jar')) {
          LOG.debug('Registering {}', url)
          def sysloader = ClassLoader.getSystemClassLoader();
          if (sysloader instanceof URLClassLoader) {
            //java < 9, reflection
            final Class sysclass = URLClassLoader.class;

            Method method = sysclass.getDeclaredMethod("addURL", [URL] as Class[]);
            method.setAccessible(true);
            method.invoke(sysloader, url);
//
//            try {
//              testDataSource()
//            } catch (Throwable e) {// seems safe to ignore here :-/
//              LOG.trace('Caught exception {}', e)
//            }
        } else {//java >= 9
            try {
              registerDriverClass(driverClassName, driverClassMatches, new File(url.file), new URLClassLoader([url] as URL[], sysloader), testDataSource);
            } catch (Throwable e) {// seems safe to ignore here :-/
              LOG.trace('Caught exception {}', e)
            }
          }
        }
      }

      Thread.currentThread().setContextClassLoader(groovyClassLoader);
      callback(groovyClassLoader)
    } finally {//restore original classloader
      Thread.currentThread().setContextClassLoader(currThreadClassLoader);
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
            final Class<?> clazz = Class.forName(classFQN, true, driversLoader);
            if (Driver.class.isAssignableFrom(clazz)) {
              //found a driver
              clazz.getDeclaredConstructor().newInstance()
              LOG.debug('Trying to activate driver class {}', clazz.name)
              //returning the first configured datasource
              return testDataSource()
            }
          } catch (NoClassDefFoundError | Exception ignore) {
            // Safe to ignore, as this is a hack to force loading naive drivers
            LOG.trace('Cannot load class', ignore)
          }
        }
      }
    }
  }
}
