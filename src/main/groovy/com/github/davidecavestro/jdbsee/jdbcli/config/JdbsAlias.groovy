package com.github.davidecavestro.jdbsee.jdbcli.config

import groovy.transform.EqualsAndHashCode;


@EqualsAndHashCode
class JdbsAlias {
    Long id
    String name
    String url
    String username
    String password

    Long driverId
    JdbsDriver driver
}
