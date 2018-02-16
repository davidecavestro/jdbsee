package com.github.davidecavestro.jdbsee.jdbcli;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import static com.google.common.collect.Iterators.*;

import com.google.common.collect.Streams;
import de.vandermeer.asciitable.AsciiTable;
import org.apache.commons.dbutils.ResultSetIterator;
import org.jdbi.v3.core.result.ResultSetScanner;
import org.jdbi.v3.core.statement.StatementContext;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class AsciiTableResultSetScanner implements ResultSetScanner<Stream<String>> {

  private int bufferSize = 20;

  @Inject
  protected AsciiTableResultSetScanner () {}

  @Override
  public Stream<String> scanResultSet (final Supplier<ResultSet> supplier, final StatementContext ctx) throws SQLException {
    final ResultSet resultSet = supplier.get ();
    final AsciiTable asciiHeader = new AsciiTable ();

    final ResultSetMetaData metaData = resultSet.getMetaData ();
    final int columnCount = metaData.getColumnCount ();
    asciiHeader.addRule ();// above header
    final Collection<String> headers = new ArrayList<> ();

    final AtomicInteger estimatedDisplaySize = new AtomicInteger (1);
    for (int colPos = 1; colPos <= columnCount; colPos++) {
      headers.add (metaData.getColumnLabel (colPos));
      estimatedDisplaySize.addAndGet (metaData.getColumnDisplaySize (colPos) + 1);
    }
    asciiHeader.addRow (headers); // header
    asciiHeader.addRule (); // below header

    return Streams.stream ( // iterator -> strem
        concat (            // flat
          transform (       // render buffer
            concat (        // header + row*
                Arrays.asList (asciiHeader).iterator (),
              iterate (resultSet)
            ),
            new Function<AsciiTable, Iterator<String>> () {
              @Nullable
              @Override
              public Iterator<String> apply (@Nullable final AsciiTable input) {
                return input.renderAsCollection (Math.min (estimatedDisplaySize.get (), 80)).iterator ();
              }
            })
        )
    );
  }

  protected Iterator<AsciiTable> iterate (final ResultSet resultSet) {
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
//          table.addRule ();
        }
        return  table;
      }
    });

  }
}
