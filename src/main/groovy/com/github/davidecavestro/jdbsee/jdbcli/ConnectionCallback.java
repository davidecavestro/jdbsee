package com.github.davidecavestro.jdbsee.jdbcli;

import java.sql.Connection;

/**
 * Callback that accepts a Connection to be accessed only within the scope the callback..
 */
@FunctionalInterface
public interface ConnectionCallback<T, X extends Exception>
{
  /**
   * Will be invoked with a connection to be used.
   *
   * @param connection connection to be used only within scope of this callback
   * @return The return value of the callback
   * @throws X optional exception thrown by the callback
   */
  T withConnection(Connection connection) throws X;
}
