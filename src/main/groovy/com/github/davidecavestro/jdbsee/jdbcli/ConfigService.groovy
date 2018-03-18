package com.github.davidecavestro.jdbsee.jdbcli

import groovy.transform.CompileStatic

import javax.inject.Inject

//FIXME extract interface
@CompileStatic
class ConfigService {
    @Inject
    ConfigService() {
    }

    File getUserFolder() {
        new File ( System.getProperty ( 'user.dir' ), '.jdbsee' )
    }

    List<File> getDropinsDirs () {
        [userFolder, homeDir].collect {new File (it, 'dropins')}
    }

    File getHomeDir () {
        new File (System.getenv('JDBSEE_HOME')?:System.getProperty('jdbsee.home', System.getProperty('user.dir')))
    }

    /**
     * Returns the user data folder
     */
    File getUserDataDir () {
        new File (userFolder, 'data')
    }
}
