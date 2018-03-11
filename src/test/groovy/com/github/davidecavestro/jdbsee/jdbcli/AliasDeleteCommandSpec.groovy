package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsAliasDao
import spock.lang.Specification

import static com.github.davidecavestro.jdbsee.jdbcli.AliasCommand.AliasDeleteCommand

class AliasDeleteCommandSpec extends Specification {
    def "args and opts are passed to the dao"() {
        given: "a run command initialized with args and opts"

        def dao = Mock JdbsAliasDao
        def parentCmd = new AliasCommand(jdbsAliasDao: dao)
        AliasDeleteCommand cmd = new AliasDeleteCommand(
                aliasCommand: parentCmd,
                aliasIds: [1, 2]
        )


        when: "the command is run"
        cmd.run()

        then: "the dao is indeed called with the respective args and opts"
        1 * dao.delete(1, 2)
    }
}
