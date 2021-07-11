package com.bitquest.bitquest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Population {
  public Connection conn;

  public Population(Connection conn) {
    this.conn = conn;
  }

  public BitQuestPlayer player(String playerId) throws SQLException {
    String sql = "SELECT clan, experience FROM players WHERE uuid='" +
        playerId +
        "'";
    Statement st = this.conn.createStatement();
    ResultSet rs = st.executeQuery(sql);
    BitQuestPlayer player = null;
    while (rs.next()) {
      player = new BitQuestPlayer(conn);
      player.uuid = playerId;
      if (rs.getString(1) != null) player.clan = rs.getString(1);
      player.experience = rs.getInt(2);
    }
    rs.close();
    st.close();
    if (player == null) {
      player = new BitQuestPlayer(conn);
      player.uuid = playerId;
      sql = "INSERT INTO players (uuid, experience) VALUES ('" +
          playerId +
          "',0)";
      System.out.println(sql);
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.executeUpdate();
      ps.close();
    }
    return player;
  }

  public boolean clanExists(String clanName) throws SQLException {
    String sql = "SELECT * FROM players WHERE clan='" +
        clanName +
        "'";
    System.out.println(sql);
    Statement st = this.conn.createStatement();
    ResultSet rs = st.executeQuery(sql);
    boolean found = false;
    while (rs.next()) {
      found = true;
    }
    rs.close();
    st.close();
    return found;
  }

  public boolean createClan(String uuid, String clanName) throws SQLException {
    if (!BitQuest.validName(clanName)) return false;
    if (clanExists(clanName)) return false;
    if (player(uuid).clan != null) return false;
    String sql = "UPDATE players SET clan = '" +
        clanName +
        "' WHERE uuid = '" +
        uuid +
        "'";
    System.out.println(sql);
    PreparedStatement ps = this.conn.prepareStatement(sql);
    ps.executeUpdate();
    ps.close();
    sql = "DELETE FROM clan_invites WHERE clan = '" +
        clanName +
        "'";
    System.out.println(sql);
    ps = this.conn.prepareStatement(sql);
    ps.executeUpdate();
    ps.close();
    return true;
  }

  public boolean joinClan(String playerId, String clanName) throws SQLException {
    if (!BitQuest.validName(clanName)) return false;
    if (!clanExists(clanName)) return false;
    BitQuestPlayer player = player(playerId);
    if (player.clan != null) return false;
    if (!player.invitedToClan(clanName)) return false;
    String sql = "UPDATE players SET clan = '" +
        clanName +
        "' WHERE uuid = '" +
        playerId +
        "'";
    System.out.println(sql);
    PreparedStatement ps = this.conn.prepareStatement(sql);
    ps.executeUpdate();
    ps.close();
    sql = "DELETE FROM clan_invites WHERE uuid = '" +
        playerId +
        "'";
    System.out.println(sql);
    ps = this.conn.prepareStatement(sql);
    ps.executeUpdate();
    ps.close();
    return true;
  }  

  public boolean leaveClan(String playerId) throws SQLException {
    if (player(playerId).clan == null) return false;
    String sql = "UPDATE players SET clan = NULL WHERE uuid = '" +
        playerId +
        "'";
    System.out.println(sql);
    PreparedStatement ps = this.conn.prepareStatement(sql);
    ps.executeUpdate();
    ps.close();
    return true;
  }  

  public void runMigrations() throws SQLException {
    // Create players table
    String sql = "CREATE TABLE IF NOT EXISTS players (uuid varchar(36) PRIMARY KEY, clan varchar(32), experience int NOT NULL);";
    PreparedStatement ps = this.conn.prepareStatement(sql);
    System.out.println(sql);
    ps.executeUpdate();
    ps.close();
    // Create clan_invites table
    sql = "CREATE TABLE IF NOT EXISTS clan_invites (uuid varchar(36), clan varchar(32));";
    ps = this.conn.prepareStatement(sql);
    System.out.println(sql);
    ps.executeUpdate();
    ps.close();
  }
  

}
