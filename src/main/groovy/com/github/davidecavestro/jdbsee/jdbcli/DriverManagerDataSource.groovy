package com.github.davidecavestro.jdbsee.jdbcli;

import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection
import java.sql.DriverManager;
import java.sql.SQLException
import java.util.logging.Logger;

public class DriverManagerDataSource implements DataSource{
    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger (DriverManagerDataSource.class);

    String url;

    String username;

    String password;

    String catalog;

    String schema;

    Properties connectionProperties;

    @Override
    public Connection getConnection() throws SQLException {
        return getDrivermanagerConnection(username, password);
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return getDrivermanagerConnection(username, password);
    }


    protected Connection getDrivermanagerConnection(final String username, final String password) throws SQLException {
        Properties mergedProps = new Properties();
        Properties connProps = getConnectionProperties();
        if (connProps != null) {
            mergedProps.putAll(connProps);
        }
        if (username != null) {
            mergedProps.setProperty("user", username);
        }
        if (password != null) {
            mergedProps.setProperty("password", password);
        }

        Connection con = getDrivermanagerConnection(mergedProps);
        if (catalog != null) {
            con.catalog = catalog
        }
        if (schema != null) {
            con.schema = schema
        }
        return con;
    }


    protected Connection getDrivermanagerConnection(Properties props) throws SQLException {
        return getDrivermanagerConnection(url, props);
    }

    protected Connection getDrivermanagerConnection(String url, Properties props) throws SQLException {
        return DriverManager.getConnection(url, props);
    }


    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setLoginTimeout(int timeout) throws SQLException {
        throw new UnsupportedOperationException("setLoginTimeout");
    }

    @Override
    public PrintWriter getLogWriter() {
        throw new UnsupportedOperationException("getLogWriter");
    }

    @Override
    public void setLogWriter(PrintWriter pw) throws SQLException {
        throw new UnsupportedOperationException("setLogWriter");
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("DataSource of type [" + getClass().getName() +
                "] cannot be unwrapped as [" + iface.getName() + "]");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }

    @Override
    public Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

}
