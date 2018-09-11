package com.bitquest.bitquest;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.ECKey.ECDSASignature;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

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
    }

    JSONObject txSkeleton(String _address, Long sat) throws IOException, ParseException {
        // inputs
        final JSONArray inputs = new JSONArray();
        final JSONArray input_addresses = new JSONArray();
        final JSONObject input = new JSONObject();
        input_addresses.add(this.address);
        input.put("addresses", input_addresses);
        inputs.add(input);

        // outputs
        final JSONArray outputs = new JSONArray();
        final JSONArray output_addresses = new JSONArray();
        final JSONObject output = new JSONObject();
        output_addresses.add(_address);
        ;
        output.put("addresses", output_addresses);
        output.put("value", sat);
        outputs.add(output);

        // parameters to be sent to API
        final JSONObject blockcypher_params = new JSONObject();
        blockcypher_params.put("inputs", inputs);
        blockcypher_params.put("outputs", outputs);
        URL url = new URL("https://api.blockcypher.com/v1/" + BitQuest.BLOCKCYPHER_CHAIN + "/txs/new");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(5000);
        con.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        out.write(blockcypher_params.toString());
        System.out.println(url.toString()+" --> "+blockcypher_params.toString());
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

        return (JSONObject) parser.parse(response.toString());
    }

    public boolean payment(String _address, Long sat) throws IOException, ParseException {

        // create skeleton tx to be signed
        JSONObject tx = txSkeleton(_address, sat);
        // obtain message (hash) to be signed with private key
        JSONArray tosign = (JSONArray) tx.get("tosign");
        String msg = tosign.get(0).toString();
        // TODO: Create raw transaction with in full node
        // creating a key object from WiF
        DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(null, this.wif);
        ECKey key = dpk.getKey();
        // checking our key object
        NetworkParameters params = TestNet3Params.get();
        if (System.getenv("BITQUEST_ENV") != null) {
            if (System.getenv("BITQUEST_ENV").equalsIgnoreCase("production")) {
                System.out.println("[transaction] main net transaction start");
                params = MainNetParams.get();
            }
        }
        String check = ((org.bitcoinj.core.ECKey) key).getPrivateKeyAsWiF(params);
        // System.out.println(wif.equals(check));  // true
        // creating Sha object from string
        Sha256Hash hash = Sha256Hash.wrap(msg);
        // creating signature
        ECDSASignature sig = key.sign(hash);
        // encoding
        byte[] res = sig.encodeToDER();
        // converting to hex
        String hex = DatatypeConverter.printHexBinary(res);
        JSONArray signatures = new JSONArray();
        signatures.add(hex);
        tx.put("signatures", signatures);
        JSONArray pubkeys = new JSONArray();
        // add my public key
        pubkeys.add(this.public_key);
        tx.put("pubkeys", pubkeys);
        // go back to blockcypher with signed transaction
        URL url;
        if (System.getenv("BLOCKCYPHER_TOKEN") != null) {
            url = new URL("https://api.blockcypher.com/v1/" + BitQuest.BLOCKCYPHER_CHAIN + "/txs/send?token=" + System.getenv("BLOCKCYPHER_TOKEN"));
        } else {
            url = new URL("https://api.blockcypher.com/v1/" + BitQuest.BLOCKCYPHER_CHAIN + "/txs/send");
        }

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(5000);
        con.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        out.write(tx.toString());
        out.close();
        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JSONParser parser = new JSONParser();
        JSONObject response_object = (JSONObject) parser.parse(response.toString());
        System.out.println("[payment] " + this.address + " -> " + sat + " -> " + _address);
        return true;
    }

    public Long getBalance(int confirmations) throws IOException, ParseException {
            HttpsURLConnection c = null;
            URL u = new URL("https://api.blockcypher.com/v1/" + BitQuest.BLOCKCYPHER_CHAIN + "/addrs/" + this.address + "/balance");
            c = (HttpsURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.connect();
            int status = c.getResponseCode();
            System.out.println("[balance] status:"+status);
            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    System.out.println(sb.toString());
                    JSONParser parser = new JSONParser();
                    JSONObject response_object = (JSONObject) parser.parse(sb.toString());
                    return (Long) response_object.get("final_balance");
            }

            return Long.valueOf(0);


    }

    public String url() {
        if (address.substring(0, 1).equals("N") || address.substring(0, 1).equals("n") || address.substring(0, 1).equals("m")) {
            return "live.blockcypher.com/btc-testnet/address/" + address;
        }
        if (address.substring(0, 1).equals("D")) {
            return "live.blockcypher.com/doge/address/" + address;
        } else {
            return "live.blockcypher.com/btc/address/" + address;
        }
    }

    public boolean save(UUID uuid, Connection db_con) throws SQLException {
        PreparedStatement user_create_pst = db_con.prepareStatement("INSERT INTO USERS (uuid,private,public,address,wif) VALUES ('" + uuid.toString() + "','" + this.private_key + "','" + this.public_key + "','" + this.address + "','"+this.wif+"')");
        user_create_pst.executeUpdate();

        return true;
    }
}
