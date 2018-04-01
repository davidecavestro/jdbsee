package com.github.davidecavestro.jdbsee.jdbcli

import spock.lang.Specification


class ConnectCommandSpec extends Specification {
    def "args and opts are passed to the service"() {
        given: "a command initialized with args and opts"

        ConnectCommand cmd = new ConnectCommand(
                deps: ['com.h2database:h2:1.4.196'],
                url: 'jdbc:h2:mem:test',
                username: 'testuser',
                password: 'testpass'
        )

        ConnectCommandService service = Mock(ConnectCommandService)

        cmd.with {
          parent = Mock(MainCommand)
          connectCommandService = service
        }

        when: "the command is run"
        cmd.run()

        then: "the service is indeed called with the respective args and opts"
        1 * service.run(cmd)
    }
}
