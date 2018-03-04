package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDao
import de.vandermeer.asciitable.AsciiTable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject

class DriverCommandService {

  private final static Logger LOG = LoggerFactory.getLogger (DriverCommandService.class)

  @Inject//dagger
  public JdbsDriverDao driverDao

  @Inject//dagger
  public DriverCommandService (){}

  void listDrivers () {
    final AsciiTable asciiTable = new AsciiTable ();

    asciiTable.addRule ();// above header

    asciiTable.addRow('ID', 'NAME', 'CLASS NAME', 'CLASS SEARCH REGEX')
    asciiTable.addRule () // below header

    driverDao.listDrivers().each {
      addRow asciiTable, it.id as String, it.name, it.driverClass, it.driverClassExpr
      asciiTable.addRule () // below header
    }
    asciiTable.renderAsCollection()//FIXME autodetect screen width
    .each {
      System.out.println it
    }
  }

  void showDriver (final long driverId) {
    final AsciiTable asciiTable = new AsciiTable ();

    def driverDetails = driverDao.findDriverById(driverId)
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
      addRow asciiTable, "No entries for id #$driverId"
      asciiTable.addRule ()
    }


    asciiTable.renderAsCollection()//FIXME autodetect screen width
    .each {
      System.out.println it
    }
  }

  //see https://github.com/vdmeer/asciitable/issues/14
  def addRow (final AsciiTable asciiTable, final Object... cell) {
    asciiTable.addRow(cell.collect {it?:'-'})
  }
}
