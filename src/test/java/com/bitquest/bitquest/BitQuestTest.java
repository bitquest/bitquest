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
    public void testWallet() throws SQLException, IOException, ParseException {
        System.out.println("full node username: "+BitQuest.BITCOIN_NODE_USERNAME);
        System.out.println("full node host: "+BitQuest.BITCOIN_NODE_HOST);
        System.out.println("full node port: "+BitQuest.BITCOIN_NODE_PORT);
        System.out.println("full node password: "+BitQuest.BITCOIN_NODE_PASSWORD);
        String test_uuid="354dc083-d9ee-4322-a7eb-3d5880918be1";

        Connection db_con = DriverManager.getConnection(BitQuest.db_url, BitQuest.db_user, BitQuest.db_password);
        Statement st = db_con.createStatement();
        ResultSet rs = st.executeQuery("SELECT VERSION()");

        if (rs.next()) {
            System.out.println(rs.getString(1));
        }
        DBMigrationCheck migration = new DBMigrationCheck(db_con);
        Wallet test_user_wallet=BitQuest.generateNewWallet();
        Wallet test_world_wallet=new Wallet("d6918becd01bfaa50a848eb346c38739fb8b9ce6d10124f8758a94498bd21bb8","03870b86fbdee06f9104ac4ece5435d4d9283366508cee764df84e7b5747605ec0","mysVa261EMDtnUYDLY54TEaVAoEbHudY8D","cUmo47gffcGAePgvLY97nwqd5y1KPTnAi5S7soVio5PYzFayBfhf");
        assertTrue(test_world_wallet.payment(test_user_wallet.address, Long.valueOf(10)));
        LegacyWallet legacy_wallet=new LegacyWallet(test_uuid);
        test_world_wallet.payment(legacy_wallet.getAccountAddress(), Long.valueOf(10000));
        System.out.println("legacy wallet balance: "+legacy_wallet.getBalance(5));

        if(legacy_wallet.getBalance(5)>0) {
            legacy_wallet.sendFrom(test_user_wallet.address,legacy_wallet.getBalance(5));
        }
    }
}
