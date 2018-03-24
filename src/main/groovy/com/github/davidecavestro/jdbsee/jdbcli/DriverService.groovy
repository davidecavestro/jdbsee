package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDao
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDetails
import de.vandermeer.asciitable.AsciiTable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject

class DriverService {

  private final static Logger LOG = LoggerFactory.getLogger (DriverService.class)

  @Inject//dagger
  public JdbsDriverDao driverDao

  @Inject//dagger
  public ConsoleService consoleService

  def printStream = {ascii->consoleService.renderTable (ascii)}

  @Inject//dagger
  public DriverService(){}

  void listDrivers () {
    final AsciiTable asciiTable = new AsciiTable ();

    asciiTable.addRule ();// above header

    asciiTable.addRow('ID', 'NAME', 'CLASS NAME', 'CLASS SEARCH REGEX')
    asciiTable.addRule () // below header

    driverDao.listDrivers().each {
      addRow asciiTable, it.id as String, it.name, it.driverClass, it.driverClassExpr
      asciiTable.addRule () // below header
    }

    printStream(asciiTable)
  }

  void showDriver (final long driverId) {
    printDriverDetails driverDao.findDriverById(driverId), {"No entries for id #$driverId"}
  }

  void showDriver (final String name) {
    printDriverDetails driverDao.findDriverByName(name), {"No entries for name '$name'"}
  }

  protected void printDriverDetails (final Optional<JdbsDriverDetails> driverDetails, final Closure<String> noEntriesMsg) {
    final AsciiTable asciiTable = new AsciiTable()

    if (driverDetails.isPresent()) {
      driverDetails.get().with {
        asciiTable.addRule ()
        addRow asciiTable,'ID', it.id as String
        asciiTable.addRule ()
        addRow asciiTable, 'NAME', it.name
        asciiTable.addRule ()
        addRow asciiTable, 'CLASS NAME', it.driverClass
        asciiTable.addRule ()
        addRow asciiTable, 'CLASS NAME REGEX', it.driverClassExpr

        asciiTable.addRule ()
        asciiTable.addRow( null, 'JARS')
        asciiTable.addRule ()
        jars.each {jar->
          asciiTable.addRow null, "# $jar.id - ${jar.file.absolutePath}"
          asciiTable.addRule () // below header
        }

        asciiTable.addRule ()
        asciiTable.addRow( null, 'DEPENDENCIES')
        asciiTable.addRule ()
        deps.each {dep->
          asciiTable.addRow null, "# $dep.id - $dep.gav"
          asciiTable.addRule () // below header
        }
      }
    } else {
      asciiTable.addRule ()
      addRow asciiTable, noEntriesMsg()
      asciiTable.addRule ()
    }

    printStream(asciiTable)
  }

  //see https://github.com/vdmeer/asciitable/issues/14
  def addRow (final AsciiTable asciiTable, final Object... cell) {
    asciiTable.addRow(cell.collect {it?:'-'})
  }
}
