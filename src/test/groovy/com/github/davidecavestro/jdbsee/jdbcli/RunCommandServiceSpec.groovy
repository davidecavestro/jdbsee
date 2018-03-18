package com.github.davidecavestro.jdbsee.jdbcli

import com.github.davidecavestro.jdbsee.jdbcli.config.*
import org.apache.commons.io.output.WriterOutputStream
import spock.lang.Specification

class RunCommandServiceSpec extends Specification {

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
            File getUserDataDir() {
                return tmpDataDir
            }
        }
    }

    def cleanup() {
        tmpDataDir.deleteDir()
    }

    def "loading driver from jar"() {
        given: "a run command referring a jar"

        RunCommand runCommand = new RunCommand(
            url: 'jdbc:h2:mem:test',
            sqlText: "SELECT 1",
            jars: [new File ('http://central.maven.org/maven2/com/h2database/h2/1.4.196/h2-1.4.196.jar')]
        )

        def consoleService = Mock(ConsoleService)
        def queryService = Mock(QueryService)
        def driverManagerFacade = new DriverManagerFacade()
        final RunCommandService runCommandService = new RunCommandService(
            queryService: queryService, consoleService: consoleService,
            configService: configService, driverManagerFacade: driverManagerFacade)

        when: "runnning the command"
        runCommandService.run(runCommand)

        then: "no exceptions are thrown"
        notThrown(Exception)
    }

}