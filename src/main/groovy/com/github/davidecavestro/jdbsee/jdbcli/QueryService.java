package com.github.davidecavestro.jdbsee.jdbcli;

import com.google.common.collect.Iterables;
import org.jdbi.v3.core.ConnectionFactory;
import org.jdbi.v3.core.Jdbi;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

public class QueryService {

  @Inject
  public QueryService(){}

  public <X extends Exception> void execute (
      final String url,
      final String username,
      final String password,
      final QueryCallback<Void, X> callback,
      final String... sql) throws X {
    execute (Jdbi.create (url, username, password), callback, sql);
  }

  public <X extends Exception> void execute (
      final ConnectionFactory connectionFactory,
      final QueryCallback<Void, X> callback,
      final String... sql) throws X {
    execute (Jdbi.create (connectionFactory), callback, sql);
  }

  public <X extends Exception> void execute (
      final DataSource dataSource,
      final QueryCallback<Void, X> callback,
      final String... sql) throws X {
    execute (Jdbi.create (dataSource), callback, sql);
  }

  public <X extends Exception> void execute (
      final Jdbi jdbi,
      final QueryCallback<Void, X> callback,
      final String... sql
      ) throws X {
    jdbi.withHandle (handle -> {
      final List<String> queries = Arrays.asList (sql);
      final String last = Iterables.getLast (queries);
      for (final String command : Iterables.filter (queries, it->it!=last)) {
        handle.execute (command);
      }
      callback.withQuery (handle.select (last));
      return null;
    });
  }
}
