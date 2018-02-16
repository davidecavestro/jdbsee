package com.github.davidecavestro.jdbsee.jdbcli;

import org.jdbi.v3.core.ConnectionFactory;
import org.jdbi.v3.core.statement.Query;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static picocli.CommandLine.*;

@Command(name = "run")
public class RunCommand implements Runnable {

  @Inject
  DataSourceService dataSourceService;
  @Inject
  ConsoleService consoleService;
  @Inject
  QueryService queryService;

//  @Parameters(arity = "0..1", paramLabel = "DRIVER_CLASS_NAME", description = "The JDBC driver class name.")
//  private String driverClassName;

  @Parameters(index = "0", arity = "1..1", paramLabel = "URL", description = "The JDBC url.")
  private String url;

  @Parameters(index = "1..*", arity = "1..*", paramLabel = "QUERY", description = "The SQL to run.")
  private String sqlText;

  @Option(names = {"-u", "--user"}, description = "The username")
  private String username = "";

  @Option(names = {"-p", "--password"}, description = "The password")
  private String password = "";

//  @Parameters(arity = "0..1", paramLabel = "DATA_SOURCE", description = "The name of the data source.")
//  private String dataSourceName;

  @Option(names = {"-f", "--file"}, description = "File containing the query to run.")
  private File sqlFile;

  @Option(names = {"-x", "--execute"}, description = "Additional SQL commands to be executed before the specified QUERY")
  private List<String> execute;

  @Inject
  public RunCommand (
//      final DataSourceService dataSourceService,
//      final ConsoleService consoleService,
//      final QueryService queryService
  ){
//    this.dataSourceService = dataSourceService;
//    this.consoleService = consoleService;
//    this.queryService = queryService;
  }

  @Override
  public void run () {

//    final ConnectionFactory connectionFactory;
//    final DataSource dataSource;
//
//    if (dataSourceName!=null) {
//      dataSource = dataSourceService.getDataSource (dataSourceName, username, password);
//    } else if (url!=null) {
//      queryService.execute (driverClassName, url, username, password, sqlText);
//    } else {
//      throw new IllegalArgumentException ("One between DATA_SOURCE_NAME and URL args must be passed");
//    }

    if (sqlText!=null) {
      final List<String> sql = new ArrayList<> ();
      if (Objects.nonNull (execute)) {
        sql.addAll (execute);
      }
      sql.add (sqlText);

      queryService.execute (url, username, password, (Query query) -> {
        System.err.println ("url:"+url);
        System.err.println ("sql:"+sqlText);
        try {
          consoleService.renderResultSet (query);
        } catch (final SQLException e) {
          throw new RuntimeException (e);
        }
        return null;
      }, sql.toArray (new String[sql.size ()]));
    } else {
      //FIXME
    }
//      } else {//FIXME
//        consoleService.renderResultSet (queryService.execute (dataSource, sqlFile, callback));
  }
}
