package com.github.davidecavestro.jdbsee.jdbcli

import groovy.sql.Sql
import groovy.transform.CompileStatic

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
    Sql sql = Sql.newInstance(dbUrl, dbUser, dbPassword, dbDriver)
    try {
      return closure(sql)
    } finally {
      sql.close()
    }
  }

  protected File getDbFile () {
    new File(configService.getDataDir(), dbFileName)
  }

  protected String getDbUrl () {
    "jdbc:hsqldb:file:${dbFile.absolutePath}"
  }
}
