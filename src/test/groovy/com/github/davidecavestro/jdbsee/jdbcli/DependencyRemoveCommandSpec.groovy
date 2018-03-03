package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.DriverCommand.DependencyRemoveCommand
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDao
import spock.lang.Specification

class DependencyRemoveCommandSpec extends Specification {
    def "args and opts are passed to driver dao"() {
        given: "a dependency remove command initialized with args and opts"

        JdbsDriverDao jdbsDriverDao = Mock(JdbsDriverDao)
        DependencyRemoveCommand depRemoveCommand = new DependencyRemoveCommand(
            ids: [1,2],

            driverCommand: new DriverCommand (jdbsDriverDao: jdbsDriverDao)
        )

        when: "the command is run"
        depRemoveCommand.run()

        then: "the dao is indeed called with the respective args and opts"
        1 * jdbsDriverDao.removeDependency(1, 2)
    }
}
