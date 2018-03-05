package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsAliasDao
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsAliasDetails
import de.vandermeer.asciitable.AsciiTable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject

class AliasService {

  private final static Logger LOG = LoggerFactory.getLogger (AliasService.class)

  PrintStream printStream = System.out

  @Inject//dagger
  public JdbsAliasDao aliasDao

  @Inject//dagger
  public AliasService(){}

  void listAlias () {
    final AsciiTable asciiTable = new AsciiTable ();

    asciiTable.addRule ();// above header

    asciiTable.addRow('ID', 'NAME', 'DRIVER', 'URL')
    asciiTable.addRule () // below header

    aliasDao.listAliases().each {
      addRow asciiTable, it.id as String, it.name, it.driver.name, it.url
      asciiTable.addRule () // below header
    }
    asciiTable.renderAsCollection()//FIXME autodetect screen width
    .each {
      printStream.println it
    }
    printStream.flush()
  }

  void showAlias (final long aliasId) {
    printAliasDetails aliasDao.findAliasById(aliasId), {"No entries for id #$aliasId"}
  }

  void showAlias (final String name) {
    printAliasDetails aliasDao.findAliasByName(name), {"No entries for name '$name'"}
  }

  protected void printAliasDetails (final Optional<JdbsAliasDetails> aliasDetails, final Closure<String> noEntriesMsg) {
    final AsciiTable asciiTable = new AsciiTable()

    if (aliasDetails.isPresent()) {
      aliasDetails.get().with {
        asciiTable.addRule ()
        addRow asciiTable,'ID', it.id as String
        asciiTable.addRule ()
        addRow asciiTable, 'NAME', it.name
        asciiTable.addRule ()
        addRow asciiTable, 'DRIVER', it.driver.name
        asciiTable.addRule ()
        addRow asciiTable, 'URL', it.url
        asciiTable.addRule ()
        addRow asciiTable, 'USERNAME', it.username
        asciiTable.addRule ()
        addRow asciiTable, 'PASSWORD', it.password//TODO mask it
      }
    } else {
      asciiTable.addRule ()
      addRow asciiTable, noEntriesMsg()
      asciiTable.addRule ()
    }


    asciiTable.renderAsCollection()//FIXME autodetect screen width
    .each {
      printStream.println it
    }
    printStream.flush()
  }

  //see https://github.com/vdmeer/asciitable/issues/14
  def addRow (final AsciiTable asciiTable, final Object... cell) {
    asciiTable.addRow(cell.collect {it?:'-'})
  }
}
