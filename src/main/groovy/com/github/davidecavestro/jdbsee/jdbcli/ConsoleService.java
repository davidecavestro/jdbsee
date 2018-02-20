package com.github.davidecavestro.jdbsee.jdbcli;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.jdbi.v3.core.result.NoResultsException;
import org.jdbi.v3.core.result.ResultSetScanner;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.function.Supplier;

public class ConsoleService {

  private final static Logger LOG = LoggerFactory.getLogger (ConsoleService.class);

  private AsciiTableResultSetScanner ascii;

  @Inject
  public ConsoleService (final AsciiTableResultSetScanner ascii) {
    this.ascii = ascii;
  }

  public void renderResultSet (final Query query, final OutputType outputType, final File outputFile) throws IOException {
    final PrintWriter outWriter = new PrintWriter (outputFile!=null?new FileWriter (outputFile):new OutputStreamWriter (System.out));

    switch (outputType) {
      case CSV:
        query.scanResultSet (new CsvResultSetScanner (outWriter));
        break;
      case JSON:
      case JSON_PRETTY:
        query.scanResultSet (new ResultSetScanner<Void> () {
          @Override
          public Void scanResultSet (final Supplier<ResultSet> resultSetSupplier, final StatementContext ctx) throws SQLException {
            final ObjectMapper mapper = new ObjectMapper ();
            try {
              final ResultSet resultSet = resultSetSupplier.get ();

              final ResultSetMetaData metaData = resultSet.getMetaData ();
              final int columnCount = metaData.getColumnCount ();

              final Map<Integer, String> colNames = new LinkedHashMap<> ();
              for (int colPos = 1; colPos <= columnCount; colPos++) {
                final String colName = metaData.getColumnLabel (colPos);
                colNames.put (colPos, colName);
              }

              try {
                try (final SequenceWriter writer = mapper
                        .writer (outputType==OutputType.JSON?null:new DefaultPrettyPrinter ())
                        .writeValuesAsArray (outWriter)) {

                  final Map row = new LinkedHashMap<> (columnCount);
                  while (resultSet.next ()) {
                    row.clear ();
                    for (int colPos = 1; colPos <= columnCount; colPos++) {
                      row.put (colNames.get (colPos), resultSet.getObject (colPos));
                    }
                    writer.write (row);
                    writer.flush ();
                  }
                }
              } catch (IOException e) {
                throw new RuntimeException (e);
              }

            } catch (final NoResultsException e) {
              if (ctx.getStatement ().getUpdateCount ()>=0) {
                //it was some DML/DDL
                LOG.warn ("No results");
              } else {
                throw e;
              }
            }
            return null;
          }
        });
        break;
      default:
        query.scanResultSet (ascii).forEach (line -> outWriter.println (line));
        break;
    }
  }

  private static class CsvResultSetScanner implements ResultSetScanner<Void> {
    private final PrintWriter outWriter;

    public CsvResultSetScanner (final PrintWriter outWriter) {
      this.outWriter = outWriter;
    }

    @Override
    public Void scanResultSet (final Supplier<ResultSet> supplier, final StatementContext ctx) throws SQLException {
      final CsvMapper mapper = new CsvMapper();
      mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);

      final ResultSet resultSet = supplier.get ();

      final ResultSetMetaData metaData = resultSet.getMetaData ();
      final int columnCount = metaData.getColumnCount ();
      final Collection<String> headers = new ArrayList<> ();

      final Map<Integer,String> colNames=new LinkedHashMap<> ();
      final CsvSchema.Builder builder = CsvSchema.builder ();
      for (int colPos = 1; colPos <= columnCount; colPos++) {
        final String colName = metaData.getColumnLabel (colPos);
        colNames.put (colPos, colName);
        headers.add (colName);
        final int colType = metaData.getColumnType (colPos);
        final CsvSchema.ColumnType outColType;
        switch (colType) {
          case Types.BIGINT:
          case Types.DOUBLE:
          case Types.DECIMAL:
          case Types.NUMERIC:
          case Types.REAL:
          case Types.SMALLINT:
          case Types.TINYINT:
            outColType = CsvSchema.ColumnType.NUMBER_OR_STRING;
            builder.addNumberColumn (colName);
            break;
          case Types.BOOLEAN:
          case Types.BIT:
            outColType = CsvSchema.ColumnType.BOOLEAN;
            builder.addBooleanColumn (colName);
            break;
          default:
            outColType = CsvSchema.ColumnType.STRING_OR_LITERAL;
            builder.addColumn (colName);
        }
//              builder.addColumn (colName, outColType);
      }
      try {
        final SequenceWriter writer = mapper.writer (
                builder.build ()
                .withLineSeparator ("\n")
                .withQuoteChar ('"')
                .withoutHeader()
                .withEscapeChar ('"')
                .withColumnSeparator (';')
        ).writeValues (outWriter).write (headers);

        final Map row = new LinkedHashMap<> (columnCount);
        while (resultSet.next ()) {
          row.clear ();
          for (int colPos = 1; colPos <= columnCount; colPos++) {
            row.put (colNames.get (colPos), resultSet.getObject (colPos));
          }
          writer.write (row);
        }
      } catch (final IOException e) {
        throw new RuntimeException (e);
      }
      return null;
    }
  }
}
