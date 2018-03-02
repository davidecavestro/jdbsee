package com.github.davidecavestro.jdbsee.jdbcli.config

import com.github.davidecavestro.jdbsee.jdbcli.ConfigService
import com.github.davidecavestro.jdbsee.jdbcli.SettingsService
import groovy.sql.BatchingPreparedStatementWrapper
import groovy.sql.GroovyResultSet
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

  long insert (
      final String name,
      final String driverClass,
      final String driverClassExpr,
      final List<File> jars,
      final List<String> deps) {
    Long driverId
    withSql { Sql sql->
      sql.cacheConnection {
        sql.withTransaction {
          //persist driver info
          driverId = sql.executeInsert(
              """\
                        INSERT INTO jdbs_drivers 
                          (name, clazz, clazz_expr) 
                        VALUES 
                          (:name, :clazz, :clazz_expr)
                      """, name: name, clazz: driverClass, clazzExpr: driverClassExpr).flatten().first() as Long

          //persist jar paths
  //        sql.withBatch (10,
  //            'INSERT INTO jdbs_jars (driver_id, path) VALUES (?, ?)'
  //          ) { BatchingPreparedStatementWrapper stmt ->
  //          jars.each { File jarFile ->
  //            stmt.addBatch(driverId, jarFile.absolutePath)
  //          }
  //        }
          jars?.each { File jarFile ->
            addJar sql, driverId, jarFile
          }

          //persist deps
  //        sql.withBatch (10,
  //              'INSERT INTO jdbs_deps (driver_id, gav) VALUES (?, ?)'
  //        ) { BatchingPreparedStatementWrapper stmt ->
  //          deps.each { String dependency ->
  //            stmt.addBatch (driverId, dependency)
  //          }
  //        }
          deps?.each { String dependency ->
            addDependency sql, driverId, dependency
          }
        }
      }
    }

    return driverId
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

  int delete (final Long... ids) {
    withSql { Sql sql ->
      sql.executeUpdate(ids: ids,
          """\
                    DELETE FROM jdbs_drivers 
                    WHERE 
                      id IN (:ids)
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
              GroovyRowResult driverRow = sql.firstRow(name: name,
                  '''
                      SELECT 
                        drv.id AS drv_id, drv.name AS drv_name, drv.clazz AS drv_clazz, drv.clazz_expr AS drv_clazz_expr,
                        jar.id AS jar_id, jar.driver_id AS jar_driver_id, jar.path AS jar_path,
                        NULL AS dep_id, NULL AS dep_driver_id, NULL AS dep_gav
                      FROM 
                        jdbs_drivers drv LEFT OUTER JOIN 
                        jdbs_jars jar ON (drv.id=jar.driver_id)
                      WHERE 
                        drv.name = :name
                  ''')

              JdbsDriverDetails result
              if (driverRow) {
                Long driverId = driverRow.drv_id as Long

                result = new JdbsDriverDetails(
                    id: driverId,
                    name: driverRow.drv_name as String,
                    driverClass: driverRow.drv_clazz as String,
                    driverClassExpr: driverRow.drv_clazz_expr as String,
                    jars: [], deps: []
                )

                result.with {
                  sql.eachRow(driverId: driverId, '''
                        SELECT 
                          id, driver_id, path
                        FROM 
                          jdbs_jars
                        WHERE 
                          driver_id = :driverId
                        ORDER BY id
                        '''
                  ) { GroovyResultSet row ->
                    jars << new JdbsJar(
                        id: row['id'] as Long,
                        driverId: row['driver_id'] as Long,
                        file: new File(row['path'] as String)
                    )
                  }

                  sql.eachRow(driverId: driverId, '''
                        SELECT 
                          id, driver_id, gav
                        FROM 
                          jdbs_deps
                        WHERE 
                          driver_id = :driverId
                        ORDER BY id
                        '''
                  ) { GroovyResultSet row ->
                      deps << new JdbsDep(
                          id: row['id'] as Long,
                          driverId: row['driver_id'] as Long,
                          gav: row['gav'] as String
                      )
                  }
                }
              } else {
                result = null
              }


      return Optional.ofNullable(result)
    }
  }

  long addJar (final Sql sql, final Long driverId, final File jarFile) {
    sql.executeInsert(driver_id: driverId, path: jarFile.absolutePath, 'INSERT INTO jdbs_jars (driver_id, path) VALUES (:driver_id, :path)').flatten().first() as Long
  }

  List<Long> addJar (final Long driverId, final File... jarFiles) {
    withSql {Sql sql->
      //FIXME switch to batches
      jarFiles.collect {File jarFile->
        addJar sql, driverId, jarFile
      }
    }
  }

  int removeJar (final Long... jarIds) {
    withSql {Sql sql->
      sql.executeUpdate(jarIds: jarIds, 'DELETE FROM jdbs_jars WHERE id IN (:jarIds)')
    }
  }

  long addDependency (final Sql sql, final Long driverId, final String dependency) {
    sql.executeInsert(driver_id: driverId, gav: dependency, 'INSERT INTO jdbs_deps (driver_id, gav) VALUES (:driver_id, :gav)').flatten().first() as Long
  }

  List<Long> addDependency (final Long driverId, final String... dependencies) {
    withSql {Sql sql->
      //FIXME switch to batches
      dependencies.collect { String dependency ->
        addDependency sql, driverId, dependency
      }
    }
  }

  int removeDependency (final Long... dependencyIds) {
    withSql {Sql sql->
      sql.executeUpdate(dependencyIds: dependencyIds, 'DELETE FROM jdbs_deps WHERE id IN (:dependencyIds)')
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
