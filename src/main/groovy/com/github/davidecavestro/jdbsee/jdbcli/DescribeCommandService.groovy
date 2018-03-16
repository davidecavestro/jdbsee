package com.github.davidecavestro.jdbsee.jdbcli

import com.google.common.collect.ArrayTable
import com.google.common.collect.Table
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject
import java.sql.Connection
import java.sql.DatabaseMetaData

import static DescribeCommand.*

//@CompileStatic
class DescribeCommandService extends AbstractDbCommandService{

  private final static Logger LOG = LoggerFactory.getLogger (DescribeCommandService.class)

  @Inject
  public ConsoleService consoleService
  @Inject
  public QueryService queryService

  @Inject
  public DescribeCommandService(){}

  void run (final DescribeDriverCommand cmd) {
    cmd.with {
      Closure<Void> callback = { dataSource ->
        Connection connection = dataSource.getConnection()
        try {
          DatabaseMetaData dbMeta = connection.metaData
          Table table = ArrayTable.create(dbMeta.properties.keySet(), ['key', 'value'])
          dbMeta.properties.with { props ->
            props.each { k, v ->
              table.put(k, 'key', k)
              def valueString = v.with {
                it instanceof Iterable?it.collect {it as String}:it as String
              }
              table.put(k, 'value', v as String)
            }
          }
          consoleService.renderTable(table)
        } finally {
          connection.close()
        }
      }

      doRun (cmd, callback)
    }
  }

}
