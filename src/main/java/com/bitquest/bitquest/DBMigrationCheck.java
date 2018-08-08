package com.bitquest.bitquest;

import java.sql.*;

public class DBMigrationCheck {

    public DBMigrationCheck(Connection con) throws SQLException {
        System.out.println("[db] checking for table 'users'");
        PreparedStatement ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS users (id serial PRIMARY KEY, uuid VARCHAR(64) UNIQUE, private VARCHAR(128), public VARCHAR(64), address VARCHAR(128), wif VARCHAR(128))");
        ps.executeUpdate();
    }
}