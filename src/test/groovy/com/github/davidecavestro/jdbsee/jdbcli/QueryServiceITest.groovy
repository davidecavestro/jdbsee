package com.github.davidecavestro.jdbsee.jdbcli

import com.google.common.collect.Iterables
import org.jdbi.v3.core.result.ResultIterable
import org.jdbi.v3.core.statement.Query
import org.junit.Test

public class QueryServiceITest {

    @Test
    public void execute() throws Exception {
        new QueryService()
                .execute("jdbc:hsqldb:mem:test", "", "",
                new VoidQueryCallback<Exception>() {
                    @Override
                    protected Void doWithQuery(final Query query) throws Exception {
                        final ResultIterable<Map<String, Object>> results = query.mapToMap();
                        final List<Map<String, Object>> expected = [
                                [id: 1, name: 'Alice'],
                                [id: 2, name: 'Bob']
                        ];
                        assert Iterables.elementsEqual(expected, results)
                    }
                },
                "create table contacts (id int primary key, name varchar(100))",
                "insert into contacts (id, name) values (1, 'Alice')",
                "insert into contacts (id, name) values (2, 'Bob')",
                "SELECT * FROM contacts");
    }
}