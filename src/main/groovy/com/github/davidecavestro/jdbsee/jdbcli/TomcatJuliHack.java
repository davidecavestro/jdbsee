package com.github.davidecavestro.jdbsee.jdbcli;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.inject.Inject;

public class TomcatJuliHack {

  @Inject
  public TomcatJuliHack () {
  }

  public void install (){
    LogManager.getLogManager().reset();
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
    Logger.getLogger("global").setLevel(Level.FINEST);
  }
}
