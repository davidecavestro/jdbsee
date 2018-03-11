package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsAliasDao
import spock.lang.Specification

import static com.github.davidecavestro.jdbsee.jdbcli.AliasCommand.AliasShowCommand

class AliasShowCommandSpec extends Specification {
    def "args and opts are passed to the service"() {
        given: "a run command initialized with args and opts"

        def dao = Mock JdbsAliasDao
        def svc = Mock AliasService
        def parentCmd = new AliasCommand(jdbsAliasDao: dao)
        AliasShowCommand cmd = new AliasShowCommand(
                aliasCommand: parentCmd,
                aliasService: svc,
                aliasId: 1
        )


        when: "the command is run"
        cmd.run()

        then: "the svc is indeed called with the respective args and opts"
        1 * svc.showAlias(1)
    }
}
