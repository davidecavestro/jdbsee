package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.DriverCommand.DependencyAddCommand
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDao
import spock.lang.Specification

class DependencyAddCommandSpec extends Specification {
    def "args and opts are passed to driver dao"() {
        given: "a dependency add command initialized with args and opts"

        JdbsDriverDao jdbsDriverDao = Mock(JdbsDriverDao)
        DependencyAddCommand depAddCommand = new DependencyAddCommand(
            driverId: 1,
            deps: ['com.acme:foo:1.0', 'com.acme:bar:2.0'],

            driverCommand: new DriverCommand (jdbsDriverDao: jdbsDriverDao)
        )

        when: "the command is run"
        depAddCommand.run()

        then: "the dao is indeed called with the respective args and opts"
        1 * jdbsDriverDao.addDependency(1, 'com.acme:foo:1.0', 'com.acme:bar:2.0')
    }
}
