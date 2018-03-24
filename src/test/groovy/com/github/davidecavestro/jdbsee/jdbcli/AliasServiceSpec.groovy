package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.*
import org.apache.commons.io.output.WriterOutputStream
import spock.lang.Specification

class AliasServiceSpec extends Specification {

    def "listing two aliases"() {
        given: "a dao listing two aliases"

        JdbsAliasDao aliasDao = Stub(JdbsAliasDao) {
            def driver = new JdbsDriver(id: 1, name: 'psql', driverClass: 'org.postgresql.Driver')

            listAliases() >> [
                new JdbsAlias(
                    id: 1, driverId: driver.id, driver: driver,
                    name: 'testOne', url: 'jdbc:postgressql:testOne',
                    username: 'foo', password: 'nope'),
                new JdbsAlias(
                    id: 2, driverId: driver.id, driver: driver,
                    name: 'testTwo', url: 'jdbc:postgressql:testTwo')
            ]
        }

        final StringWriter writer = new StringWriter()
        def printWriter = new PrintWriter(writer)
        def consoleService = new ConsoleService (sysOut: printWriter)
        final AliasService aliasService = new AliasService(aliasDao: aliasDao, consoleService: consoleService)

        when: "listing all aliases"
        aliasService.listAliases()

        then: "the two aliases are listed in tabular format"
        def actual = writer.toString().trim()
        def expected = """\
┌───────────────────┬───────────────────┬───────────────────┬──────────────────┐
│ID                 │NAME               │DRIVER             │URL               │
├───────────────────┼───────────────────┼───────────────────┼──────────────────┤
│1                  │testOne            │psql               │jdbc:postgressql:t│
│                   │                   │                   │estOne            │
├───────────────────┼───────────────────┼───────────────────┼──────────────────┤
│2                  │testTwo            │psql               │jdbc:postgressql:t│
│                   │                   │                   │estTwo            │
└───────────────────┴───────────────────┴───────────────────┴──────────────────┘
""".stripIndent().trim()
        actual == expected
    }

    def "show alias by id"() {
        given: "a dao providing alias details by id"

        JdbsAliasDao aliasDao = Stub(JdbsAliasDao) {
            findAliasById(10) >> Optional.of(new JdbsAliasDetails(
                id: 10,
                name: 'testOne',
                url: 'jdbc:h2:file:/path/to/testOne',
                username: 'test',
                password: 'guess',
                driverId: 1,
                driver: new JdbsDriverDetails(
                    id: 1,
                    name: 'h2',
                    driverClassExpr: null,
                    driverClass: 'org.h2.Driver')
            ))
        }

        final StringWriter writer = new StringWriter()
        def printWriter = new PrintWriter(writer)
        def consoleService = new ConsoleService (sysOut: printWriter)
        final AliasService aliasService = new AliasService(aliasDao: aliasDao, consoleService: consoleService)

        when: "showing alias details"
        aliasService.showAlias(10)

        then: "the details are shown in tabular format"
        def actual = writer.toString().trim()
        def expected = """\
┌───────────────────────────────────────┬──────────────────────────────────────┐
│ID                                     │10                                    │
├───────────────────────────────────────┼──────────────────────────────────────┤
│NAME                                   │testOne                               │
├───────────────────────────────────────┼──────────────────────────────────────┤
│DRIVER                                 │h2                                    │
├───────────────────────────────────────┼──────────────────────────────────────┤
│URL                                    │jdbc:h2:file:/path/to/testOne         │
├───────────────────────────────────────┼──────────────────────────────────────┤
│USERNAME                               │test                                  │
├───────────────────────────────────────┼──────────────────────────────────────┤
│PASSWORD                               │guess                                 │
└───────────────────────────────────────┴──────────────────────────────────────┘
""".stripIndent().trim()
        actual == expected
    }

    def "show missing alias by id prints an error message"() {
        given: "a dao providing alias details by id"

        JdbsAliasDao aliasDao = Stub(){
            findAliasById(10) >> Optional.ofNullable(null)
        }

        final StringWriter writer = new StringWriter()
        def printWriter = new PrintWriter(writer)
        def consoleService = new ConsoleService (sysOut: printWriter)
        final AliasService aliasService = new AliasService(aliasDao: aliasDao, consoleService: consoleService)

        when: "showing alias details"
        aliasService.showAlias(10)

        then: "an error message is shown in tabular format"
        def actual = writer.toString().trim()
        def expected = """\
┌──────────────────────────────────────────────────────────────────────────────┐
│No entries for id #10                                                         │
└──────────────────────────────────────────────────────────────────────────────┘
""".stripIndent().trim()
        actual == expected
    }

    def "show missing alias by name prints an error message"() {
        given: "a dao providing empty alias details by name"

        JdbsAliasDao aliasDao = Stub(){
            findAliasByName('foo') >> Optional.ofNullable(null)
        }

        final StringWriter writer = new StringWriter()
        def printWriter = new PrintWriter(writer)
        def consoleService = new ConsoleService (sysOut: printWriter)
        final AliasService aliasService = new AliasService(aliasDao: aliasDao, consoleService: consoleService)

        when: "showing alias details"
        aliasService.showAlias('foo')

        then: "an error message is shown in tabular format"
        def actual = writer.toString().trim()
        def expected = """\
┌──────────────────────────────────────────────────────────────────────────────┐
│No entries for name 'foo'                                                     │
└──────────────────────────────────────────────────────────────────────────────┘
""".stripIndent().trim()
        actual == expected
    }

    def "show alias by id delegates to dao"() {
        given: "a dao providing empty alias details by id"

        JdbsAliasDao aliasDao = Stub(){
            findAliasById(123) >> Optional.ofNullable(null)
        }

        def printWriter = new PrintWriter(new StringWriter())
        def consoleService = new ConsoleService (sysOut: printWriter)
        final AliasService aliasService = new AliasService(aliasDao: aliasDao, consoleService: consoleService)

        when: "showing alias details"
        aliasService.showAlias(123)

        then: "no exceptions are thrown"
        notThrown(Exception)
    }

    def "show alias by name delegates to dao"() {
        given: "a dao providing alias details by name"

        JdbsAliasDao aliasDao = Stub(){
            findAliasByName('h2') >> Optional.ofNullable(null)
        }

        def printWriter = new PrintWriter(new StringWriter())
        def consoleService = new ConsoleService (sysOut: printWriter)
        final AliasService aliasService = new AliasService(aliasDao: aliasDao, consoleService: consoleService)

        when: "showing alias details"
        aliasService.showAlias('h2')

        then: "no exceptions are thrown"
        notThrown(Exception)
    }
}