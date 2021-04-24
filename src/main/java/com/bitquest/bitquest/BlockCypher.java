package com.bitquest.bitquest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;


public class BlockCypher {
    public static final JSONObject api(String method, JSONObject params) throws ParseException, IOException {
        try {
            URL url = new URL("https://api.blockcypher.com/v1/" + BitQuest.BLOCKCYPHER_CHAIN+ "/" + method + "?token=" + System.getenv("BLOCKCYPHER_TOKEN"));
            System.out.println(url);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);
            con.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
            out.write(params.toString());
            System.out.println(url.toString() + " --> " + params.toString());
            out.close();

            
            int responseCode = con.getResponseCode();
            InputStream inputStream;

            if (200 <= responseCode && responseCode <= 299) {
                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());
            JSONParser parser = new JSONParser();

            return (JSONObject) parser.parse(response.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("[blockcypher] error "+method);
            throw(e);
        }
    }
}
