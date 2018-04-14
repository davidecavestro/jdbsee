package com.github.davidecavestro.jdbsee.jdbcli.config

import groovy.transform.EqualsAndHashCode

/**
 * Domain model for db driver jar dependency.
 */
@EqualsAndHashCode
class JdbsJar {
  Long id
  Long driverId
  File file
}
