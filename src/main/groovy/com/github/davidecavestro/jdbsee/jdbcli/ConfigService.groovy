package com.github.davidecavestro.jdbsee.jdbcli

import javax.inject.Inject

class ConfigService {
    @Inject
    ConfigService() {
    }

    File getDropinsDir () {
        new File (homeDir, 'dropins')
    }

    File getHomeDir () {
        new File (System.getenv('JDBSEE_HOME')?:System.getProperty('jdbsee.home', System.getProperty('user.dir')))
    }
}
