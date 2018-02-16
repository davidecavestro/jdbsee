package com.github.davidecavestro.jdbsee.jdbcli;

import com.github.davidmoten.guavamini.Iterators;
import com.github.davidmoten.guavamini.Lists;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import dagger.Component;
import org.jdbi.v3.core.ConnectionFactory;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

public class QueryService {

  private final DataSourceService dataSourceService;

  @Inject
  public QueryService(final DataSourceService dataSourceService){
    this.dataSourceService = dataSourceService;
  }

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
//      handle.execute ("create table contacts (id int primary key, name varchar(100))");
//      handle.execute ("insert into contacts (id, name) values (?, ?)", 1, "Alice");
//      handle.execute ("insert into contacts (id, name) values (?, ?)", 2, "Bob");
      final List<String> queries = Arrays.asList (sql);
      final String last = Iterables.getLast (queries);
      for (final String command : Iterables.filter (queries, it->it!=last)) {
        handle.execute (command);
      }
      callback.withQuery (handle.select (last));
      return null;
    });
//    listStream.forEach (line -> System.out.println (line));

  }
}
