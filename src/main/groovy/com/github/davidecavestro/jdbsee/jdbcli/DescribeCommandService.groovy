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

  void run (final DescribeViewsCommand cmd) {
    describeTables (cmd, cmd.matches, 'VIEW')
  }

  void run (final DescribeTablesCommand cmd) {
    describeTables (cmd, cmd.matches, 'TABLE')
  }

  void describeTables (final AbstractDbCommand cmd, final String matches, final String... types) {
    Closure<Void> callback = { dataSource ->
      Connection connection = dataSource.getConnection()
      try {

        final Table table = connection.metaData.getTables(null, null, matches, types).with {
          List<Map<String,Object>> rows = []
          def header = [key: 'header', catalog:'CATALOG', schema:'SCHEMA', name:'NAME']
          rows << header
          def count = 0
          while (next()) {
            rows << [
                key: count++ as String,
                catalog: getObject(1),
                schema: getObject(2),
                name: getObject(3)
            ]
          }
          final Table table = ArrayTable.create(rows.collect {it.key}, header.keySet()-'key')
          if (rows) {
            rows.each {row->
              row.each {k,v->
                if (k!='key') {
                  table.put(row.key, k, v?:'-' as String)
                }
              }
            }
          }
          return table
        }

        consoleService.renderTable(table)
      } finally {
        connection.close()
      }
    }

    doRun (cmd, callback)
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
