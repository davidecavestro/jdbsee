package com.github.davidecavestro.jdbsee.jdbcli.config

import com.github.davidecavestro.jdbsee.jdbcli.SqlService
import groovy.sql.GroovyRowResult
import groovy.sql.Sql;

import javax.inject.Inject
import java.io.File


public class JdbsDriverDao {
  SqlService sqlService

  @Inject
  JdbsDriverDao(SqlService sqlService){this.sqlService = sqlService}

  protected void initTable () {
    "CREATE TEXT TABLE IF NOT EXISTS jdbc_driver (ID LONG IDENTITY PRIMARY KEY, name VARCHAR NOT NULL, clazz VARCHAR, clazz_expr VARCHAR)"
    sqlService.withSql { Sql sql ->
      sql.execute(
              """\
                    CREATE TEXT TABLE IF NOT EXISTS jdbc_driver (
                      ID LONG IDENTITY PRIMARY KEY, 
                      name VARCHAR NOT NULL, 
                      clazz VARCHAR, 
                      clazz_expr VARCHAR
                    )
                  """)
    }
  }

  void insert (
          String name,
          String driverClass,
          String driverClassExpr,
          File[] jars,
          String[] deps) {
    sqlService.withSql { Sql sql->
      //persist driver info
      def keys = sql.executeInsert(
              """\
                    INSERT INTO jdbs_driver 
                      (name, clazz, clazz_expr) 
                    VALUES 
                      (:name, :clazz, :clazz_expr)
                  """, name: name, clazz: driverClass, clazzExpr: driverClassExpr)

      //persist jar paths
      jars.each { jarFile ->
        sql.executeInsert(
                """\
                    INSERT INTO jdbs_jar 
                      (path) 
                    VALUES 
                      (:path)
                  """, path: jarFile.absolutePath)
      }

      //persist deps
      deps.each { dependency ->
        sql.executeInsert(
                """\
                    INSERT INTO jdbs_dep 
                      (dep_ref) 
                    VALUES 
                      (:dep_ref)
                  """, dep_ref: dependency)
      }
    }
  }

  void delete (String... names) {
    sqlService.withSql { Sql sql ->
      sql.executeInsert(
              """\
                    DELETE FROM jdbs_dep 
                    WHERE 
                      name IN (:names)
                  """, names: names)
    }
  }

  List<JdbsDriver> listDrivers () {
    sqlService.withSql { Sql sql ->
      sql.rows('SELECT * FROM jdbs_dep').collect {row->
        rowToBean(row)
      }
    }
  }

  protected JdbsDriver rowToBean(final GroovyRowResult row) {
    new JdbsDriver(
            id: row.id,
            name: row.name,
            clazz: row.clazz,
            clazzExpr: row.clazz_expr
    )
  }
}
