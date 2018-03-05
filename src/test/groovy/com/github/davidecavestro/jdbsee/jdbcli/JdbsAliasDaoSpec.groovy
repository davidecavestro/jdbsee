package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsAliasDao
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsAlias
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsAliasDao
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsAliasDetails
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDao


class JdbsAliasDaoSpec extends DaoSpec {


    def "listAliases when db is empty"() {
        given: "an empty db"

        ConfigService configService = configService

        SettingsService settingsService = new SettingsService(configService: configService)
        JdbsAliasDao aliasDao = new JdbsAliasDao(
            settingsService: settingsService,
            configService: configService
        )

        when: "listing all aliases"
        List aliases = aliasDao.listAliases()

        then: "no rows are found"
        aliases.isEmpty()
    }

    def "insert two aliases and list them"() {
        given: "an empty db"

        ConfigService configService = configService

        SettingsService settingsService = new SettingsService(configService: configService)
        JdbsDriverDao driverDao = new JdbsDriverDao(
            settingsService: settingsService,
            configService: configService
        )
        JdbsAliasDao aliasDao = new JdbsAliasDao(
            settingsService: settingsService,
            configService: configService,
            driverDao: driverDao
        )

        when: "inserting two aliases and listing all "
        driverDao.insert('h2', 'org.h2.Driver', null, [], ['com.h2database:h2:1.4.196'])
        aliasDao.insert('h2', 'testOne', 'jdbc:h2:mem:foo', 'sa', 'sa')
        aliasDao.insert('h2', 'testTwo', 'jdbc:h2:mem:bar', 'sa', null)
        List<JdbsAlias> aliases = aliasDao.listAliases()

        then: "two rows are found with relevant data"
        aliases.size() == 2
        with (aliases[0]) {
            name == 'testOne'
            url == 'jdbc:h2:mem:foo'
            username == 'sa'
            password == 'sa'
        }
        with (aliases[1]) {
            name == 'testTwo'
            url == 'jdbc:h2:mem:bar'
            username == 'sa'
            password == null
        }
    }

    def "insert two aliases and findAliasByName"() {
        given: "an empty db"

        ConfigService configService = configService

        SettingsService settingsService = new SettingsService(configService: configService)
        JdbsDriverDao driverDao = new JdbsDriverDao(
            settingsService: settingsService,
            configService: configService
        )
        JdbsAliasDao aliasDao = new JdbsAliasDao(
            settingsService: settingsService,
            configService: configService,
            driverDao: driverDao
        )

        when: "inserting two aliases and finding each by name"
        driverDao.insert('h2', 'org.h2.Driver', null, [], ['com.h2database:h2:1.4.196'])
        aliasDao.insert('h2', 'testOne', 'jdbc:h2:mem:foo', 'sa', 'sa')
        aliasDao.insert('h2', 'testTwo', 'jdbc:h2:mem:bar', 'sa', null)
        Optional<JdbsAliasDetails> aliasOneOpt = aliasDao.findAliasByName('testOne')
        Optional<JdbsAliasDetails> aliasTwoOpt = aliasDao.findAliasByName('testTwo')

        then: "the single aliases are found with relevant details"
        aliasOneOpt.present == true
        aliasTwoOpt.present == true
        with (aliasOneOpt.get()) {
            name == 'testOne'
            url == 'jdbc:h2:mem:foo'
            username == 'sa'
            password == 'sa'

            with (driver) {
                name == 'h2'
                driverClass == 'org.h2.Driver'
            }
        }

        with (aliasTwoOpt.get()) {
            name == 'testTwo'
            url == 'jdbc:h2:mem:bar'
            username == 'sa'
            password == null

            with (driver) {
                name == 'h2'
                driverClass == 'org.h2.Driver'
            }
        }
    }

    def "insert two aliases and remove one"() {
        given: "an empty db"

        ConfigService configService = configService

        SettingsService settingsService = new SettingsService(configService: configService)
        JdbsDriverDao driverDao = new JdbsDriverDao(
            settingsService: settingsService,
            configService: configService
        )
        JdbsAliasDao aliasDao = new JdbsAliasDao(
            settingsService: settingsService,
            configService: configService,
            driverDao: driverDao
        )

        when: "inserting two aliases and listing all "

        driverDao.insert('h2', 'org.h2.Driver', null, [], ['com.h2database:h2:1.4.196'])

        aliasDao.insert('h2', 'testOne', 'jdbc:h2:mem:foo', 'sa', 'sa')
        def toDelete = aliasDao.insert('h2', 'testTwo', 'jdbc:h2:mem:bar', 'sa', null)

        aliasDao.findAliasByName('testTwo').present == true

        def delCount = aliasDao.delete(toDelete)


        then: "the removed alias is not found while the remaining is found with relevant details"
        aliasDao.findAliasByName('testOne').present == true
        aliasDao.findAliasByName('testTwo').present == false
    }

}
