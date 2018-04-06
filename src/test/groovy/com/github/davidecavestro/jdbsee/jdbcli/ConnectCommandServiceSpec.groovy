package com.github.davidecavestro.jdbsee.jdbcli

import spock.lang.Specification


class ConnectCommandServiceSpec extends Specification {


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

  def "the sql session returns data"() {
    given: "a service and a command initialized with args"

    ConnectCommand cmd = new ConnectCommand(
            deps: ['com.h2database:h2:1.4.196'],
            url: 'jdbc:h2:mem:test',
            username: 'testuser',
            password: 'testpass'
    )
    cmd.parent = Mock(MainCommand)

    ConsoleService consoleService = Mock(ConsoleService)
//    ShellService shellService = Spy(ShellService)
    ShellService shellService = new ShellService()

    shellService.consoleService = consoleService

    ConnectCommandService service = new ConnectCommandService (
        configService: configService,
        shellService: shellService
    )

    shellService.readLine (_) >>> ['help', 'SELECT 1', 'quit']


    when: "the service is invoked"
    service.run(cmd)

    then: "the service is indeed called with the respective args and opts"
//    1 * shellService.printResultSet(_)
  }
}
