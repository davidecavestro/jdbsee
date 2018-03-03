package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.DriverCommand.JarRemoveCommand
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDao
import spock.lang.Specification

class JarRemoveCommandSpec extends Specification {
    def "args and opts are passed to driver dao"() {
        given: "a jar remove command initialized with args and opts"

        JdbsDriverDao jdbsDriverDao = Mock(JdbsDriverDao)
        JarRemoveCommand jarRemoveCommand = new JarRemoveCommand(
            ids: [1,2],

            driverCommand: new DriverCommand (jdbsDriverDao: jdbsDriverDao)
        )

        when: "the command is run"
        jarRemoveCommand.run()

        then: "the dao is indeed called with the respective args and opts"
        1 * jdbsDriverDao.removeJar(1, 2)
    }
}
