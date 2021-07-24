package com.bitquest.bitquest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.Location;
import org.bukkit.World;

public class Land {
  public Connection conn;

  public Land(Connection conn) {
    this.conn = conn;
  }

  public void runMigrations() throws SQLException {
    String sql = "CREATE TABLE IF NOT EXISTS chunks (x integer, z integer, owner varchar(36), name varchar(32), permission smallint);";
    PreparedStatement ps = this.conn.prepareStatement(sql);
    System.out.println(sql);
    ps.executeUpdate();
    ps.close();
  }

  public static String landIsClaimedCacheKey(Location location, World world) {
    return  "land:claimed:" + world.getName() + ":" + location.getChunk().getX() + ":" + location.getChunk().getZ();
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
