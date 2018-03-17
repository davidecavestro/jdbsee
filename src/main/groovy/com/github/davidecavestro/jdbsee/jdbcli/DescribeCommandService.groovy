package com.github.davidecavestro.jdbsee.jdbcli

import com.google.common.collect.ArrayTable
import com.google.common.collect.Table
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.sql.ResultSetMetaData

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
                switch (it) {
                  case ResultSet:
                    toString (it)
                    break
                  case Iterable:
                    it.collect {it as String}
                  default:
                    it as String
                }
              }
              table.put(k, 'value', valueString)
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

  String toString (final ResultSet resultSet) {
    toList (resultSet) as String
  }

  List<Map<String,Object>> toList (final ResultSet resultSet) {
    resultSet.with {
      metaData.with {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>> ()
        while (next ()) {
          int colCount = columnCount
          Map<String, Object> row = new HashMap<String, Object> (colCount)
          for(int i = 1; i <= colCount; ++i){
            row[getColumnName(i)] = getObject(i)
          }
          rows << row
        }
        return rows
      }
    }
  }
}
