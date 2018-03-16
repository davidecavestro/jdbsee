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

    @Test
    void testDescribeDriver() throws Exception {
        checkOutput (
            '''\
       ┌───────────────────────────────────────┬──────────────────────────────────────┐
       │key                                    │value                                 │
       ├───────────────────────────────────────┼──────────────────────────────────────┤
       │schemas                                │rs8:                                  │
       │                                       │org.h2.result.LocalResult@503         │
       │                                       │56b5 columns: 3 rows: 2 pos: -1       │
       │extraNameCharacters                    │                                      │
       │maxBinaryLiteralLength                 │0                                     │
       │numericFunctions                       │ABS,ACOS,ASIN,ATAN,COS,COSH,COT,SIN,SI│
       │                                       │NH,TAN,TANH,ATAN2,BITAND,BITGET,BITOR,│
       │                                       │BITXOR,MOD,CEILING,DEGREES,EXP,FLOOR,L│
       │                                       │OG,LOG10,RADIANS,SQRT,PI,POWER,RAND,RA│
       │                                       │NDOM_UUID,ROUND,ROUNDMAGIC,SECURE_RAND│
       │                                       │,SIGN,ENCRYPT,DECRYPT,HASH,TRUNCATE,CO│
       │                                       │MPRESS,EXPAND,ZERO                    │
       │maxConnections                         │0                                     │
       │URL                                    │jdbc:h2:mem:test                      │
       │maxCharLiteralLength                   │0                                     │
       │tableTypes                             │rs10:                                 │
       │                                       │org.h2.result.LocalResult@5           │
       │                                       │67268 columns: 1 rows: 5 pos: -1      │
       │traceObjectName                        │dbMeta0                               │
       │timeDateFunctions                      │CURRENT_DATE,CURRENT_TIME,CURRENT_TIME│
       │                                       │STAMP,DATEADD,DATEDIFF,DAYNAME,DAY_OF_│
       │                                       │MONTH,DAY_OF_WEEK,DAY_OF_YEAR,EXTRACT,│
       │                                       │FORMATDATETIME,HOUR,MINUTE,MONTH,MONTH│
       │                                       │NAME,PARSEDATETIME,QUARTER,SECOND,WEEK│
       │                                       │,YEAR                                 │
       │catalogSeparator                       │.                                     │
       │maxColumnsInIndex                      │0                                     │
       │databaseMinorVersion                   │4                                     │
       │clientInfoProperties                   │org.h2.tools.SimpleResultSet@1af7f54a │
       │JDBCMajorVersion                       │4                                     │
       │maxIndexLength                         │0                                     │
       │maxStatementLength                     │0                                     │
       │maxSchemaNameLength                    │0                                     │
       │maxTablesInSelect                      │0                                     │
       │traceId                                │0                                     │
       │maxTableNameLength                     │0                                     │
       │maxCursorNameLength                    │0                                     │
       │databaseMajorVersion                   │1                                     │
       │searchStringEscape                     │\\                                     │
       │typeInfo                               │rs12:                                 │
       │                                       │org.h2.result.LocalResult@4d          │
       │                                       │57787 columns: 18 rows: 31 pos: -1    │
       │procedureTerm                          │procedure                             │
       │infoEnabled                            │false                                 │
       │stringFunctions                        │ASCII,BIT_LENGTH,LENGTH,OCTET_LENGTH,C│
       │                                       │HAR,CONCAT,CONCAT_WS,DIFFERENCE,HEXTOR│
       │                                       │AW,RAWTOHEX,INSTR,INSERT,LOWER,UPPER,L│
       │                                       │EFT,RIGHT,LOCATE,POSITION,LPAD,RPAD,LT│
       │                                       │RIM,RTRIM,TRIM,REGEXP_REPLACE,REGEXP_L│
       │                                       │IKE,REPEAT,REPLACE,SOUNDEX,SPACE,STRIN│
       │                                       │GDECODE,STRINGENCODE,STRINGTOUTF8,SUBS│
       │                                       │TRING,UTF8TOSTRING,XMLATTR,XMLNODE,XML│
       │                                       │COMMENT,XMLCDATA,XMLSTARTDOC,XMLTEXT,T│
       │                                       │O_CHAR,TRANSLATE                      │
       │maxStatements                          │0                                     │
       │SQLKeywords                            │LIMIT,MINUS,ROWNUM,SYSDATE,SYSTIME,SYS│
       │                                       │TIMESTAMP,TODAY                       │
       │catalogs                               │rs14:                                 │
       │                                       │org.h2.result.LocalResult@51          │
       │                                       │929ae columns: 1 rows: 1 pos: -1      │
       │databaseProductVersion                 │1.4.196 (2017-06-10)                  │
       │resultSetHoldability                   │2                                     │
       │JDBCMinorVersion                       │0                                     │
       │identifierQuoteString                  │"                                     │
       │userName                               │TEST                                  │
       │connection                             │conn1: url=jdbc:h2:mem:test user=TEST │
       │class                                  │class org.h2.jdbc.JdbcDatabaseMetaData│
       │catalogTerm                            │catalog                               │
       │debugEnabled                           │false                                 │
       │maxCatalogNameLength                   │0                                     │
       │driverName                             │H2 JDBC Driver                        │
       │driverMajorVersion                     │1                                     │
       │maxColumnsInSelect                     │0                                     │
       │maxColumnsInGroupBy                    │0                                     │
       │maxProcedureNameLength                 │0                                     │
       │readOnly                               │false                                 │
       │systemFunctions                        │ARRAY_GET,ARRAY_LENGTH,ARRAY_CONTAINS,│
       │                                       │AUTOCOMMIT,CANCEL_SESSION,CASEWHEN,CAS│
       │                                       │T,COALESCE,CONVERT,CURRVAL,CSVREAD,CSV│
       │                                       │WRITE,DATABASE,DATABASE_PATH,DECODE,DI│
       │                                       │SK_SPACE_USED,FILE_READ,FILE_WRITE,GRE│
       │                                       │ATEST,IDENTITY,IFNULL,LEAST,LOCK_MODE,│
       │                                       │LOCK_TIMEOUT,LINK_SCHEMA,MEMORY_FREE,M│
       │                                       │EMORY_USED,NEXTVAL,NULLIF,NVL2,READONL│
       │                                       │Y,ROWNUM,SCHEMA,SCOPE_IDENTITY,SESSION│
       │                                       │_ID,SET,TABLE,TRANSACTION_ID,TRUNCATE_│
       │                                       │VALUE,USER,H2VERSION                  │
       │maxUserNameLength                      │0                                     │
       │maxLogicalLobSize                      │0                                     │
       │maxColumnNameLength                    │0                                     │
       │maxRowSize                             │0                                     │
       │driverMinorVersion                     │4                                     │
       │databaseProductName                    │H2                                    │
       │catalogAtStart                         │true                                  │
       │defaultTransactionIsolation            │2                                     │
       │SQLStateType                           │2                                     │
       │schemaTerm                             │schema                                │
       │rowIdLifetime                          │ROWID_UNSUPPORTED                     │
       │maxColumnsInOrderBy                    │0                                     │
       │maxColumnsInTable                      │0                                     │
       │driverVersion                          │1.4.196 (2017-06-10)                  │
       └───────────────────────────────────────┴──────────────────────────────────────┘
            ''',
            "describe",
            "driver",
            "-u",
            "test",
            "-p",
            "test",
            "-d",
            "com.h2database:h2:1.4.196",
            "-l",
            "jdbc:h2:mem:test"
        )
    }

}