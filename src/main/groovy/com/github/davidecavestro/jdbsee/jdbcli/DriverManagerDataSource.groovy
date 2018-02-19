package com.github.davidecavestro.jdbsee.jdbcli

import org.slf4j.LoggerFactory

import javax.sql.DataSource
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.logging.Logger

class DriverManagerDataSource implements DataSource{
    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger (DriverManagerDataSource.class)

    String url
    String username
    String password
    String driverClassName
    String catalog
    String schema

    Properties connectionProperties

//    void setDriverClassName(String driverClassName) {
//        final String driverFQN = driverClassName.trim()
//        try {
//            Class.forName(driverFQN, true, getDefaultClassLoader())
//        }
//        catch (final ClassNotFoundException ex) {
//            throw new IllegalStateException("Could not load JDBC driver class [${driverFQN}]", ex)
//        }
//        LOG.info("Loaded JDBC driver: " + driverFQN)
//    }

    protected ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        }
        catch (final Exception ex) {
            // safe to ignore
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = getClass().getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                }
                catch (Throwable ex) {
                    // safe to ignore
                }
            }
        }
        return cl;
    }

    @Override
    Connection getConnection() throws SQLException {
        return getDrivermanagerConnection(username, password)
    }

    @Override
    Connection getConnection(final String username, final String password) throws SQLException {
        return getDrivermanagerConnection(username, password)
    }


    protected Connection getDrivermanagerConnection(final String username, final String password) throws SQLException {
        Properties mergedProps = new Properties()
        Properties connProps = getConnectionProperties()
        if (connProps != null) {
            mergedProps.putAll(connProps)
        }
        if (username != null) {
            mergedProps.setProperty("user", username)
        }
        if (password != null) {
            mergedProps.setProperty("password", password)
        }

        Connection con = getDrivermanagerConnection(mergedProps)
        if (catalog != null) {
            con.catalog = catalog
        }
        if (schema != null) {
            con.schema = schema
        }
        return con
    }


    protected Connection getDrivermanagerConnection(Properties props) throws SQLException {
        return getDrivermanagerConnection(url, props)
    }

    protected Connection getDrivermanagerConnection(String url, Properties props) throws SQLException {
        return DriverManager.getConnection(url, props)
    }


    @Override
    int getLoginTimeout() throws SQLException {
        return 0
    }

    @Override
    void setLoginTimeout(int timeout) throws SQLException {
        throw new UnsupportedOperationException("setLoginTimeout")
    }

    @Override
    PrintWriter getLogWriter() {
        throw new UnsupportedOperationException("getLogWriter")
    }

    @Override
    void setLogWriter(PrintWriter pw) throws SQLException {
        throw new UnsupportedOperationException("setLogWriter")
    }


    @Override
    @SuppressWarnings("unchecked")
    <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("DataSource of type [" + getClass().getName() +
                "] cannot be unwrapped as [" + iface.getName() + "]")
    }

    @Override
    boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this)
    }

    @Override
    Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
    }

}
