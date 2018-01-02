package com.bitquest.bitquest;

import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.Overridden;
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
                    params.add(0);
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
    

    
    // @todo: make this just accept the endpoint name and (optional) parameters
    public JSONObject makeBlockCypherCall(String requestedURL) {
        JSONParser parser = new JSONParser();
        
        try {
            System.out.println("Making Blockcypher API call...");
            // @todo: add support for some extra params in this method (allow passing in an optional hash/dictionary/whatever Java calls it)?
            URL url;
            if(BitQuest.BLOCKCYPHER_API_KEY!=null) {
                url = new URL(requestedURL + "?token=" + BitQuest.BLOCKCYPHER_API_KEY);

            } else {
                url = new URL(requestedURL);

            }

            System.out.println(url.toString());
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            return (JSONObject) parser.parse(response.toString());
        } catch (IOException e) {
            System.out.println("problem making API call");
            System.out.println(e);
            // Unable to call API?
        } catch (ParseException e) {
            // Bad JSON?
        }
        
        return new JSONObject(); // just give them an empty object
    }



    String get_xapo_token() throws IOException {
        URL url = new URL("https://v2.api.xapo.com/oauth2/token");
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        String inputLine="";
        try {
            System.out.println("\nSending 'POST' request to URL : " + url);
            String key_secret=BitQuest.XAPO_API_KEY+":"+BitQuest.XAPO_SECRET;
            System.out.println(" key_secret: "+key_secret);
            String base64_key_secret=Base64.encodeBase64String(key_secret.getBytes());
            System.out.println(" base64_key_secret: "+base64_key_secret);

            String urlParameters  = "grant_type=client_credentials&redirect_uri=http://bitquest.co/xapo";

            byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
            int    postDataLength = postData.length;

            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Basic "+base64_key_secret);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("charset", "utf-8");
            con.setRequestProperty("Content-Length", Integer.toString(postDataLength));

            con.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
            out.write(urlParameters);
            out.close();
            int responseCode = con.getResponseCode();

            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            if (responseCode == 200||responseCode==201) {
                JSONParser parser = new JSONParser();

                final JSONObject jsonobj;
                try {
                    jsonobj = (JSONObject) parser.parse(response.toString());
                    String access_token=(String) jsonobj.get("access_token");
                    System.out.println(" access_token: "+access_token);
                    return access_token;
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                    return null;
                }
                // return true;
            } else {
                return null;
            }
        } catch(IOException ioe) {
            System.err.println("IOException: " + ioe);

            InputStream error = con.getErrorStream();

            int data = error.read();
            while (data != -1) {
                //do something with data...
                inputLine = inputLine + (char)data;
                data = error.read();
            }
            error.close();


            System.out.println(inputLine);


            return null;
        }
    }
    boolean xapo_transaction(String token, String email, int sat) throws IOException {
            URL url = new URL("https://v2.api.xapo.com/accounts/"+this.get_xapo_primary_account_id(token)+"/transactions?to="+email+"&amount="+sat+"&currency=SAT&notes=&type=pay");
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            String inputLine="";
            try {
                System.out.println("\nSending 'POST' request to URL : " + url);
                String urlParameters  = "grant_type=client_credentials&redirect_uri=http://bitquest.co/xapo";

                byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
                int    postDataLength = postData.length;

                con.setRequestMethod("POST");
                String authorization_header="Bearer "+token;
                System.out.println("Authorization: "+authorization_header);
                con.setRequestProperty("Authorization", authorization_header);
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setRequestProperty("charset", "utf-8");
                con.setRequestProperty("Content-Length", Integer.toString(postDataLength));

                con.setDoOutput(true);
                OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
                out.write(urlParameters);
                out.close();
                int responseCode = con.getResponseCode();

                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                if (responseCode == 200||responseCode==201) {
                    JSONParser parser = new JSONParser();

                    final JSONObject jsonobj;
                    try {
                        jsonobj = (JSONObject) parser.parse(response.toString());
                        System.out.println(jsonobj);
                        return false;
                    } catch (org.json.simple.parser.ParseException e) {
                        e.printStackTrace();
                        return false;
                    }
                    // return true;
                } else {
                    return false;
                }
            } catch(IOException ioe) {
                System.err.println("IOException: " + ioe);

                InputStream error = con.getErrorStream();

                int data = error.read();
                while (data != -1) {
                    //do something with data...
                    inputLine = inputLine + (char)data;
                    data = error.read();
                }
                error.close();


                System.out.println(inputLine);


                return false;
            }


    }
    int get_xapo_primary_account_id(String token) throws IOException {
        JSONArray accounts=get_xapo_accounts(token);
        int account_id=0;
        for(int i=0;i<accounts.size();i++) {
            JSONObject account= (JSONObject) accounts.get(i);
            System.out.println(account);
            boolean is_primary=(boolean) account.get("is_primary");
            if(is_primary==true) {
                account_id=((Long) account.get("id")).intValue();
            }
        }
        return account_id;
    }
    JSONArray get_xapo_accounts(String token) throws IOException {
        URL url = new URL("https://v2.api.xapo.com/accounts");
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        String inputLine="";
        try {
            System.out.println(" url : " + url);

            con.setRequestProperty("Authorization", "Bearer "+token);
            int responseCode = con.getResponseCode();

            System.out.println(" response code: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            if (responseCode == 200||responseCode==201) {
                JSONParser parser = new JSONParser();

                final JSONArray jsonarray;
                try {
                    // String access_token=(String) jsonobj.get("access_token");
                    return (JSONArray) parser.parse(response.toString());
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                return null;
            }
        } catch(IOException ioe) {
            System.err.println("IOException: " + ioe);

            InputStream error = con.getErrorStream();

            int data = error.read();
            while (data != -1) {
                //do something with data...
                inputLine = inputLine + (char)data;
                data = error.read();
            }
            error.close();


            System.out.println(inputLine);

            return null;
        }
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
            return "live.blockcypher.com/btc-testnet/address/"+address;
        } else {
            return "live.blockcypher.com/btc/address/"+address;
        }
    }
}
