package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDao
import spock.lang.Specification
import com.github.davidecavestro.jdbsee.jdbcli.DriverCommand.DriverCreateCommand

class DriverCreateCommandSpec extends Specification {
    def "args and opts are passed to driver dao"() {
        given: "a driver create command initialized with args and opts"

        DriverCreateCommand driverCreateCommand = new DriverCreateCommand(
                name: 'acme',
                driverClassName: 'org.acme.foo.Driver',
                jars: ['path/rp/jar1.jar', '/path/to/jar2.jar'],
                deps: ['org.acme:foo:1.0']
        )

        JdbsDriverDao jdbsDriverDao = Mock(JdbsDriverDao)

        driverCreateCommand.driverCommand = new DriverCommand (jdbsDriverDao: jdbsDriverDao)

        when: "the command is run"
        driverCreateCommand.run()

        then: "the dao is indeed called with the respective args and opts"
        1 * jdbsDriverDao.insert(
            'acme', 'org.acme.foo.Driver', '(.*)Driver(.*)', ['path/rp/jar1.jar', '/path/to/jar2.jar'], ['org.acme:foo:1.0'])
    }
}
