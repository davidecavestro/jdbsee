package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDep
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriver
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDao
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDetails
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsJar
import org.apache.commons.io.output.WriterOutputStream
import spock.lang.Specification

class DriverServiceSpec extends Specification {

    def "listing two drivers"() {
        given: "a dao listing two drivers"

        JdbsDriverDao driverDao = Stub(JdbsDriverDao) {
            listDrivers() >> [
                new JdbsDriver(id: 1, name: 'h2', driverClassExpr: '(.*)Driver(.*)'),
                new JdbsDriver(id: 2, name: 'postgresql', driverClass: 'org.postgresql.Driver')
            ]
        }

        final StringWriter writer = new StringWriter()
        def printStream = new PrintStream(new WriterOutputStream(writer))
        final DriverService driverService = new DriverService(driverDao: driverDao, printStream: printStream)

        when: "listing all drivers"
        driverService.listDrivers()

        then: "the two drivers are listed in tabular format"
        def actual = writer.toString().trim()
        def expected = """\
┌───────────────────┬───────────────────┬───────────────────┬──────────────────┐
│ID                 │NAME               │CLASS NAME         │CLASS SEARCH REGEX│
├───────────────────┼───────────────────┼───────────────────┼──────────────────┤
│1                  │h2                 │-                  │(.*)Driver(.*)    │
├───────────────────┼───────────────────┼───────────────────┼──────────────────┤
│2                  │postgresql         │org.postgresql.Driv│-                 │
│                   │                   │er                 │                  │
└───────────────────┴───────────────────┴───────────────────┴──────────────────┘

""".stripIndent().trim()
        actual == expected
    }

    def "show driver by id"() {
        given: "a dao providing driver details by id"

        JdbsDriverDao driverDao = Stub(JdbsDriverDao) {
            findDriverById(10) >> Optional.of(new JdbsDriverDetails(
                id: 10,
                name: 'h2',
                driverClassExpr: null,
                driverClass: 'org.h2.Driver',
                jars: [
                    new JdbsJar(id: 20, driverId: 10, file: new File('/path/to/fake1.jar')),
                    new JdbsJar(id: 21, driverId: 10, file: new File('/path/to/fake2.jar'))
                ],
                deps: [
                    new JdbsDep(id: 15, driverId: 10, gav: 'org.acme:foo:1.0')
                ]
            ))
        }

        final StringWriter writer = new StringWriter()
        def printStream = new PrintStream(new WriterOutputStream(writer))
        final DriverService driverService = new DriverService(driverDao: driverDao, printStream: printStream)

        when: "showing driver details"
        driverService.showDriver(10)

        then: "the details are shown in tabular format"
        def actual = writer.toString().trim()
        def expected = """\
┌───────────────────────────────────────┬──────────────────────────────────────┐
│ID                                     │10                                    │
├───────────────────────────────────────┼──────────────────────────────────────┤
│NAME                                   │h2                                    │
├───────────────────────────────────────┼──────────────────────────────────────┤
│CLASS NAME                             │org.h2.Driver                         │
├───────────────────────────────────────┼──────────────────────────────────────┤
│CLASS NAME REGEX                       │-                                     │
├───────────────────────────────────────┴──────────────────────────────────────┤
│JARS                                                                          │
├──────────────────────────────────────────────────────────────────────────────┤
│# 20 - /path/to/fake1.jar                                                     │
├──────────────────────────────────────────────────────────────────────────────┤
│# 21 - /path/to/fake2.jar                                                     │
├┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┬┤
├┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┴┤
│DEPENDENCIES                                                                  │
├──────────────────────────────────────────────────────────────────────────────┤
│# 15 - org.acme:foo:1.0                                                       │
└──────────────────────────────────────────────────────────────────────────────┘
""".stripIndent().trim()
        actual == expected
    }

    def "show missing driver by id prints an error message"() {
        given: "a dao providing driver details by id"

        JdbsDriverDao driverDao = Stub(){
            findDriverById(10) >> Optional.ofNullable(null)
        }

        final StringWriter writer = new StringWriter()
        def printStream = new PrintStream(new WriterOutputStream(writer))
        final DriverService driverService = new DriverService(driverDao: driverDao, printStream: printStream)

        when: "showing driver details"
        driverService.showDriver(10)

        then: "an error message is shown in tabular format"
        def actual = writer.toString().trim()
        def expected = """\
┌──────────────────────────────────────────────────────────────────────────────┐
│No entries for id #10                                                         │
└──────────────────────────────────────────────────────────────────────────────┘
""".stripIndent().trim()
        actual == expected
    }

    def "show missing driver by name prints an error message"() {
        given: "a dao providing empty driver details by name"

        JdbsDriverDao driverDao = Stub(){
            findDriverByName('foo') >> Optional.ofNullable(null)
        }

        final StringWriter writer = new StringWriter()
        def printStream = new PrintStream(new WriterOutputStream(writer))
        final DriverService driverService = new DriverService(driverDao: driverDao, printStream: printStream)

        when: "showing driver details"
        driverService.showDriver('foo')

        then: "an error message is shown in tabular format"
        def actual = writer.toString().trim()
        def expected = """\
┌──────────────────────────────────────────────────────────────────────────────┐
│No entries for name 'foo'                                                     │
└──────────────────────────────────────────────────────────────────────────────┘
""".stripIndent().trim()
        actual == expected
    }

    def "show driver by id delegates to dao"() {
        given: "a dao providing empty driver details by id"

        JdbsDriverDao driverDao = Stub(){
            findDriverById(123) >> Optional.ofNullable(null)
        }

        final DriverService driverService = new DriverService(
            driverDao: driverDao,
            printStream: new PrintStream(new WriterOutputStream(new StringWriter()))
        )

        when: "showing driver details"
        driverService.showDriver(123)

        then: "no exceptions are thrown"
        notThrown(Exception)
    }

    def "show driver by name delegates to dao"() {
        given: "a dao providing driver details by name"

        JdbsDriverDao driverDao = Stub(){
            findDriverByName('h2') >> Optional.ofNullable(null)
        }

        final DriverService driverService = new DriverService(
            driverDao: driverDao,
            printStream: new PrintStream(new WriterOutputStream(new StringWriter()))
        )

        when: "showing driver details"
        driverService.showDriver('h2')

        then: "no exceptions are thrown"
        notThrown(Exception)
    }
}