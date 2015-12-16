package com.bitquest.bitquest;

import com.google.gson.JsonObject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by cristian on 12/15/15.
 */
public class Wallet {
    public Wallet(String address,String privatekey) {
        this.address=address;
        this.privatekey=privatekey;
    }
    public String address;
    private String privatekey;
    int balance() throws IOException, ParseException {
        URL url = new URL("https://api.blockcypher.com/v1/btc/main/addrs/"+address+"/balance");
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        int responseCode = con.getResponseCode();
        // System.out.println("\nSending 'POST' request to URL : " + url);
        // System.out.println("Post parameters : " + urlParameters);
        // System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        JSONParser parser = new JSONParser();
        System.out.println(response.toString());
        final JSONObject jsonobj = (JSONObject) parser.parse(response.toString());
        return ((Number) jsonobj.get("final_balance")).intValue();
    }
    boolean transaction(int sat, Wallet wallet) throws IOException {
        JsonObject payload=new JsonObject();
        payload.addProperty("from_private",this.privatekey);
        payload.addProperty("to_address",wallet.address);
        payload.addProperty("value_satoshis",sat);

        URL url = new URL("https://api.blockcypher.com/v1/bcy/main/txs/micro?token="+BitQuest.BLOCKCYPHER_API_KEY);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setDoOutput(true);
        OutputStreamWriter out = new   OutputStreamWriter(con.getOutputStream());
        out.write(payload.toString());
        out.close();
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Payload : " + payload);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        if(responseCode==200) {
            return true;
        } else {
            return false;
        }
    }
}
