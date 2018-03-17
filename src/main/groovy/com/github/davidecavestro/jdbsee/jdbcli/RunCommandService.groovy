package com.github.davidecavestro.jdbsee.jdbcli

import org.jdbi.v3.core.statement.Query
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject
import java.sql.SQLException

//@CompileStatic
class RunCommandService extends AbstractDbCommandService{

  private final static Logger LOG = LoggerFactory.getLogger (RunCommandService.class)

  @Inject
  public ConsoleService consoleService
  @Inject
  public QueryService queryService

  @Inject
  public RunCommandService (){}

  void run (final RunCommand runCommand) {
    runCommand.with {
      String[] sqlArray = getSqlArray(runCommand)

      Closure<Void> queryCall = {dataSource->
        final QueryCallback<Void, Exception> callback = { Query query ->
          try {
            consoleService.renderResultSet(query, outputType, outputFile)
          } catch (final SQLException e) {
            throw new RuntimeException(e)
          }
          return null
        }

        queryService.execute(dataSource, callback, sqlArray)
      }

      doRun (runCommand, queryCall)
    }
  }

  protected String[] getSqlArray (final RunCommand runCommand) {
    runCommand.with {
      final List<String> sql = new ArrayList<>()
      if (execute) {
        sql.addAll execute
      }
      if (sqlText) {
        sql.add sqlText
      }

      return sql.toArray(new String[sql.size()])
    }
  }
}
