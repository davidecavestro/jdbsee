package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriver
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDao
import com.github.davidecavestro.jdbsee.jdbcli.config.JdbsDriverDetails
import spock.lang.Specification

abstract class DaoSpec extends Specification {

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

}
