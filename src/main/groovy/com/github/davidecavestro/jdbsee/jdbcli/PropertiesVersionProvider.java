package com.github.davidecavestro.jdbsee.jdbcli;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import picocli.CommandLine.IVersionProvider;

import javax.inject.Inject;

/**
 * Provides version info from resource "/version.txt" within the jar.
 */
public class PropertiesVersionProvider implements IVersionProvider {

  private final Properties properties = new Properties();
  private final URL url;
  private Exception exception;

  @Inject
  public PropertiesVersionProvider() {
    url = getClass().getResource("/version.txt");
    if (url != null) {
      try {
        properties.load(url.openStream());
      } catch (final Exception e) {
        //save exception here to satisfy interface specs
        this.exception = e;
      }
    }
  }

  public String[] getVersion() throws Exception {
    if (url == null) {
      return new String[] {"No version.txt file found in the classpath. Is examples.jar in the classpath?"};
    }

    if (exception!=null) {
      throw exception;
    }

    return new String[] {
      String.format ("%s version \"%s\" ", getApplicationNameProp()),
      String.format ("Built: %s", getVersionProp(), getBuildtimeProp())
    };
  }

  public String getBuildtimeProp() {
    return properties.getProperty("Buildtime", "n.a.");
  }

  public String getVersionProp() {
    return properties.getProperty("Version", "n.a.");
  }

  public String getApplicationNameProp() {
    return properties.getProperty("Application-name", "n.a.");
  }

}

