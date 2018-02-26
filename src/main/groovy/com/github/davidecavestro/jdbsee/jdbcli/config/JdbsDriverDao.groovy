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
  String driversFileName = 'drivers.csv'
  String jarsFileName = 'jars.csv'
  String depsFileName = 'deps.csv'

  boolean csvBacked = true

  JdbsDriverDao(){}

  @Inject
  JdbsDriverDao(
      SettingsService settingsService,
      ConfigService configService
  ){
    this.configService = configService
    this.settingsService = settingsService
  }

//  protected File getDriversDataFile () {
//    new File (configService.getDataDir(), driversFileName)
//  }
//
//  protected File getJarsDataFile () {
//    new File (configService.getDataDir(), jarsFileName)
//  }
//
//  protected File getDepsDataFile () {
//    new File (configService.getDataDir(), depsFileName)
//  }

  protected void initTables (final Sql sql) {
    String tableAttr = csvBacked ? 'TEXT' : ''

    sql.with {
      executeUpdate """
            CREATE $tableAttr TABLE IF NOT EXISTS jdbs_drivers (
              id BIGINT IDENTITY PRIMARY KEY, 
              name VARCHAR(100) NOT NULL, 
              clazz VARCHAR(500), 
              clazz_expr VARCHAR(500)
            )
          """ as String

    execute """
            CREATE ${tableAttr} TABLE IF NOT EXISTS jdbs_jars (
              id BIGINT IDENTITY PRIMARY KEY,
              driver_id BIGINT NOT NULL, 
              path VARCHAR(1000) NOT NULL
            )
          """ as String

    execute """
            ALTER TABLE jdbs_jars 
            ADD CONSTRAINT fk_jdbs_jars_driver 
            FOREIGN KEY (driver_id)
            REFERENCES jdbs_drivers (id)
            ON DELETE CASCADE
          """ as String

    execute """
            CREATE ${tableAttr} TABLE IF NOT EXISTS jdbs_deps (
              id BIGINT IDENTITY PRIMARY KEY, 
              driver_id BIGINT NOT NULL, 
              gav VARCHAR(200) NOT NULL
            )
          """ as String

    execute """
              ALTER TABLE jdbs_deps 
              ADD CONSTRAINT fk_jdbs_deps_driver 
              FOREIGN KEY (driver_id)
              REFERENCES jdbs_drivers (id)
              ON DELETE CASCADE
            """ as String

    if (csvBacked) {
      executeUpdate "SET TABLE jdbs_drivers SOURCE '$driversFileName'" as String
      executeUpdate "SET TABLE jdbs_jars SOURCE '$jarsFileName'" as String
      executeUpdate "SET TABLE jdbs_deps SOURCE '$depsFileName'" as String
    }

    }

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
      sql.rows('SELECT * FROM jdbs_drivers').collect {row->
        rowToBean(row)
      }
    }
  }

  Optional<JdbsDriver> findDriver (final String name) {
    withSql { Sql sql ->
      Optional.ofNullable(
          rowToBean(
              sql.firstRow(name: name,
                  'SELECT * FROM jdbs_drivers WHERE name LIKE :name'
              )
          )
      )
    }
  }

  public <T> T withSql (final Closure<T> closure) {
    settingsService.withSql { Sql sql ->
      initTables (sql)//grant tables exist
      closure (sql)
    }
  }

  protected JdbsDriver rowToBean(final GroovyRowResult row) {
    row==null?
        null:
        new JdbsDriver(
            id: row.id as Long,
            name: row.name as String,
            clazz: row.clazz as String,
            clazzExpr: row.clazz_expr as String
    )
  }
}
