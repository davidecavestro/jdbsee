package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriver
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDao
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDetails
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

    def "insert two drivers and listDrivers on a previously empty db"() {
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

    def "insert two drivers and findDriverByName on a previously empty db"() {
        given: "an empty db"

        ConfigService configService = configService

        SettingsService settingsService = new SettingsService(configService: configService)
        JdbsDriverDao driverDao = new JdbsDriverDao(
            settingsService: settingsService,
            configService: configService
        )

        when: "inserting two drivers and finding single drivers by name"
        driverDao.insert('h2', 'org.h2.Driver', null, null, ['com.h2database:h2:1.4.196'] as String[])
        driverDao.insert('postgres', 'org.postgresql.Driver', null, [new File ('/fake/path/to/driver.jar')] as File[], [] as String[])
        Optional<JdbsDriverDetails> driverH2Opt = driverDao.findDriverByName('h2')
        Optional<JdbsDriverDetails> driverPsqlOpt = driverDao.findDriverByName('postgres')

        then: "the single drivers are found with relevant details"
        driverH2Opt.present == true
        driverPsqlOpt.present == true
        def driverH2 = driverH2Opt.get()
        def driverPsql = driverPsqlOpt.get()
        with (driverH2) {
            name == 'h2'
            driverClass == 'org.h2.Driver'
            driverClassExpr == null
            deps != null
            deps.size == 1

            with (deps[0]) {
                driverId == driverH2.id
                gav == 'com.h2database:h2:1.4.196'
            }
        }

        with (driverPsql) {
            name == 'postgres'
            driverClass == 'org.postgresql.Driver'
            driverClassExpr == null
            jars != null
            jars.size == 1

            with (jars[0]) {
                driverId == driverPsql.id
                file == new File ('/fake/path/to/driver.jar')
            }
        }
    }
}
