package com.github.davidecavestro.jdbsee.jdbcli.config;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbsDriverRowMapper implements RowMapper<JdbsDriver> {
  @Override
  public JdbsDriver map (final ResultSet rs, final StatementContext ctx) throws SQLException {
    return null;//TODO implement
  }

  @Override
  public RowMapper specialize (final ResultSet rs, final StatementContext ctx) throws SQLException {
    return null;//TODO implement
  }
}
