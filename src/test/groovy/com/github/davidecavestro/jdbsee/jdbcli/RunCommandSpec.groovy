package com.github.davidecavestro.jdbsee.jdbcli

import spock.lang.Specification

class RunCommandSpec extends Specification {
    def "args and opts are passed to query service"() {
        given: "a run command initialized with args and opts"

        RunCommand runCommand = new RunCommand(
                url: 'jdbc:h2:memory:test',
                username: 'testuser',
                password: 'testpass',
                execute: ['cmd1...', 'cmd2...'],
                sqlText: 'SELECT * FROM foo'
        )

        QueryService queryService = Mock(QueryService.class)
        ConfigService configService = Mock(ConfigService.class)
        ConsoleService consoleService = Mock(ConsoleService.class)

        RunCommandService runCmdService = new RunCommandService(configService, consoleService, queryService)
        runCommand.runCommandService = runCmdService

        when: "the command is run"
        runCommand.run()

        then: "the queryService is indeed called with the respective args and opts"
        1 * queryService.execute(
                'jdbc:h2:memory:test',
                'testuser',
                'testpass',
                _ as QueryCallback,
                ['cmd1...', 'cmd2...', 'SELECT * FROM foo'])
    }
}
