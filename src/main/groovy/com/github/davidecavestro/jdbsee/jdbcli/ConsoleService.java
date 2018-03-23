package com.github.davidecavestro.jdbsee.jdbcli;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.base.Function;
import com.google.common.collect.Streams;
import com.google.common.collect.Table;
import de.vandermeer.asciitable.AsciiTable;
import org.apache.commons.io.output.WriterOutputStream;
import org.jdbi.v3.core.result.NoResultsException;
import org.jdbi.v3.core.result.ResultSetScanner;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.*;
import java.nio.charset.Charset;
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

  private final static Logger LOG = LoggerFactory.getLogger (ConsoleService.class);

  private AsciiTableResultSetScanner ascii;
  private PrintWriter sysOut = new PrintWriter(System.out);
  private PrintWriter sysErr = new PrintWriter(System.err);

  @Inject
  public ConsoleService (final AsciiTableResultSetScanner ascii) {
    this.ascii = ascii;
  }

  public void renderResultSet (final Query query, final OutputType outputType, final File outputFile) throws IOException {
    try (final PrintWriter outWriter = getOutWriter(outputFile)) {

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
                          .writer (outputType == OutputType.JSON ? null : new DefaultPrettyPrinter ())
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
                if (ctx.getStatement ().getUpdateCount () >= 0) {
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
  }

  protected PrintWriter getOutWriter (final File outputFile) throws IOException {
    return outputFile != null ?
            new PrintWriter (new FileWriter (outputFile)) :
            Optional.ofNullable (sysOut)
                    .orElse (new PrintWriter(new OutputStreamWriter (System.out)));
  }

  public PrintWriter getSysOut () {
    return sysOut;
  }

  public void setSysOut (final PrintWriter sysOut) {
    this.sysOut = sysOut;
  }

  public PrintStream getSysOutStream () {
    return new PrintStream (new WriterOutputStream (sysOut, Charset.defaultCharset()));
  }

  public PrintWriter getSysErr () {
    return sysErr;
  }

  public void setSysErr (final PrintWriter sysErr) {
    this.sysErr = sysErr;
  }

  public PrintStream getSysErrStream () {
    return new PrintStream (new WriterOutputStream (sysErr, Charset.defaultCharset()));
  }

  static class CsvResultSetScanner implements ResultSetScanner<Void> {
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

  public void printResultSet (final ResultSet resultSet) throws SQLException, IOException {
    renderStream(ascii.printResultSet(resultSet));
  }

  protected void renderStream (final Stream<String> stream) throws IOException {
    try (final PrintWriter outWriter = getOutWriter(null)) {
      stream.forEach((String row)->{
        outWriter.println (row);
      });
      outWriter.flush();
    }
  }

  public void renderTable (final Table<String,String,String> table, final int width) throws IOException {
    final AsciiTable ascii = new AsciiTable ();

    final int columnCount = table.columnKeySet().size();
    ascii.addRule ();// above header
//    final Collection<String> headers = new ArrayList<> ();
//
//    for (final String col : table.columnKeySet()) {
//      headers.add (col);
//    }
//    ascii.addRow (headers); // header
//    ascii.addRule (); // below header

    try (final PrintWriter outWriter = getOutWriter(null)) {
      table.rowMap().forEach((String row, Map<String, String> colVal) -> {
        final List<String> rowVals = new ArrayList<>(columnCount);
        colVal.forEach((String col, String val) -> rowVals.add(val));
        ascii.addRow(rowVals);
        ascii.addRule ();
      });

      ascii.renderAsIterator(width).forEachRemaining((String row)->{
        outWriter.println (row);
      });
      outWriter.flush();
    }
  }
}
