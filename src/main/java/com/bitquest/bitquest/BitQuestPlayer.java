package com.bitquest.bitquest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BitQuestPlayer {
  public String clan;
  public String uuid;
  private Connection conn;

  public BitQuestPlayer(Connection conn) {
    this.conn = conn;
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

  public boolean leaveClan() throws SQLException {
    String sql = "UPDATE players SET clan = NULL WHERE uuid = '" +
        uuid +
        "'";
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
}
