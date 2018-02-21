package com.github.davidecavestro.jdbsee.jdbcli

import com.google.common.collect.Iterables
import com.google.common.collect.Streams
import org.apache.commons.io.output.WriterOutputStream
import org.jdbi.v3.core.result.ResultIterable
import org.jdbi.v3.core.statement.Query
import org.junit.Test

public class CliApplicationITest {


    @Test
    public void testTable() throws Exception {
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
                "jdbc:h2:mem:test",
                "SELECT * FROM contacts;"
        )
    }

    @Test
    public void testCSV() throws Exception {
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
            "jdbc:h2:mem:test",
            "SELECT * FROM contacts;"
        )
    }

    @Test
    public void testJSONPretty() throws Exception {
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
                "jdbc:h2:mem:test",
                "SELECT * FROM contacts;"
        )
    }
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

}