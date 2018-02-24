package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDao
import spock.lang.Specification

class JdbsDaoISpec extends Specification {
    def "args and opts are passed to query service"() {
        given: "a run command initialized with args and opts"

        SqlService sqlService = Mock(SqlService)
        JdbsDriverDao driverDao = new JdbsDriverDao(sqlService)


        when: "the command is run"
        driverDao.listDrivers()

        then: "the queryService is indeed called with the respective args and opts"
        1 * queryService.execute(
                'jdbc:h2:mem:test',
                'testuser',
                'testpass',
                _ as QueryCallback,
                ['cmd1...', 'cmd2...', 'SELECT * FROM foo'])
    }
}
