package com.github.davidecavestro.jdbsee.jdbcli

import groovy.grape.Grape
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

  DataSourceService dataSourceService;
  ConsoleService consoleService;
  QueryService queryService;

  @Inject
  public RunCommandService (
      final DataSourceService dataSourceService,
      final ConsoleService consoleService,
      final QueryService queryService
  ){
    this.dataSourceService = dataSourceService;
    this.consoleService = consoleService;
    this.queryService = queryService;
  }

  public void run (final RunCommand runCommand) {

//    final ConnectionFactory connectionFactory;
//    final DataSource dataSource;
//
//    if (dataSourceName!=null) {
//      dataSource = dataSourceService.getDataSource (dataSourceName, username, password);
//    } else if (url!=null) {
//      queryService.execute (driverClassName, url, username, password, sqlText);
//    } else {
//      throw new IllegalArgumentException ("One between DATA_SOURCE_NAME and URL args must be passed");
//    }

    runCommand.with {
      if (sqlText != null) {
        final List<String> sql = new ArrayList<>();
        if (execute) {
          sql.addAll(execute);
        }
        sql.add(sqlText);

        String[] sqlArray = sql.toArray(new String[sql.size()])

        final QueryCallback<Void, Exception> callback = { Query query ->
          try {
            consoleService.renderResultSet(query, outputType)
          } catch (final SQLException e) {
            throw new RuntimeException(e)
          }
          return null
        }

        def createDataSource = {
//              new BasicDataSource(
          new org.apache.tomcat.jdbc.pool.DataSource(
              username: username,
              password: password,
//                      driverClassLoader: driversLoader,
              driverClassName: driverClassName,
              url: url
          )
        }

        if (jars || deps) {
          withDynamicDataSource (jars, deps, createDataSource) {dataSource->
            queryService.execute(dataSource, callback, sqlArray);
          }
        } else {
          queryService.execute(url, username, password, callback, sqlArray);
        }
      } else {
        //FIXME
      }
    }
//      } else {//FIXME
//        consoleService.renderResultSet (queryService.execute (dataSource, sqlFile, callback));
  }

  def withDynamicDataSource(
      final List<File> jars,
      final List<String> deps,
      final Closure<DataSource> createDataSource,
      final Closure<Void> callback) {
    def testDataSource = {recover->
      def tmpDataSource = createDataSource()
      try {
//              tmpDataSource.getConnection().close()//tries to acquire a connection and release it
        return tmpDataSource
      } catch (SQLException e) {
        if (recover) {
          recover()
        }
      }
    }

    withDeps(jars, deps, testDataSource) {
      ClassLoader driversLoader ->

        DataSource dataSource = testDataSource {
          //retries forcing class loading
          for (final File jar : jars) {
            JarFile jarFile = new JarFile(jar)
            for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
              String name = jarEntry.getName();
              if (name.endsWith(".class")) {
                try {
                  LOG.trace ('Analyzing jar class {}', name)
                  final Class<?> clazz = Class.forName(name.replace("/", ".").replaceAll('\\.class$', ''), true, driversLoader);
                  if (Driver.class.isAssignableFrom(clazz)) {
                    //found a driver
                    LOG.info ('Trying to activate driver class {}', clazz.name)
                    def tmpDataSource = createDataSource()
                    tmpDataSource.getConnection().close()//tries to acquire a connection and release it

                    return tmpDataSource
                  }
                } catch (Exception ignore) {
                  // Safe to ignore, as this is a hack to force loading naive drivers
                  LOG.trace ('Cannot load class', ignore)
                }
              }
            }
          }
        }
        callback(dataSource)
    }
  }

  void withDeps(final List<File> files, final List<String> deps, def testDataSource, final Closure closure) {
    final ClassLoader currThreadClassLoader = Thread.currentThread().getContextClassLoader();
    try {//chain jars classloader with original one
      final ClassLoader cl;
      if (files) {
        cl = new URLClassLoader(files
            .findAll { it.exists() }         // existing file
            .collect { it.toURI().toURL() }.toArray(new URL[0]), // pick its URL
            currThreadClassLoader);
      } else {
        cl = currThreadClassLoader
      }
      ClassLoader groovyClassLoader = new groovy.lang.GroovyClassLoader(cl)
      Map[] grapez = deps.collect {
        def depParts = it.split('\\:')
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
//      Grape.grab([classLoader: groovyClassLoader] as Map<String,Object>, grapez)
      def grab = Grape.getInstance().grab([classLoader: groovyClassLoader] as Map<String, Object>, grapez)
      LOG.info('Deps resolved {}', Grape.getInstance().enumerateGrapes())
//      def driver = Class.forName('org.postgresql.Driver', true, groovyClassLoader)
//      DriverManager.registerDriver(driver.newInstance())

      //using org.reflections.util.ClasspathHelper to get the relevant URLs
      def urls = (ClasspathHelper.forClassLoader(groovyClassLoader) - ClasspathHelper.forClassLoader(currThreadClassLoader)).toArray() as URL[]

      //and finally adding the URLs to the SystemClassLoader with ClassPathHacker
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

            testDataSource()
          } else {//java >= 9
            try {
              registerDriver(new File(url.file), new URLClassLoader([url] as URL[], sysloader), testDataSource);
            } catch (Throwable e) {// seems safe to ignore here :-/
              LOG.trace('Caught exception {}', e)
            }
          }
        }
      }

      Thread.currentThread().setContextClassLoader(groovyClassLoader);
      closure(groovyClassLoader)
    } finally {//restore original classloader
      Thread.currentThread().setContextClassLoader(currThreadClassLoader);
    }
  }

  protected DataSource registerDriver (final File jar, ClassLoader driversLoader, def testDataSource) {
    final JarFile jarFile = new JarFile(jar)
    for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
      String name = jarEntry.getName();
      if (name.endsWith(".class")) {
        try {
          LOG.trace ('Analyzing jar class {}', name)
          final Class<?> clazz = Class.forName(name.replace("/", ".").replaceAll('\\.class$', ''), true, driversLoader);
          if (Driver.class.isAssignableFrom(clazz)) {
            //found a driver
            LOG.trace ('Trying to activate driver class {}', clazz.name)

            testDataSource()
          }
        } catch (Exception ignore) {
          // Safe to ignore, as this is a hack to force loading naive drivers
          LOG.trace ('Cannot load class', ignore)
        }
      }
    }
  }

  protected void testDataSource (def createDataSource) {
    def tmpDataSource = createDataSource()
    tmpDataSource.getConnection().close()//tries to acquire a connection and release it
  }
}
