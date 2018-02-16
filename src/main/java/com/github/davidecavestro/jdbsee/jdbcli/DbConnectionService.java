package com.github.davidecavestro.jdbsee.jdbcli;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class DbConnectionService {

  @Inject
  public DbConnectionService() {}

  public <T> T withConnection (final String connName, final CheckedFunction<Connection, T, SQLException> function) throws SQLException {
    try (final Connection connection = getConnection (connName)) {
      return function.apply (connection);
    }
  }

  protected Connection getConnection (final String connName) throws SQLException {
//    final SimpleDriverDataSource dataSource = new SimpleDriverDataSource ();
//
//    //TODO config
//    final Class<? extends Driver> driverClass = null;
//    dataSource.setDriverClass (driverClass);
//
//    return dataSource.getConnection ();
    return null;//FIXME
  }

  public DatabaseMetaData getDatabaseMetadata (final String connName) throws SQLException {
    return withConnection (connName, connection -> connection.getMetaData ());
  }

//  public List<String> getTables (final String connName) throws SQLException {
//    getDatabaseMetadata (connName).getTables ();
//    return withConnection (connName, connection -> connection.getMetaData ());
//  }
}
