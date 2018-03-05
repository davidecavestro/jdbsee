package com.github.davidecavestro.jdbsee.jdbcli.config

import com.github.davidecavestro.jdbsee.jdbcli.ConfigService
import com.github.davidecavestro.jdbsee.jdbcli.SettingsService
import groovy.sql.GroovyResultSet
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.CompileStatic

import javax.inject.Inject

@CompileStatic
class JdbsAliasDao {

  SettingsService settingsService
  ConfigService configService
  JdbsDriverDao driverDao

  JdbsAliasDao(){}

  @Inject
  JdbsAliasDao(
      SettingsService settingsService,
      ConfigService configService,
      JdbsDriverDao driverDao
  ){
    this.configService = configService
    this.settingsService = settingsService
    this.driverDao = driverDao
  }

  long insert (
      final String driver,
      final String name,
      final String url,
      final String username,
      final String password
      ) {
    Long aliasId
    withSql { Sql sql->
      sql.cacheConnection {
        sql.withTransaction {
          def driverId
          try {
            driverId = Long.valueOf(driver)
          } catch (NumberFormatException e){
            driverId = driverDao.findDriverByName(driver).get().id
          }
          //persist alias info
          aliasId = sql.executeInsert(
              name: name, driverId: driverId, url: url, username: username, password: password,
              """\
                        INSERT INTO jdbs_aliases 
                          (name, driver_id, url, username, password) 
                        VALUES 
                          (:name, :driverId, :url, :username, :password)
                      """).flatten().first() as Long
        }
      }
    }

    return aliasId
  }

  int delete (final String... names) {
    withSql { Sql sql ->
      sql.executeUpdate(names: names,
              """\
                    DELETE FROM jdbs_aliases 
                    WHERE 
                      name IN (${names.collect{"'$it'"}.join(',')})
                  """ as String)
    }
  }

  int delete (final Long... ids) {
    withSql { Sql sql ->
      sql.executeUpdate(
          """\
                    DELETE FROM jdbs_aliases 
                    WHERE 
                      id IN (${ids.join ', '})
                  """ as String)
    }
  }

  List<JdbsAlias> listAliases () {
    withSql { Sql sql ->
      sql.rows("""
          SELECT 
            als.id, als.name, als.url, als.username, als.password,
            drv.id AS driver_id, drv.name AS driver_name, 
            drv.clazz AS driver_clazz, drv.clazz_expr AS driver_expr
          FROM 
            jdbs_aliases als INNER JOIN jdbs_drivers drv ON (als.driver_id=drv.id)
          ORDER BY name ASC"""
      ).collect {row->
        rowToBean(row)
      }
    }
  }

  Optional<JdbsAliasDetails> findAliasByName (final String name) {
    getAliasDetails {Sql sql-> sql.firstRow(name: name,
        'SELECT * FROM jdbs_aliases WHERE name = :name')
    }
  }

  Optional<JdbsAliasDetails> findAliasById (final Long driverId) {
    getAliasDetails {Sql sql-> sql.firstRow(driverId: driverId,
        'SELECT * FROM jdbs_aliases WHERE id = :driverId')
    }
  }

  Optional<JdbsAliasDetails> getAliasDetails (final Closure<GroovyRowResult> aliasRowClosure) {
    withSql { Sql sql ->
              GroovyRowResult aliasRow = aliasRowClosure(sql)

              JdbsAliasDetails result
              if (aliasRow) {
                Long aliasId = aliasRow.id as Long

                result = new JdbsAliasDetails(
                    id: aliasId,
                    driverId: aliasRow.driver_id as Long,
                    name: aliasRow.name as String,
                    url: aliasRow.url as String,
                    username: aliasRow.username as String,
                    password: aliasRow.password as String,
                    driverDetails: driverDao.findDriverById (aliasRow.driver_id as Long).get()
                )
                result.driver = result.driverDetails
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

  protected JdbsAlias rowToBean(final GroovyRowResult row) {
    row==null?
        null:
        new JdbsAlias(
            id: row.id as Long,
            name: row.name as String,
            url: row.url as String,
            username: row.username as String,
            password: row.password as String,
            driverId: row.driver_id as Long,
            driver: new JdbsDriver(
                id: row.driver_id as Long,
                name: row.driver_name as String,
                driverClass: row.driver_clazz as String,
                driverClassExpr: row.driver_expr as String
            )
    )
  }
}
