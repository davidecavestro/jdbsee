package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriver
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDao
import spock.lang.Specification

class JdbsDriverDaoSpec extends Specification {

    ConfigService configService
    File tmpDataDir

    def setup() {
        def ghost = File.createTempFile('jdbsee', '.ghost')
        ghost.delete()

        tmpDataDir = new File (ghost.absolutePath - '.ghost')
        tmpDataDir.with {
            mkdirs()
            deleteOnExit()
        }

        configService = new ConfigService(){
            @Override
            File getConfigDir() {
                return tmpDataDir
            }
        }
    }

    def cleanup() {
        tmpDataDir.deleteDir()
    }

    def "listDrivers when db is empty"() {
        given: "an empty db"

        ConfigService configService = configService

        SettingsService settingsService = new SettingsService(configService: configService)
        JdbsDriverDao driverDao = new JdbsDriverDao(
            settingsService: settingsService,
            configService: configService
        )

        when: "listing all drivers"
        List drivers = driverDao.listDrivers()

        then: "no rows are found"
        drivers.isEmpty()
    }

    def "insert and listDrivers on a previously empty db"() {
        given: "an empty db"

        ConfigService configService = configService

        SettingsService settingsService = new SettingsService(configService: configService)
        JdbsDriverDao driverDao = new JdbsDriverDao(
            settingsService: settingsService,
            configService: configService
        )

        when: "inserting two drivers and listing all drivers"
        driverDao.insert('h2', 'org.h2.Driver', null, null, ['com.h2database:h2:1.4.196'] as String[])
        driverDao.insert('postgres', 'org.postgresql.Driver', null, [new File ('/fake/path/to/driver.jar')] as File[], [] as String[])
        List<JdbsDriver> drivers = driverDao.listDrivers()

        then: "two rows are found with relevant data"
        drivers.size() == 2
        with (drivers[0]) {
            name == 'h2'
            driverClass == 'org.h2.Driver'
            driverClassExpr == null
        }
        with (drivers[1]) {
            name == 'postgres'
            driverClass == 'org.postgresql.Driver'
            driverClassExpr == null
        }
    }
}
