package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.DriverCommand.JarAddCommand
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDao
import spock.lang.Specification

class JarAddCommandSpec extends Specification {
    def "args and opts are passed to driver dao"() {
        given: "a jar add command initialized with args and opts"

        JdbsDriverDao jdbsDriverDao = Mock(JdbsDriverDao)
        JarAddCommand jarAddCommand = new JarAddCommand(
            driverId: 1,
            jars: ['path/rp/jar1.jar', '/path/to/jar2.jar'],

            driverCommand: new DriverCommand (jdbsDriverDao: jdbsDriverDao)
        )

        when: "the command is run"
        jarAddCommand.run()

        then: "the dao is indeed called with the respective args and opts"
        1 * jdbsDriverDao.addJar(1, 'path/rp/jar1.jar', '/path/to/jar2.jar')
    }
}
