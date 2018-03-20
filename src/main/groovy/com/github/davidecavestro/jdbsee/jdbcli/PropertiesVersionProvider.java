package com.github.davidecavestro.jdbsee.jdbcli;

import java.net.URL;
import java.util.Properties;
import picocli.CommandLine.IVersionProvider;

import javax.inject.Inject;

/**
 * Provides version info from resource "/version.txt" within the jar.
 */
public class PropertiesVersionProvider implements IVersionProvider {

  @Inject
  public PropertiesVersionProvider(){}

  public String[] getVersion() throws Exception {
    final URL url = getClass().getResource("/version.txt");
    if (url == null) {
      return new String[] {"No version.txt file found in the classpath. Is examples.jar in the classpath?"};
    }
    Properties properties = new Properties ();
    properties.load(url.openStream());
    return new String[] {
        properties.getProperty("Application-name") + " version \"" + properties.getProperty("Version") + "\"",
        "Built: " + properties.getProperty("Buildtime"),
    };
  }
}

