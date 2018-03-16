package com.github.davidecavestro.jdbsee.jdbcli

import spock.lang.Specification
import static DescribeCommand.*

class DescribeCommandSpec extends Specification {
    def "args and opts are passed to the service"() {
        given: "a command initialized with args and opts"

        DescribeDriverCommand cmd = new DescribeDriverCommand(
                deps: ['com.h2database:h2:1.4.196'],
                url: 'jdbc:h2:mem:test',
                username: 'testuser',
                password: 'testpass'
        )

        DescribeCommandService service = Mock(DescribeCommandService)

        cmd.parentCommand = new DescribeCommand(
            service: service
        )

        when: "the command is run"
        cmd.run()

        then: "the service is indeed called with the respective args and opts"
        1 * service.run(cmd)
    }
}
