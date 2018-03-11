package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsAliasDao
import spock.lang.Specification

import static com.github.davidecavestro.jdbsee.jdbcli.AliasCommand.AliasListCommand

class AliasListCommandSpec extends Specification {
    def "args and opts are passed to service"() {
        given: "a run command initialized with args and opts"

        def dao = Mock JdbsAliasDao
        def svc = Mock AliasService
        def parentCmd = new AliasCommand(jdbsAliasDao: dao)
        AliasListCommand cmd = new AliasListCommand(
                aliasCommand: parentCmd,
                aliasService: svc
        )


        when: "the command is run"
        cmd.run()

        then: "the service is indeed called with the respective args and opts"
        1 * svc.listAliases()
    }
}
