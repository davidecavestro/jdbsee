package com.github.davidecavestro.jdbsee.jdbcli

import spock.lang.Specification

import javax.sql.DataSource

class RunCommandSpec extends Specification {
    def "args and opts are passed to query service"() {
        given: "a run command initialized with args and opts"

        RunCommand runCommand = new RunCommand(
                url: 'jdbc:h2:mem:test',
                username: 'testuser',
                password: 'testpass',
                execute: ['cmd1...', 'cmd2...'],
                sqlText: 'SELECT * FROM foo'
        )

        QueryService queryService = Mock(QueryService)

        runCommand.runCommandService = new RunCommandService(
                configService: Stub(ConfigService),
                consoleService: Stub(ConsoleService),
                queryService: queryService,
                driverManagerFacade: Stub(DriverManagerFacade))

        when: "the command is run"
        runCommand.run()

        then: "the queryService is indeed called with the respective args and opts"
        1 * queryService.execute(
                {
                    it.url == 'jdbc:h2:mem:test' &&
                    it.username == 'testuser' &&
                    it.password == 'testpass'
                },
                _ as QueryCallback,
                ['cmd1...', 'cmd2...', 'SELECT * FROM foo']
        )
    }
}
