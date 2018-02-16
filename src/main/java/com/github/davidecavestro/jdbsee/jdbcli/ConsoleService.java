package com.github.davidecavestro.jdbsee.jdbcli;

import org.jdbi.v3.core.statement.Query;

import javax.inject.Inject;
import java.sql.SQLException;

public class ConsoleService {

  private AsciiTableResultSetScanner ascii;

  @Inject
  public ConsoleService (final AsciiTableResultSetScanner ascii) {
    this.ascii = ascii;
  }

  public void renderResultSet (final Query query) throws SQLException {
    query.scanResultSet (ascii).forEach (line->System.out.println (line));
  }

}
