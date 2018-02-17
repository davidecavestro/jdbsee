package com.github.davidecavestro.jdbsee.jdbcli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Streams;
import de.vandermeer.asciitable.AsciiTable;
import org.jdbi.v3.core.result.ResultSetScanner;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.StatementContext;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.collect.Iterators.concat;
import static com.google.common.collect.Iterators.transform;

public class ConsoleService {

  private AsciiTableResultSetScanner ascii;

  @Inject
  public ConsoleService (final AsciiTableResultSetScanner ascii) {
    this.ascii = ascii;
  }

  public void renderResultSet (final Query query, final OutputType outputType) throws SQLException {
    switch (outputType) {
      case CSV:
        query.scanResultSet (new CsvResultSetScanner ());
        break;
      case JSON:
      case JSON_PRETTY:
        query.scanResultSet (new ResultSetScanner<Void> () {
          @Override
          public Void scanResultSet (final Supplier<ResultSet> resultSetSupplier, final StatementContext ctx) throws SQLException {
            final ObjectMapper mapper = new ObjectMapper ();
            final ResultSet resultSet = resultSetSupplier.get ();

            final ResultSetMetaData metaData = resultSet.getMetaData ();
            final int columnCount = metaData.getColumnCount ();

            final Map<Integer,String> colNames=new LinkedHashMap<> ();
            for (int colPos = 1; colPos <= columnCount; colPos++) {
              final String colName = metaData.getColumnLabel (colPos);
              colNames.put (colPos, colName);
            }

            try {
              try (final SequenceWriter writer = mapper.writerWithDefaultPrettyPrinter ().writeValuesAsArray (System.out)) {

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

            return null;
          }
        });
        break;
      default:
        query.scanResultSet (ascii).forEach (line -> System.out.println (line));
        break;
    }
  }

  private static class CsvResultSetScanner implements ResultSetScanner<Void> {
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
        ).writeValues (System.out).write (headers);

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
