package com.github.davidecavestro.jdbsee.jdbcli

import spock.lang.Specification

class DriverManagerDataSourceSpec extends Specification {
    def "passes props to driver manager facade"() {
        given: "a datasource initialized with props"

        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                url: 'jdbc:h2:mem:test',
                username: 'testuser',
                password: 'testpass'
        )

        DriverManagerFacade driverManagerFacade = Mock(DriverManagerFacade)
        dataSource.driverManagerFacade = driverManagerFacade

        when: "a connection is requested is run"
        dataSource.getConnection()

        then: "the driver manager facade is indeed called with the respective props"
        1 * driverManagerFacade.getConnection(
            'jdbc:h2:mem:test',
                _ as Properties)
    }
}
