package com.github.davidecavestro.jdbsee.jdbcli.config

import com.github.davidecavestro.jdbsee.jdbcli.ConfigService
import com.github.davidecavestro.jdbsee.jdbcli.SettingsService
import groovy.sql.BatchingPreparedStatementWrapper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.CompileStatic;

import javax.inject.Inject

@CompileStatic
class JdbsDriverDao {

  SettingsService settingsService
  ConfigService configService

  JdbsDriverDao(){}

  @Inject
  JdbsDriverDao(
      SettingsService settingsService,
      ConfigService configService
  ){
    this.configService = configService
    this.settingsService = settingsService
  }

  void insert (
      final String name,
      final String driverClass,
      final String driverClassExpr,
      final File[] jars,
      final String[] deps) {
    withSql { Sql sql->
      sql.withTransaction {
        //persist driver info
        def keys = sql.executeInsert(
                """\
                      INSERT INTO jdbs_drivers 
                        (name, clazz, clazz_expr) 
                      VALUES 
                        (:name, :clazz, :clazz_expr)
                    """, name: name, clazz: driverClass, clazzExpr: driverClassExpr)

        def driverId = keys[0][0]

        //persist jar paths
//        sql.withBatch (10,
//            'INSERT INTO jdbs_jars (driver_id, path) VALUES (?, ?)'
//          ) { BatchingPreparedStatementWrapper stmt ->
//          jars.each { File jarFile ->
//            stmt.addBatch(driverId, jarFile.absolutePath)
//          }
//        }
        jars.each { File jarFile ->
          sql.executeInsert(driver_id: driverId, path: jarFile.absolutePath, 'INSERT INTO jdbs_jars (driver_id, path) VALUES (:driver_id, :path)')
        }

        //persist deps
//        sql.withBatch (10,
//              'INSERT INTO jdbs_deps (driver_id, gav) VALUES (?, ?)'
//        ) { BatchingPreparedStatementWrapper stmt ->
//          deps.each { String dependency ->
//            stmt.addBatch (driverId, dependency)
//          }
//        }
        deps.each { String dependency ->
          sql.executeInsert(driver_id: driverId, gav: dependency, 'INSERT INTO jdbs_deps (driver_id, gav) VALUES (:driver_id, :gav)')
        }

        return keys
      }
    }
  }

  int delete (final String... names) {
    withSql { Sql sql ->
      sql.executeUpdate(names: names,
              """\
                    DELETE FROM jdbs_drivers 
                    WHERE 
                      name IN (:names)
                  """)
    }
  }

  List<JdbsDriver> listDrivers () {
    withSql { Sql sql ->
      sql.rows('SELECT * FROM jdbs_drivers ORDER BY name ASC').collect {row->
        rowToBean(row)
      }
    }
  }

  Optional<JdbsDriverDetails> findDriverByName (final String name) {
    withSql { Sql sql ->
              List<GroovyRowResult> rows = sql.rows(name: name,
                  '''
                      SELECT 
                        drv.id AS drv_id, drv.name AS drv_name, drv.clazz AS drv_clazz, drv.clazz_expr AS drv_clazz_expr,
                        jar.id AS jar_id, jar.driver_id as jar_driver_id, jar.path AS jar_path,
                        dep.id AS dep_id, dep.driver_id as dep_driver_id, dep.gav AS dep_gav
                      FROM 
                        jdbs_drivers drv LEFT OUTER JOIN 
                        jdbs_jars jar ON (drv.id=jar.driver_id) LEFT OUTER JOIN
                        jdbs_deps dep ON (drv.id=dep.driver_id)
                      WHERE 
                        drv.name LIKE :name
                      '''
              )
      JdbsDriverDetails result
      if (rows) {
        result = new JdbsDriverDetails(jars: [], deps: [])
        rows.each {row->
          if (row.drv_id!=null) {
            result.id = row.drv_id as Long
            result.name = row.drv_name as String
            result.driverClass = row.drv_clazz as String
            result.driverClassExpr = row.drv_clazz_expr as String
          }
          if (row.jar_id!=null) {
            result.jars << new JdbsJar(
                id: row.jar_id as Long,
                driverId: row.jar_driver_id as Long,
                file: new File (row.jar_path as String)
            )
          }
          if (row.dep_id!=null) {
            result.deps << new JdbsDep(
                id: row.dep_id as Long,
                driverId: row.dep_driver_id as Long,
                gav: row.dep_gav as String
            )
          }
        }
      } else {
        result = null
      }

      return Optional.ofNullable(result)
    }
  }

  public <T> T withSql (final Closure<T> closure) {
    settingsService.withSql { Sql sql ->
      closure (sql)
    }
  }

  protected JdbsDriver rowToBean(final GroovyRowResult row) {
    row==null?
        null:
        new JdbsDriver(
            id: row.id as Long,
            name: row.name as String,
            driverClass: row.clazz as String,
            driverClassExpr: row.clazz_expr as String
    )
  }
}
