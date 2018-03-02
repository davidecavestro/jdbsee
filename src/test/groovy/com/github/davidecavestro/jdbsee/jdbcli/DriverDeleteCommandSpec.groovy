package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.DriverCommand.DriverDeleteCommand
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDao
import spock.lang.Specification

class DriverDeleteCommandSpec extends Specification {
    def "args are passed to driver dao"() {
        given: "a driver delete command initialized with args and opts"

        DriverDeleteCommand driverDeleteCommand = new DriverDeleteCommand(
            driverIds: [1,2,3]
        )

        JdbsDriverDao jdbsDriverDao = Mock(JdbsDriverDao)

        driverDeleteCommand.driverCommand = new DriverCommand (jdbsDriverDao: jdbsDriverDao)

        when: "the command is run"
        driverDeleteCommand.run()

        then: "the dao is indeed called with the respective args"
        1 * jdbsDriverDao.delete(1,2,3)
    }
}
