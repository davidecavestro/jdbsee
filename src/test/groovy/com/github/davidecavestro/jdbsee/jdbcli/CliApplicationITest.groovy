package com.github.davidecavestro.jdbsee.jdbcli

import org.apache.commons.io.input.ReaderInputStream
import org.apache.commons.io.output.WriterOutputStream
import org.junit.Test
import org.spockframework.util.TeePrintStream


class CliApplicationITest {

    StringWriter getOutput(String... args) throws Exception {
        StringWriter writer = new StringWriter()
        PrintStream out = new PrintStream(new WriterOutputStream(writer))

        // replace sysout
        System.setOut(out)
        //run
        CliApplication.main(args)

        return writer
    }


    void checkOutput(String expected, Closure transform = {it->it}, String... args) throws Exception {
        def actualData = transform(getOutput(transform, args).toString().trim())
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
       ├───────────────────────────────────────┼──────────────────────────────────────┤
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
        //FIXME replace with smarter check
        checkOutput (
            '''\
        ┌───────────────────────────────────────┬──────────────────────────────────────┐
        │schemas                                │[[CATALOG_NAME:TEST, IS_DEFAULT:false,│
        │                                       │SCHEMA_NAME:INFORMATION_SCHEMA], [CATA│
        │                                       │LOG_NAME:TEST,        IS_DEFAULT:true,│
        │                                       │SCHEMA_NAME:PUBLIC]]                  │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │extraNameCharacters                    │                                      │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxBinaryLiteralLength                 │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │numericFunctions                       │ABS,ACOS,ASIN,ATAN,COS,COSH,COT,SIN,SI│
        │                                       │NH,TAN,TANH,ATAN2,BITAND,BITGET,BITOR,│
        │                                       │BITXOR,MOD,CEILING,DEGREES,EXP,FLOOR,L│
        │                                       │OG,LOG10,RADIANS,SQRT,PI,POWER,RAND,RA│
        │                                       │NDOM_UUID,ROUND,ROUNDMAGIC,SECURE_RAND│
        │                                       │,SIGN,ENCRYPT,DECRYPT,HASH,TRUNCATE,CO│
        │                                       │MPRESS,EXPAND,ZERO                    │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxConnections                         │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │URL                                    │jdbc:h2:mem:test                      │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxCharLiteralLength                   │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │tableTypes                             │[[TYPE:EXTERNAL], [TYPE:SYSTEM TABLE],│
        │                                       │[TYPE:TABLE],    [TYPE:TABLE    LINK],│
        │                                       │[TYPE:VIEW]]                          │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │traceObjectName                        │dbMeta0                               │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │timeDateFunctions                      │CURRENT_DATE,CURRENT_TIME,CURRENT_TIME│
        │                                       │STAMP,DATEADD,DATEDIFF,DAYNAME,DAY_OF_│
        │                                       │MONTH,DAY_OF_WEEK,DAY_OF_YEAR,EXTRACT,│
        │                                       │FORMATDATETIME,HOUR,MINUTE,MONTH,MONTH│
        │                                       │NAME,PARSEDATETIME,QUARTER,SECOND,WEEK│
        │                                       │,YEAR                                 │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │catalogSeparator                       │.                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxColumnsInIndex                      │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │databaseMinorVersion                   │4                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │clientInfoProperties                   │[[Value:0, Name:numServers]]          │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │JDBCMajorVersion                       │4                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxIndexLength                         │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxStatementLength                     │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxSchemaNameLength                    │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxTablesInSelect                      │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │traceId                                │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxTableNameLength                     │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxCursorNameLength                    │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │databaseMajorVersion                   │1                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │searchStringEscape                     │\\                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │typeInfo                               │[[SUFFIX:null,   CASE_SENSITIVE:false,│
        │                                       │RADIX:null,                NULLABLE:1,│
        │                                       │UNSIGNED_ATTRIBUTE:false, MINIMUM_SCAL│
        │                                       │E:0, PRECISION:0,  SQL_DATETIME_SUB:0,│
        │                                       │AUTO_INCREMENT:false, MAXIMUM_SCALE:0,│
        │                                       │SEARCHABLE:3,             PREFIX:null,│
        │                                       │FIXED_PREC_SCALE:false,   PARAMS:null,│
        │                                       │TYPE_NAME:RESULT_SET,  DATA_TYPE:-10],│
        │                                       │[SUFFIX:null,    CASE_SENSITIVE:false,│
        │                                       │RADIX:10,                  NULLABLE:1,│
        │                                       │UNSIGNED_ATTRIBUTE:false, MINIMUM_SCAL│
        │                                       │E:0, PRECISION:3,  SQL_DATETIME_SUB:0,│
        │                                       │AUTO_INCREMENT:false, MAXIMUM_SCALE:0,│
        │                                       │SEARCHABLE:3,             PREFIX:null,│
        │                                       │FIXED_PREC_SCALE:false,   PARAMS:null,│
        │                                       │TYPE_NAME:TINYINT,      DATA_TYPE:-6],│
        │                                       │[SUFFIX:null,    CASE_SENSITIVE:false,│
        │                                       │RADIX:10,                  NULLABLE:1,│
        │                                       │UNSIGNED_ATTRIBUTE:false, MINIMUM_SCAL│
        │                                       │E:0, PRECISION:19, SQL_DATETIME_SUB:0,│
        │                                       │AUTO_INCREMENT:false, MAXIMUM_SCALE:0,│
        │                                       │SEARCHABLE:3,             PREFIX:null,│
        │                                       │FIXED_PREC_SCALE:false,   PARAMS:null,│
        │                                       │TYPE_NAME:BIGINT,       DATA_TYPE:-5],│
        │                                       │[SUFFIX:null,    CASE_SENSITIVE:false,│
        │                                       │RADIX:10,                  NULLABLE:1,│
        │                                       │UNSIGNED_ATTRIBUTE:false, MINIMUM_SCAL│
        │                                       │E:0, PRECISION:19, SQL_DATETIME_SUB:0,│
        │                                       │AUTO_INCREMENT:true,  MAXIMUM_SCALE:0,│
        │                                       │SEARCHABLE:3,             PREFIX:null,│
        │                                       │FIXED_PREC_SCALE:false,   PARAMS:null,│
        │                                       │TYPE_NAME:IDENTITY,     DATA_TYPE:-5],│
        │                                       │[SUFFIX:',       CASE_SENSITIVE:false,│
        │                                       │RADIX:null,                NULLABLE:1,│
        │                                       │UNSIGNED_ATTRIBUTE:false, MINIMUM_SCAL│
        │                                       │E:0,             PRECISION:2147483647,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:',      FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:LENGTH, TYPE_NAME:LONGVARBINARY│
        │                                       │,      DATA_TYPE:-4],       [SUFFIX:',│
        │                                       │CASE_SENSITIVE:false,      RADIX:null,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0, PRECISION:2147483647,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:',      FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:LENGTH,    TYPE_NAME:VARBINARY,│
        │                                       │DATA_TYPE:-3],              [SUFFIX:',│
        │                                       │CASE_SENSITIVE:false,      RADIX:null,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0, PRECISION:2147483647,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:',      FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:LENGTH,       TYPE_NAME:BINARY,│
        │                                       │DATA_TYPE:-2],              [SUFFIX:',│
        │                                       │CASE_SENSITIVE:false,      RADIX:null,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0, PRECISION:2147483647,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:',      FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:LENGTH,         TYPE_NAME:UUID,│
        │                                       │DATA_TYPE:-2],              [SUFFIX:',│
        │                                       │CASE_SENSITIVE:true,       RADIX:null,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0, PRECISION:2147483647,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:',      FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:LENGTH,  TYPE_NAME:LONGVARCHAR,│
        │                                       │DATA_TYPE:-1],              [SUFFIX:',│
        │                                       │CASE_SENSITIVE:true,       RADIX:null,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0, PRECISION:2147483647,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:',      FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:LENGTH,         TYPE_NAME:CHAR,│
        │                                       │DATA_TYPE:1],            [SUFFIX:null,│
        │                                       │CASE_SENSITIVE:false,        RADIX:10,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0, PRECISION:2147483647,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:null,   FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:PRECISION,SCALE, TYPE_NAME:NUME│
        │                                       │RIC,    DATA_TYPE:2],    [SUFFIX:null,│
        │                                       │CASE_SENSITIVE:false,        RADIX:10,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0, PRECISION:2147483647,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:null,   FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:PRECISION,SCALE, TYPE_NAME:DECI│
        │                                       │MAL,    DATA_TYPE:3],    [SUFFIX:null,│
        │                                       │CASE_SENSITIVE:false,        RADIX:10,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0,         PRECISION:10,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:null,   FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:null,        TYPE_NAME:INTEGER,│
        │                                       │DATA_TYPE:4],            [SUFFIX:null,│
        │                                       │CASE_SENSITIVE:false,        RADIX:10,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0,         PRECISION:10,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:tru│
        │                                       │e,   MAXIMUM_SCALE:0,    SEARCHABLE:3,│
        │                                       │PREFIX:null,   FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:null,         TYPE_NAME:SERIAL,│
        │                                       │DATA_TYPE:4],            [SUFFIX:null,│
        │                                       │CASE_SENSITIVE:false,        RADIX:10,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0,          PRECISION:5,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:null,   FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:null,       TYPE_NAME:SMALLINT,│
        │                                       │DATA_TYPE:5],            [SUFFIX:null,│
        │                                       │CASE_SENSITIVE:false,        RADIX:10,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0,         PRECISION:17,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:null,   FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:null,          TYPE_NAME:FLOAT,│
        │                                       │DATA_TYPE:6],            [SUFFIX:null,│
        │                                       │CASE_SENSITIVE:false,        RADIX:10,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0,          PRECISION:7,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:null,   FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:null,           TYPE_NAME:REAL,│
        │                                       │DATA_TYPE:7],            [SUFFIX:null,│
        │                                       │CASE_SENSITIVE:false,        RADIX:10,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0,         PRECISION:17,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:null,   FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:null,         TYPE_NAME:DOUBLE,│
        │                                       │DATA_TYPE:8],               [SUFFIX:',│
        │                                       │CASE_SENSITIVE:true,       RADIX:null,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0, PRECISION:2147483647,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:',      FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:LENGTH,      TYPE_NAME:VARCHAR,│
        │                                       │DATA_TYPE:12],              [SUFFIX:',│
        │                                       │CASE_SENSITIVE:false,      RADIX:null,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0, PRECISION:2147483647,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:',      FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:LENGTH, TYPE_NAME:VARCHAR_IGNOR│
        │                                       │ECASE,  DATA_TYPE:12],   [SUFFIX:null,│
        │                                       │CASE_SENSITIVE:false,        RADIX:10,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0,          PRECISION:1,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:null,   FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:null,        TYPE_NAME:BOOLEAN,│
        │                                       │DATA_TYPE:16],              [SUFFIX:',│
        │                                       │CASE_SENSITIVE:false,      RADIX:null,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0,          PRECISION:8,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:DATE ', FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:null,           TYPE_NAME:DATE,│
        │                                       │DATA_TYPE:91],              [SUFFIX:',│
        │                                       │CASE_SENSITIVE:false,      RADIX:null,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0,          PRECISION:6,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:TIME ', FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:null,           TYPE_NAME:TIME,│
        │                                       │DATA_TYPE:92],              [SUFFIX:',│
        │                                       │CASE_SENSITIVE:false,      RADIX:null,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0,         PRECISION:23,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,  MAXIMUM_SCALE:10,   SEARCHABLE:3,│
        │                                       │PREFIX:TIMESTAMP                    ',│
        │                                       │FIXED_PREC_SCALE:false,   PARAMS:null,│
        │                                       │TYPE_NAME:TIMESTAMP,    DATA_TYPE:93],│
        │                                       │[SUFFIX:',       CASE_SENSITIVE:false,│
        │                                       │RADIX:null,                NULLABLE:1,│
        │                                       │UNSIGNED_ATTRIBUTE:false, MINIMUM_SCAL│
        │                                       │E:0,             PRECISION:2147483647,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:',      FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:LENGTH,        TYPE_NAME:OTHER,│
        │                                       │DATA_TYPE:1111],            [SUFFIX:',│
        │                                       │CASE_SENSITIVE:false,      RADIX:null,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0, PRECISION:2147483647,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:',      FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:LENGTH,     TYPE_NAME:GEOMETRY,│
        │                                       │DATA_TYPE:1111],            [SUFFIX:',│
        │                                       │CASE_SENSITIVE:false,      RADIX:null,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0, PRECISION:2147483647,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:',      FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:LENGTH,         TYPE_NAME:ENUM,│
        │                                       │DATA_TYPE:1111],           [SUFFIX:'),│
        │                                       │CASE_SENSITIVE:false,      RADIX:null,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0,          PRECISION:0,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:(,      FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:null,          TYPE_NAME:ARRAY,│
        │                                       │DATA_TYPE:2003],            [SUFFIX:',│
        │                                       │CASE_SENSITIVE:true,       RADIX:null,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0, PRECISION:2147483647,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:',      FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:LENGTH,         TYPE_NAME:BLOB,│
        │                                       │DATA_TYPE:2004],            [SUFFIX:',│
        │                                       │CASE_SENSITIVE:true,       RADIX:null,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0, PRECISION:2147483647,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,   MAXIMUM_SCALE:0,   SEARCHABLE:3,│
        │                                       │PREFIX:',      FIXED_PREC_SCALE:false,│
        │                                       │PARAMS:LENGTH,         TYPE_NAME:CLOB,│
        │                                       │DATA_TYPE:2005],            [SUFFIX:',│
        │                                       │CASE_SENSITIVE:false,      RADIX:null,│
        │                                       │NULLABLE:1,  UNSIGNED_ATTRIBUTE:false,│
        │                                       │MINIMUM_SCALE:0,         PRECISION:30,│
        │                                       │SQL_DATETIME_SUB:0, AUTO_INCREMENT:fal│
        │                                       │se,  MAXIMUM_SCALE:10,   SEARCHABLE:3,│
        │                                       │PREFIX:TIMESTAMP_TZ                 ',│
        │                                       │FIXED_PREC_SCALE:false,   PARAMS:null,│
        │                                       │TYPE_NAME:TIMESTAMP  WITH  TIME  ZONE,│
        │                                       │DATA_TYPE:2014]]                      │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │procedureTerm                          │procedure                             │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │infoEnabled                            │false                                 │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
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
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxStatements                          │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │SQLKeywords                            │LIMIT,MINUS,ROWNUM,SYSDATE,SYSTIME,SYS│
        │                                       │TIMESTAMP,TODAY                       │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │catalogs                               │[[CATALOG_NAME:TEST]]                 │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │databaseProductVersion                 │1.4.196 (2017-06-10)                  │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │resultSetHoldability                   │2                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │JDBCMinorVersion                       │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │identifierQuoteString                  │"                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │userName                               │TEST                                  │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │connection                             │connX: url=jdbc:h2:mem:test user=TEST │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │class                                  │class org.h2.jdbc.JdbcDatabaseMetaData│
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │catalogTerm                            │catalog                               │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │debugEnabled                           │false                                 │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxCatalogNameLength                   │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │driverName                             │H2 JDBC Driver                        │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │driverMajorVersion                     │1                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxColumnsInSelect                     │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxColumnsInGroupBy                    │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxProcedureNameLength                 │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │readOnly                               │false                                 │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
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
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxUserNameLength                      │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxLogicalLobSize                      │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxColumnNameLength                    │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxRowSize                             │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │driverMinorVersion                     │4                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │databaseProductName                    │H2                                    │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │catalogAtStart                         │true                                  │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │defaultTransactionIsolation            │2                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │SQLStateType                           │2                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │schemaTerm                             │schema                                │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │rowIdLifetime                          │ROWID_UNSUPPORTED                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxColumnsInOrderBy                    │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │maxColumnsInTable                      │0                                     │
        ├───────────────────────────────────────┼──────────────────────────────────────┤
        │driverVersion                          │1.4.196 (2017-06-10)                  │
        └───────────────────────────────────────┴──────────────────────────────────────┘
            ''',
            {it.replaceFirst (/\│conn\d{1}: /, '│connX: ')},//replace dynamic data such as connection number
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

    @Test
    void testDescribeTables() throws Exception {

        checkOutput (
            '''\
       ┌──────────────────────────┬─────────────────────────┬─────────────────────────┐
       │CATALOG                   │SCHEMA                   │NAME                     │
       └──────────────────────────┴─────────────────────────┴─────────────────────────┘
            ''',
                "describe",
                "tables",
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

//    @Test
//    void testShell() throws Exception {
////        StringWriter inWriter = new StringWriter()
////        PrintStream sysIn = new PrintStream()
////        new WriterOutputStream(writer) << System.in
////        System.in = new ReaderInputStream(new StringReader('help\nquit\n'))
//        StringWriter outWriter = getOutput ("shell")
////        new WriterOutputStream(writer) << System.in
//
////        outWriter << 'help\n'
//
//        assert outWriter.toString() == '>help'
//    }

}