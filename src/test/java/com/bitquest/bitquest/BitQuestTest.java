package com.bitquest.bitquest;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import static org.junit.Assert.*;

public class BitQuestTest {

    @Test
    public void testWalletFunctionality() throws SQLException, IOException, ParseException {
        String test_uuid="354dc083-d9ee-4322-a7eb-3d5880918be1";

        Connection db_con = DriverManager.getConnection(BitQuest.db_url, BitQuest.db_user, BitQuest.db_password);
        Statement st = db_con.createStatement();
        ResultSet rs = st.executeQuery("SELECT VERSION()");

        if (rs.next()) {
            System.out.println(rs.getString(1));
        }
        DBMigrationCheck migration = new DBMigrationCheck(db_con);
        Wallet test_user_wallet=BitQuest.generateNewWallet();
        Wallet test_world_wallet=new Wallet("0859d87dc0944296de8e2fd49e911491484d802f400308ccf885c4c514815697","034cd558fb702a6ce3e1c010523a3b1d146f5ebb85535ea89488bae8ac2d5aa05f","minenaq7DrYKbWw9yUhR7DA9t16YN3JFss","cMrwAyDwRwRpQzMvB5C6aws9K6sFh748oagZuDKM9Zcpg5nVtetF");
        assertTrue(test_world_wallet.payment(test_user_wallet.address, Long.valueOf(10)));

    }
}
