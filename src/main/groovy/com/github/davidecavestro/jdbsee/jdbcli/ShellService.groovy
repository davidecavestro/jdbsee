package com.github.davidecavestro.jdbsee.jdbcli

import com.google.common.base.Function
import dagger.Component;
import org.jline.reader.LineReader
import org.jline.reader.MaskingCallback

import javax.inject.Inject
import java.sql.ResultSet
import java.sql.SQLException;

class ShellService {
  @Inject //public for dagger
  public ConsoleService consoleService

  @Inject //dagger
  ShellService(){}

  String readLine(final String prompt, final LineReader reader) {
    reader.readLine (prompt, null, (MaskingCallback) null, null)
  }

  def setSysStreams(final PrintWriter writer) {
    consoleService.setSysErr (writer)
    consoleService.setSysOut (writer)
  }


  def withSysOutStream (Closure closure) {
    consoleService.withSysOutStream(new Function<PrintStream, Void>() {
      @Override
      Void apply(final PrintStream sysOut) {
        closure (sysOut)
        return null
      }
    })
  }

  void printResultSet (final ResultSet resultSet) throws SQLException, IOException {
    consoleService.printResultSet(resultSet)
  }

  void printRowsAffected(int rows) {
    consoleService.printRowsAffected(rows)
  }
}
