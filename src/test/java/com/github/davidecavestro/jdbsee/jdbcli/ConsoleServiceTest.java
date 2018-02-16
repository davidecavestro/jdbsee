package com.github.davidecavestro.jdbsee.jdbcli;

import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.Stream;

public class ConsoleServiceTest {

//  private ConsoleService consoleService;

  @Before
  public void setUp () throws Exception {
//    consoleService = new ConsoleService ();
  }

  @Test
  public void renderResultSet () {
    /*FIXME add tests!!!
    final Jdbi jdbi = Jdbi.create ("jdbc:h2:mem:test");
    final Stream<String> listStream = jdbi.withHandle (handle -> {
      handle.execute ("create table contacts (id int primary key, name varchar(100))");
      handle.execute ("insert into contacts (id, name) values (?, ?)", 1, "Alice");
      handle.execute ("insert into contacts (id, name) values (?, ?)", 2, "Bob");
      return handle.select ("SELECT * FROM contacts").scanResultSet (new AsciiTableResultSetScanner ());
    });
    listStream.forEach (line -> System.out.println (line));
    */
  }
}
