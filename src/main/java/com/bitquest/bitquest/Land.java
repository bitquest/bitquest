package com.bitquest.bitquest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.Location;

public class Land {
  public Connection conn;

  public Land() {
    String postgresDb = System.getenv("BITQUEST_POSTGRES_DB") != null ? System.getenv("BITQUEST_POSTGRES_DB") : "bitquest";
    String postgresHost = System.getenv("BITQUEST_POSTGRES_HOST") != null ? System.getenv("BITQUEST_POSTGRES_HOST") : "postgres";
    String postgresUser = System.getenv("BITQUEST_POSTGRES_USER") != null ? System.getenv("BITQUEST_POSTGRES_USER") : "postgres";
    String postgresPassword = System.getenv("BITQUEST_POSTGRES_PASSWORD");
    String postgresPort = System.getenv("BITQUEST_POSTGRES_PORT") != null ? System.getenv("BITQUEST_POSTGRES_PORT") : "5432";
    try {
      Class.forName("org.postgresql.Driver");
      String postgresUrl = "jdbc:postgresql://" + 
          postgresHost +
          ":" + 
          postgresPort + 
          "/" + 
          postgresDb;
      BitQuest.log("db","connecting to " + postgresUrl);
      this.conn = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void runMigrations() throws SQLException {
    String sql = "CREATE TABLE IF NOT EXISTS chunks (x integer, z integer, owner varchar(36), name varchar(32), permission smallint);";
    PreparedStatement ps = this.conn.prepareStatement(sql);
    System.out.println(sql);
    ps.executeUpdate();
    ps.close();
  }

  public boolean rename(int x, int z, String name) throws SQLException {
    if (!BitQuest.validName(name)) return false;
    String sql = "UPDATE chunks SET name = '" +
        name +
        "' WHERE x = " +
        String.valueOf(x) +
        " AND z = " +
        String.valueOf(z);
    System.out.println(sql);
    PreparedStatement ps = this.conn.prepareStatement(sql);
    ps.executeUpdate();
    ps.close();
    return true;
  }

  public void changePermission(int x, int z, ChunkPermission permission) throws SQLException {
    int permissionInt = 0;
    if (permission == ChunkPermission.CLAN) permissionInt = 1;
    if (permission == ChunkPermission.PUBLIC) permissionInt = 2;
    String sql = "UPDATE chunks SET permission = " +
        String.valueOf(permissionInt) +
        " WHERE x = " +
        String.valueOf(x) +
        " AND z = " +
        String.valueOf(z);
    System.out.println(sql);
    PreparedStatement ps = this.conn.prepareStatement(sql);
    ps.executeUpdate();
    ps.close();
  }

  public LandChunk chunk(Location location) throws SQLException {
    return this.chunk((int) location.getChunk().getX(), (int) location.getChunk().getZ());
  }

  public LandChunk chunk(int x, int z) throws SQLException {
    String sql = "SELECT owner,name,permission FROM chunks WHERE x=" + String.valueOf(x) + " AND z=" + String.valueOf(z);
    System.out.println(sql);
    Statement st = this.conn.createStatement();
    ResultSet rs = st.executeQuery(sql);
    LandChunk chunk = null;
    while (rs.next()) {
      chunk = new LandChunk();
      chunk.owner = rs.getString(1);
      chunk.name = rs.getString(2);
      if (rs.getInt(3) == 2) {
        chunk.permission = ChunkPermission.PUBLIC;
      } else if (rs.getInt(3) == 1) {
        chunk.permission = ChunkPermission.CLAN;
      } else {
        chunk.permission = ChunkPermission.PRIVATE;
      }
    }
    rs.close();
    st.close();
    return chunk;
  }

  public boolean claim(int x, int z, String owner, String name) throws Exception {
    if (!BitQuest.validName(name)) throw new Exception("Invalid name");
    LandChunk chunk = this.chunk(x, z);
    if (chunk != null) throw new Exception("Land already claimed");
    String sql = "INSERT INTO chunks (x, z, owner, name, permission) VALUES (" + 
        String.valueOf(x) + 
        "," +
        String.valueOf(z) +
        ",'" +
        owner +
        "','" +
        name +
        "',0)";
    System.out.println(sql);
    PreparedStatement ps = this.conn.prepareStatement(sql);
    ps.executeUpdate();
    ps.close();
    return true;
  }

}
