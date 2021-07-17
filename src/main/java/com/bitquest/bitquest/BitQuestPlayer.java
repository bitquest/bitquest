package com.bitquest.bitquest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BitQuestPlayer {
  public String clan;
  public String uuid;
  public int experience;
  private Connection conn;

  public BitQuestPlayer(Connection conn, String uuid) throws SQLException {
    this.conn = conn;
    this.uuid = uuid;
    String sql = "SELECT clan, experience FROM players WHERE uuid='" +
        this.uuid +
        "'";
    Statement st = this.conn.createStatement();
    ResultSet rs = st.executeQuery(sql);
    if(rs.next() == false) {
      sql = "INSERT INTO players (uuid, experience) VALUES ('" +
          this.uuid +
          "',0)";
      System.out.println(sql);
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.executeUpdate();
      ps.close();
    } else {
      this.clan = rs.getString(1);
      this.experience = rs.getInt(2);
    }
    rs.close();
    st.close();
  }

  public boolean createClan(String clanName) throws SQLException {
    if (!BitQuest.validName(clanName)) return false;
    if (clanExists(clanName)) return false;
    if (clan != null) return false;
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

  public boolean joinClan(String clanName) throws SQLException {
    if (!BitQuest.validName(clanName)) return false;
    if (!clanExists(clanName)) return false;
    if (clan != null) return false;
    if (!invitedToClan(clanName)) return false;
    String sql = "UPDATE players SET clan = '" +
        clanName +
        "' WHERE uuid = '" +
        uuid +
        "'";
    System.out.println(sql);
    PreparedStatement ps = this.conn.prepareStatement(sql);
    ps.executeUpdate();
    ps.close();
    sql = "DELETE FROM clan_invites WHERE uuid = '" +
        uuid +
        "'";
    System.out.println(sql);
    ps = this.conn.prepareStatement(sql);
    ps.executeUpdate();
    ps.close();
    return true;
  }  

  public boolean leaveClan() throws SQLException {
    if (clan == null) return false;
    String sql = "UPDATE players SET clan = NULL WHERE uuid = '" +
        uuid +
        "'";
    System.out.println(sql);
    PreparedStatement ps = this.conn.prepareStatement(sql);
    ps.executeUpdate();
    ps.close();
    return true;
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

  public final static void runMigrations(Connection conn) throws SQLException {
    // Create players table
    String sql = "CREATE TABLE IF NOT EXISTS players (uuid varchar(36) PRIMARY KEY, clan varchar(32), experience int NOT NULL);";
    PreparedStatement ps = conn.prepareStatement(sql);
    System.out.println(sql);
    ps.executeUpdate();
    ps.close();
    // Create clan_invites table
    sql = "CREATE TABLE IF NOT EXISTS clan_invites (uuid varchar(36), clan varchar(32));";
    ps = conn.prepareStatement(sql);
    System.out.println(sql);
    ps.executeUpdate();
    ps.close();
  }

  public boolean inviteToClan(BitQuestPlayer invitedPlayer) throws SQLException {
    if (clan == null) return false;
    if (invitedPlayer.clan != null) return false;
    if (invitedPlayer.uuid == null) return false;
    if (invitedPlayer.invitedToClan(clan)) return true;
    String sql = "INSERT INTO clan_invites (uuid, clan) VALUES ('" +
        invitedPlayer.uuid +
        "','" +
        clan +
        "')";
    System.out.println(sql);
    PreparedStatement ps = this.conn.prepareStatement(sql);
    ps.executeUpdate();
    ps.close();
    return true;
  }

  public boolean invitedToClan(String clanName) throws SQLException {
    String sql = "SELECT * FROM clan_invites WHERE uuid = '" +
        uuid + 
        "' AND clan = '" +
        clanName +
        "'";
    boolean exists = false;
    Statement st = this.conn.createStatement();
    ResultSet rs = st.executeQuery(sql);
    System.out.println(sql);
    while (rs.next()) {
      exists = true;
    }
    rs.close();
    st.close();
    return exists;
  }

  public boolean addExperience(int points) throws SQLException {
    String sql = "UPDATE players SET experience = experience + 1 WHERE uuid = '" +
        uuid +
        "'";
    System.out.println(sql);
    PreparedStatement ps = this.conn.prepareStatement(sql);
    ps.executeUpdate();
    ps.close();
    return true;
  }

  // public int experience() throws SQLException {
  //   String sql = "SELECT experience FROM players WHERE uuid = '" +
  //       uuid +
  //       "'";
  //   Statement st = this.conn.createStatement();
  //   ResultSet rs = st.executeQuery(sql);
  //   System.out.println(sql);
  //   rs.next();
  //   int experience = rs.getInt(1);
  //   rs.close();
  //   st.close();
  //   return experience;
  // }
}
