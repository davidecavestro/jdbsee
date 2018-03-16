package com.github.davidecavestro.jdbsee.jdbcli

import org.apache.commons.io.output.WriterOutputStream
import org.junit.Test


class CliApplicationITest {

    void checkOutput(String expected, String... args) throws Exception {
        StringWriter writer = new StringWriter()
        PrintStream out = new PrintStream(new WriterOutputStream(writer))

        // replace sysout
        System.setOut(out)
        //run
        CliApplication.main(args)

        def actualData = writer.toString().trim()
        def expectedData = expected.stripIndent().trim()

        assert actualData == expectedData
    }

    @Test
    void testTable() throws Exception {
        checkOutput (
                '''\
┌───────────────────────────────────────┬──────────────────────────────────────┐
│ID                                     │NAME                                  │
└───────────────────────────────────────┴──────────────────────────────────────┘
│1                                      │Alice                                 │
│2                                      │Bob                                   │
└───────────────────────────────────────┴──────────────────────────────────────┘
            ''',
                "run",
                "-x",
                "create table contacts (id int primary key, name varchar(100));",
                "-x",
                "insert into contacts (id, name) values (1, 'Alice');",
                "-x",
                "insert into contacts (id, name) values (2, 'Bob');",
                "-d",
                "com.h2database:h2:1.4.196",
                "-l",
                "jdbc:h2:mem:test",
                "SELECT * FROM contacts;"
        )
    }

    @Test
    void testCSV() throws Exception {
        checkOutput (
    '''\
                ID;NAME
                1;Alice
                2;"Bo
                b"
                3;"Joh;n"
                4;"Da""isy"""
            ''',
            "run",
            "-x",
            "create table contacts (id int primary key, name varchar(100));",
            "-x",
            "insert into contacts (id, name) values (1, 'Alice');",
            "-x",
            "insert into contacts (id, name) values (2, 'Bo\nb');",
            "-x",
            "insert into contacts (id, name) values (3, 'Joh;n');",
            "-x",
            '''insert into contacts (id, name) values (4, 'Da"isy"');''',
            "-t",
            "CSV",
            "-d",
            "com.h2database:h2:1.4.196",
            "-l",
            "jdbc:h2:mem:test",
            "SELECT * FROM contacts;"
        )
    }

    @Test
    void testJSONPretty() throws Exception {
        checkOutput (
                '''\
                   [ {
                     "ID" : 1,
                     "NAME" : "Alice"
                   }, {
                     "ID" : 2,
                     "NAME" : "Bo\\nb"
                   }, {
                     "ID" : 3,
                     "NAME" : "Joh:n"
                   }, {
                     "ID" : 4,
                     "NAME" : "Da\\"isy\\""
                   } ]
                   ''',
                "run",
                "-x",
                "create table contacts (id int primary key, name varchar(100));",
                "-x",
                "insert into contacts (id, name) values (1, 'Alice');",
                "-x",
                "insert into contacts (id, name) values (2, 'Bo\nb');",
                "-x",
                "insert into contacts (id, name) values (3, 'Joh:n');",
                "-x",
                '''insert into contacts (id, name) values (4, 'Da"isy"');''',
                "-t",
                "JSON_PRETTY",
                "-d",
                "com.h2database:h2:1.4.196",
                "-l",
                "jdbc:h2:mem:test",
                "SELECT * FROM contacts;"
        )
    }

    @Test
    void testDeps() throws Exception {
        checkOutput (
            '''\
                FOO
                1
            ''',
            "run",
            "-t",
            "CSV",
            "-d",
            "com.h2database:h2:1.4.196",
            "-l",
            "jdbc:h2:mem:test",
            "SELECT 1 AS foo;"
        )
    }

}