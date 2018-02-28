package com.github.davidecavestro.jdbsee.jdbcli

import javax.inject.Inject

//FIXME extract interface
class ConfigService {
    @Inject
    ConfigService() {
    }

    String getUserFolderPath() {
        new File ( System.getProperty ( 'user.dir' ), '.jdbsee' ).absolutePath
    }

    File getDropinsDir () {
        new File (homeDir, 'dropins')
    }

    File getHomeDir () {
        new File (System.getenv('JDBSEE_HOME')?:System.getProperty('jdbsee.home', System.getProperty('user.dir')))
    }

    File getDataDir () {
        new File (configDir, 'data')
    }

    File getConfigDir () {
        new File (System.getenv('JDBSEE_CONFIG')?:System.getProperty('jdbsee.config', userFolderPath))
    }
}
