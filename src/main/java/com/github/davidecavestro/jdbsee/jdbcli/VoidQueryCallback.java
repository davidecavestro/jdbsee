package com.github.davidecavestro.jdbsee.jdbcli;

import org.jdbi.v3.core.statement.Query;

public abstract class VoidQueryCallback<X extends Exception> implements QueryCallback<Void, X> {
  @Override
  public Void withQuery (final Query query) throws X {
    return doWithQuery (query);
  }

  protected abstract Void doWithQuery (final Query query);
}
