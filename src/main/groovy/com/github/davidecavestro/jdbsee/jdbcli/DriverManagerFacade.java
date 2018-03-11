package com.github.davidecavestro.jdbsee.jdbcli;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DriverManagerFacade {
    @Inject
    public DriverManagerFacade () {
    }

    public Connection getConnection(String url, Properties props) throws SQLException {
        return DriverManager.getConnection(url, props);
    }
    public Connection getConnection(Driver driver, String url, Properties props) throws SQLException {
        return driver.connect (url, props);
    }
}
