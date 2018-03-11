package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsAliasDao
import spock.lang.Specification
import static com.github.davidecavestro.jdbsee.jdbcli.AliasCommand.*

class AliasCreateCommandSpec extends Specification {
    def "args and opts are passed to the dao"() {
        given: "a run command initialized with args and opts"

        def dao = Mock JdbsAliasDao
        def parentCmd = new AliasCommand(jdbsAliasDao: dao)
        AliasCreateCommand cmd = new AliasCreateCommand(
                aliasCommand: parentCmd,
                driver: '1',
                name: 'foo',
                url: 'jdbc:h2:mem:test',
                username: 'testuser',
                password: 'testpass'
        )


        when: "the command is run"
        cmd.run()

        then: "the dao is indeed called with the respective args and opts"
        1 * dao.insert(
            '1',
            'foo',
            'jdbc:h2:mem:test',
            'testuser',
            'testpass'
        )
    }
}
