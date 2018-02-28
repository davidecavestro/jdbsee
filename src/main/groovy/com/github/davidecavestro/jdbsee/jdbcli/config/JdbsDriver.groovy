package com.github.davidecavestro.jdbsee.jdbcli.config

import groovy.transform.EqualsAndHashCode;


@EqualsAndHashCode
class JdbsDriver {
    Long id
    String name
    String driverClass
    String driverClassExpr
}
