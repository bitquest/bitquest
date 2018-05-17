package com.bitquest.bitquest;

import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Created by cristian on 12/15/15.
 */
public class Wallet {
    public int balance;
    public int unconfirmedBalance;
    public String path;
    public String account_id;
    public String address;

    private BitQuest bitQuest;

    public interface GetBalanceCallback {
        void run(Long balance);
    }

    public interface GetAccountAddressCallback {
        void run(String address);
    }
    public interface AddWitnessAddressCallback {
        void run(String address);
    }
    public interface SetAccountCallback {
        void run(Boolean success);
    }

    public Wallet(BitQuest plugin, String account_id) {
        this.account_id=account_id;
        this.bitQuest = plugin;
        getAccountAddress(new GetAccountAddressCallback() {
            @Override
            public void run(String accountAddress) {
                address = accountAddress;
            }
        });
    }




    public void getBalance(int confirmations, final GetBalanceCallback callback) {
        final String account_id = this.account_id;
        System.out.println(account_id);
        Bukkit.getScheduler().runTaskAsynchronously(bitQuest, new Runnable() {
            @Override
            public void run() {
                try {
                    JSONParser parser = new JSONParser();
                    final JSONObject jsonObject = new JSONObject();
                    jsonObject.put("jsonrpc", "1.0");
                    jsonObject.put("id", "bitquest");
                    jsonObject.put("method", "getbalance");
                    JSONArray params = new JSONArray();
                    params.add(account_id);
                    params.add(confirmations);
                    if(bitQuest.BITQUEST_ENV=="development")
                        System.out.println("[getbalance] "+account_id+" "+confirmations);
                    jsonObject.put("params", params);
                    URL url = new URL("http://" + BitQuest.BITCOIN_NODE_HOST + ":" + BitQuest.BITCOIN_NODE_PORT);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(5000);
                    String userPassword = BitQuest.BITCOIN_NODE_USERNAME + ":" + BitQuest.BITCOIN_NODE_PASSWORD;
                    String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
                    con.setRequestProperty("Authorization", "Basic " + encoding);

                    con.setRequestMethod("POST");
                    con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
                    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    con.setDoOutput(true);
                    OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
                    out.write(jsonObject.toString());
                    out.close();

                    int responseCode = con.getResponseCode();

                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    JSONObject response_object = (JSONObject) parser.parse(response.toString());
                    Double d = Double.parseDouble(response_object.get("result").toString().trim())*100000000L;

                    if(bitQuest.BITQUEST_ENV=="development")
                        System.out.println(response_object);
                    final Long balance = d.longValue();
                    Bukkit.getScheduler().runTask(bitQuest, new Runnable() {
                        @Override
                        public void run() {
                            callback.run(balance);
                        }
                    });
                } catch (IOException e) {
                    System.out.println("Error on getBalance");
                    e.printStackTrace();
                } catch (ParseException e) {
                    System.out.println("Error on getBalance");
                    e.printStackTrace();
                }
            }
        });

    }
    void getAccountAddress(final GetAccountAddressCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(bitQuest, new Runnable() {
            @Override
            public void run() {
                try {
                    JSONParser parser = new JSONParser();

                    final JSONObject jsonObject = new JSONObject();
                    jsonObject.put("jsonrpc", "1.0");
                    jsonObject.put("id", "bitquest");
                    jsonObject.put("method", "getaccountaddress");
                    JSONArray params = new JSONArray();
                    params.add(account_id);
                    if(bitQuest.BITQUEST_ENV=="development")
                        System.out.println("[getaccountaddress] "+account_id);
                    jsonObject.put("params", params);
                    URL url = new URL("http://" + BitQuest.BITCOIN_NODE_HOST + ":" + BitQuest.BITCOIN_NODE_PORT);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    String userPassword = BitQuest.BITCOIN_NODE_USERNAME + ":" + BitQuest.BITCOIN_NODE_PASSWORD;
                    String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
                    con.setRequestProperty("Authorization", "Basic " + encoding);
                    con.setConnectTimeout(5000);
                    con.setRequestMethod("POST");
                    con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
                    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    con.setDoOutput(true);
                    OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
                    out.write(jsonObject.toString());
                    out.close();

                    int responseCode = con.getResponseCode();

                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    JSONObject response_object = (JSONObject) parser.parse(response.toString());
                    if(bitQuest.BITQUEST_ENV=="development")
                        System.out.println(response_object);
                    callback.run(response_object.get("result").toString());
                } catch (Exception e) {
                    System.out.println("Error on getAccountAddress");
                    e.printStackTrace();
                }
            }
        });
    }

    void addWitnessAddress(String address,final AddWitnessAddressCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(bitQuest, new Runnable() {
            @Override
            public void run() {
                try {
                    JSONParser parser = new JSONParser();

                    final JSONObject jsonObject = new JSONObject();
                    jsonObject.put("jsonrpc", "1.0");
                    jsonObject.put("id", "bitquest");
                    jsonObject.put("method", "addwitnessaddress");
                    JSONArray params = new JSONArray();
                    params.add(address);

                    jsonObject.put("params", params);
                    URL url = new URL("http://" + BitQuest.BITCOIN_NODE_HOST + ":" + BitQuest.BITCOIN_NODE_PORT);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    String userPassword = BitQuest.BITCOIN_NODE_USERNAME + ":" + BitQuest.BITCOIN_NODE_PASSWORD;
                    String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
                    con.setRequestProperty("Authorization", "Basic " + encoding);
                    con.setConnectTimeout(5000);
                    con.setRequestMethod("POST");
                    con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
                    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    con.setDoOutput(true);
                    OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
                    out.write(jsonObject.toString());
                    out.close();

                    int responseCode = con.getResponseCode();

                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    JSONObject response_object = (JSONObject) parser.parse(response.toString());
                    if(bitQuest.BITQUEST_ENV=="development")
                        System.out.println(response_object);
                    callback.run(response_object.get("result").toString());
                } catch (Exception e) {
                    System.out.println("[addwitnessaddress] fail");
                    e.printStackTrace();
                }
            }
        });
    }

    void setAccount(String address,final SetAccountCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(bitQuest, new Runnable() {
            @Override
            public void run() {
                try {
                    JSONParser parser = new JSONParser();
                    final JSONObject jsonObject = new JSONObject();
                    jsonObject.put("jsonrpc", "1.0");
                    jsonObject.put("id", "bitquest");
                    jsonObject.put("method", "setaccount");
                    JSONArray params = new JSONArray();
                    params.add(address);
                    params.add(account_id);

                    jsonObject.put("params", params);
                    URL url = new URL("http://" + BitQuest.BITCOIN_NODE_HOST + ":" + BitQuest.BITCOIN_NODE_PORT);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    String userPassword = BitQuest.BITCOIN_NODE_USERNAME + ":" + BitQuest.BITCOIN_NODE_PASSWORD;
                    String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
                    con.setRequestProperty("Authorization", "Basic " + encoding);
                    con.setConnectTimeout(5000);
                    con.setRequestMethod("POST");
                    con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
                    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    con.setDoOutput(true);
                    OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
                    out.write(jsonObject.toString());
                    out.close();

                    int responseCode = con.getResponseCode();

                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    JSONObject response_object = (JSONObject) parser.parse(response.toString());
                    if(bitQuest.BITQUEST_ENV=="development")
                        System.out.println(response_object);
                    callback.run(true);
                } catch (Exception e) {
                    System.out.println("[setaccount] error");
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean move(String to,Long sat) throws IOException, ParseException {

        if(sat>=100&&sat<=Long.MAX_VALUE) {
            JSONParser parser = new JSONParser();

            final JSONObject jsonObject=new JSONObject();
            jsonObject.put("jsonrpc","1.0");
            jsonObject.put("id","bitquest");
            jsonObject.put("method","move");
            JSONArray params=new JSONArray();
            params.add(this.account_id);
            params.add(to);
            Double double_sat=new Double(sat);

            params.add(double_sat/100000000L);
            jsonObject.put("params",params);
            URL url = new URL("http://"+BitQuest.BITCOIN_NODE_HOST+":"+BitQuest.BITCOIN_NODE_PORT);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            String userPassword = BitQuest.BITCOIN_NODE_USERNAME + ":" + BitQuest.BITCOIN_NODE_PASSWORD;
            String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
            con.setRequestProperty("Authorization", "Basic " + encoding);
            con.setConnectTimeout(5000);
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
            out.write(jsonObject.toString());
            out.close();

            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            JSONObject response_object= (JSONObject) parser.parse(response.toString());
            if(bitQuest.BITQUEST_ENV=="development")
                System.out.println(response_object);
            return (boolean)response_object.get("result");
        } else {
            System.out.println("[move] "+this.account_id+"-> "+sat+" --> "+to+": FAIL (must be between 100 & "+Long.MAX_VALUE+")");
            return false;
        }
    }

    public String sendFrom(String address,Long sat) throws IOException, ParseException {
        JSONParser parser = new JSONParser();

        final JSONObject jsonObject=new JSONObject();
        jsonObject.put("jsonrpc","1.0");
        jsonObject.put("id","bitquest");
        jsonObject.put("method","sendfrom");
        JSONArray params=new JSONArray();
        params.add(this.account_id);
        params.add(address);
        System.out.println(sat);
        Double double_sat=new Double(sat);
        System.out.println(double_sat);

        params.add(double_sat/100000000L);
        System.out.println(params);
        jsonObject.put("params",params);
        System.out.println("Checking blockchain info...");
        URL url = new URL("http://"+BitQuest.BITCOIN_NODE_HOST+":"+BitQuest.BITCOIN_NODE_PORT);
        System.out.println(url.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        String userPassword = BitQuest.BITCOIN_NODE_USERNAME + ":" + BitQuest.BITCOIN_NODE_PASSWORD;
        String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
        con.setRequestProperty("Authorization", "Basic " + encoding);

        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        out.write(jsonObject.toString());
        out.close();

        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        System.out.println(response.toString());
        JSONObject response_object= (JSONObject) parser.parse(response.toString());
        System.out.println(response_object);
        return (String)response_object.get("result");
    }


    public String url() {
        if(address.substring(0,1).equals("N")||address.substring(0,1).equals("n")) {
            return "live.blockcypher.com/btc-testnet/address/" + address;
        } if(address.substring(0,1).equals("D")) {
            return "live.blockcypher.com/doge/address/"+address;
        } else {
            return "live.blockcypher.com/btc/address/"+address;
        }
    }
}
