package com.github.davidecavestro.jdbsee.jdbcli.config

import groovy.transform.EqualsAndHashCode;

/**
 * Domain model for db driver settings.
 */
@EqualsAndHashCode
class JdbsDriver {
    Long id
    String name
    String driverClass
    String driverClassExpr
}
