package com.github.davidecavestro.jdbsee.jdbcli

import groovy.sql.Sql
import groovy.transform.CompileStatic
import org.flywaydb.core.Flyway

import javax.inject.Inject

@CompileStatic
class SettingsService {

  ConfigService configService

  String dbFileName = 'hsql.csv'
  String dbUser = 'sa'
  String dbPassword = ''
  String dbDriver = 'org.hsqldb.jdbc.JDBCDriver'

  SettingsService(){}

  @Inject
  SettingsService(ConfigService configService){
    this.configService = configService
  }

  public <T> T withSql (Closure<T> closure) {
    // Create the Flyway instance
    Flyway flyway = new Flyway()

    // Point it to the database
    flyway.setDataSource(dbUrl, dbUser, dbPassword)

    // Start the migration
    flyway.migrate()

    Sql sql = Sql.newInstance(dbUrl, dbUser, dbPassword, dbDriver)
    try {
      return closure(sql)
    } finally {
      sql.close()
    }
  }

  protected File getDbFile () {
    new File(configService.getUserDataDir(), dbFileName)
  }

  protected String getDbUrl () {
    "jdbc:hsqldb:file:${dbFile.absolutePath}"
  }
}
