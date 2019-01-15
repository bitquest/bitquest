package com.bitquest.bitquest;

import java.sql.*;

public class DBMigrationCheck {

  public DBMigrationCheck(Connection con) throws SQLException {
    System.out.println("[db] checking for table 'users'");
    PreparedStatement ps =
        con.prepareStatement(
            "CREATE TABLE IF NOT EXISTS users (id serial PRIMARY KEY, uuid VARCHAR(128) UNIQUE, private VARCHAR(256), public VARCHAR(256), address VARCHAR(128), wif VARCHAR(256), experience INTEGER DEFAULT 0, last_seen TIMESTAMP)");
    ps.executeUpdate();
  }
}
