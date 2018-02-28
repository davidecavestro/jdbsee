package com.github.davidecavestro.jdbsee.jdbcli.config

import groovy.transform.EqualsAndHashCode;


@EqualsAndHashCode
class JdbsDriverDetails extends JdbsDriver{
    List<JdbsJar> jars
    List<JdbsDep> deps
}
