package com.github.davidecavestro.jdbsee.jdbcli.config

import groovy.transform.EqualsAndHashCode

/**
 * Domain model for db driver maven-like dependency.
 */
@EqualsAndHashCode
class JdbsDep {
  Long id
  Long driverId
  String gav
}
