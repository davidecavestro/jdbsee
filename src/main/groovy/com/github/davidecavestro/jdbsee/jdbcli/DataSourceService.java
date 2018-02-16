package com.github.davidecavestro.jdbsee.jdbcli;

//import org.apache.commons.dbcp2.BasicDataSource;

import javax.inject.Inject;
import javax.sql.DataSource;

public class DataSourceService {
  @Inject
  public DataSourceService(){}

//  public DataSource getDataSource (final String driverClassName, final String url, final String username, final String password) {
//    final BasicDataSource result = new BasicDataSource ();
//    result.setDriverClassName (driverClassName);
//    result.setUrl (url);
//    result.setUsername (username);
//    result.setPassword (password);
//    return result;
//  }

  public DataSource getDataSource (final String dataSourceName, final String username, final String password) {
    //FIXME
    return null;
  }
}
