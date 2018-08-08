package com.bitquest.bitquest;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Base64;

public class Wallet {
    public String address;
    private String private_key;
    private String public_key;
    private String wif;

    public Wallet(String _private_key, String _public_key, String _address, String _wif) {
        this.public_key = _public_key;
        this.private_key = _private_key;
        this.address = _address;
        this.wif = _wif;
        this.private_key = _private_key;
    }

    public boolean payment(String _address,Long sat) throws IOException, ParseException {

        // inputs
        final JSONArray inputs = new JSONArray();
        final JSONArray input_addresses = new JSONArray();
        final JSONObject input = new JSONObject();
        input.put("address",this.address);
        input_addresses.add(input);
        inputs.add(input_addresses);
        input.put("inputs",inputs);

        // outputs
        final JSONArray outputs = new JSONArray();
        final JSONArray output_addresses = new JSONArray();
        final JSONObject output = new JSONObject();
        output.put("address",_address);
        output_addresses.add(output);
        outputs.add(output_addresses);
        output.put("inputs",outputs);

        // parameters to be sent to API
        final JSONObject blockcypher_params = new JSONObject();
        blockcypher_params.put("inputs", inputs);
        blockcypher_params.put("outputs", outputs);
        blockcypher_params.put("value",sat);

        // create skeleton tx to be signed
        URL url = new URL("https://api.blockcypher.com/v1/btc/test3/txs/new");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(5000);
        con.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        System.out.println(blockcypher_params.toString());
        out.write(blockcypher_params.toString());
        out.close();

        int responseCode = con.getResponseCode();

        BufferedReader in =
                new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JSONParser parser = new JSONParser();

        JSONObject response_object = (JSONObject) parser.parse(response.toString());
        return false;
    }
    public Long getBalance(int confirmations) {
        return Long.valueOf(0);
    }

    public String url() {
        if (address.substring(0, 1).equals("N") || address.substring(0, 1).equals("n")) {
            return "live.blockcypher.com/btc-testnet/address/" + address;
        }
        if (address.substring(0, 1).equals("D")) {
            return "live.blockcypher.com/doge/address/" + address;
        } else {
            return "live.blockcypher.com/btc/address/" + address;
        }
    }
}
