package com.github.davidecavestro.jdbsee.jdbcli.config;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

/**
 * Driver configuration persistence based on hsql text tables
 * that is configuration stored on csv files and accessed
 * via hsql.
 *
 * http://hsqldb.org/doc/guide/texttables-chapt.html
 */
public interface DriverDao {

  /**
   * Creates the TEXT table
   */
  @SqlUpdate("CREATE TEXT TABLE IF NOT EXISTS jdbs_driver (" +
      "id INTEGER IDENTITY PRIMARY KEY, " +
      "name VARCHAR NOT NULL UNIQUE, " +
      "clazz VARCHAR, " +
      "clazz_expr VARCHAR)")
  void createTable();

  /**
   * Attaches the table to a csv file
   * @param path the csv file path
   */
  @SqlUpdate("SET TABLE jdbs_driver SOURCE :path")
  void attachTable(@Bind("path") String path);

  @SqlUpdate(
      "INSERT INTO jdbs_driver " +
          "(id, name, clazz, clazz_expr) " +
      "VALUES " +
          "(:id, :name, :clazz, :clazz_expr)")
  void insert(
      @Bind("name") String name,
      @Bind("clazz") String clazz,
      @Bind("clazz_expr") String clazzExpr
      );


  @SqlUpdate(
      "UPDATE jdbs_driver SET" +
          "(name=:name, clazz=:clazz, clazz_expr=:clazz_expr) " +
      "WHERE id=:id ")
  void update(
      @Bind("id") int id,
      @Bind("name") String name,
      @Bind("clazz") String clazz,
      @Bind("clazz_expr") String clazzExpr
  );

  @SqlQuery("SELECT * FROM jdbs_driver WHERE name=:name")
  @RegisterRowMapper (JdbsDriverRowMapper.class)
  JdbsDriver findDriverByName(@Bind("name") String name);

  @SqlQuery("SELECT * FROM jdbs_driver ORDER BY name")
  @RegisterRowMapper (JdbsDriverRowMapper.class)
  List<JdbsDriver> listDrivers();
}
