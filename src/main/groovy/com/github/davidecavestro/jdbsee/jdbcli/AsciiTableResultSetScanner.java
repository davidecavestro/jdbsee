package com.github.davidecavestro.jdbsee.jdbcli;

import com.google.common.base.Function;

import static com.google.common.collect.Iterators.*;

import com.google.common.collect.Streams;
import de.vandermeer.asciitable.AsciiTable;
import org.jdbi.v3.core.result.NoResultsException;
import org.jdbi.v3.core.result.ResultSetScanner;
import org.jdbi.v3.core.statement.StatementContext;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class AsciiTableResultSetScanner implements ResultSetScanner<Stream<String>> {

  private int bufferSize = 20;

  @Inject
  protected AsciiTableResultSetScanner () {}

  @Override
  public Stream<String> scanResultSet (final Supplier<ResultSet> supplier, final StatementContext ctx) throws SQLException {
    try {
      return printResultSet (supplier.get());
    } catch (final NoResultsException ex) {
      return Streams.stream(Optional.empty());
    }
  }

  public Stream<String> printResultSet (final ResultSet resultSet) throws SQLException {
    final AsciiTable asciiHeader = new AsciiTable();

    final ResultSetMetaData metaData = resultSet.getMetaData();
    final int columnCount = metaData.getColumnCount();
    asciiHeader.addRule();// above header
    final Collection<String> headers = new ArrayList<>();

    final AtomicInteger estimatedDisplaySize = new AtomicInteger(1);
    for (int colPos = 1; colPos <= columnCount; colPos++) {
      headers.add(metaData.getColumnLabel(colPos));
      estimatedDisplaySize.addAndGet(metaData.getColumnDisplaySize(colPos) + 1);
    }
    asciiHeader.addRow(headers); // header
    asciiHeader.addRule(); // below header

    return Streams.stream( // iterator -> strem
        concat(            // flat
            transform(       // render buffer
                concat(        // header + row*
                    Arrays.asList(asciiHeader).iterator(),
                    iterate(resultSet)
                ),
                new Function<AsciiTable, Iterator<String>>() {
                  @Nullable
                  @Override
                  public Iterator<String> apply(@Nullable final AsciiTable input) {
//                    return input.renderAsCollection(Math.min(estimatedDisplaySize.get(), 80)).iterator();
                    return input.renderAsCollection(80).iterator();
                  }
                })
        )
    );
  }

  protected Iterator<AsciiTable> iterate (final ResultSet resultSet) throws SQLException {
    return concat (new Iterator<AsciiTable> () {
      private ResultSetIterator delegate = new ResultSetIterator (resultSet);

      @Override
      public boolean hasNext () {//is there another buffer table?
        return delegate.hasNext ();
      }

      @Override
      public AsciiTable next () {
        final AsciiTable table = new AsciiTable ();
        int counter = 0;
        while (counter++ < bufferSize && delegate.hasNext ()) {
          final Collection<Object> row = Arrays.asList (delegate.next ());
          table.addRow (row);
          if (!delegate.hasNext ()) {
            table.addRule ();
          }
        }
        return  table;
      }
    });

  }

  public static class ResultSetIterator implements Iterator<Object[]> {

    private final AtomicBoolean ahead = new AtomicBoolean ();
    private final ResultSet resultSet;
    private boolean hasNext;
    private final int columnCount;

    public ResultSetIterator (final ResultSet resultSet) throws SQLException {
      this.resultSet = resultSet;
      columnCount = resultSet.getMetaData ().getColumnCount ();
    }

    @Override
    public boolean hasNext () {
      if (ahead.compareAndSet (false, true)) {
        // ahead is false => needs a call to next() => go ahead
        try {
          hasNext = resultSet.next ();
        } catch (final SQLException e) {
          throw new RuntimeException (e);
        }
      }
      return hasNext;
    }

    @Override
    public Object[] next () {
      hasNext ();//make sure resultset.next() has been called
      final Object[] result = new Object[columnCount];
      for (int i = 1; i <= columnCount; i++) {
        try {
          result[i-1] = resultSet.getObject (i);
        } catch (SQLException e) {
          throw new RuntimeException (e);
        }
      }
      ahead.set (false);//no need to sync here (for this application)
      return result;
    }
  }
}
