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
import java.util.UUID;

import static org.junit.Assert.*;

public class BitQuestTest {

    @Test
    public void testWallet() throws SQLException, IOException, ParseException, java.text.ParseException {
        final String db_url = "jdbc:postgresql://localhost:5432/bitquest";
        
    }
}
