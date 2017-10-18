package com.bitquest.bitquest;

import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
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

    public Wallet(String account_id) throws IOException, ParseException {
        this.account_id=account_id;
        this.address=this.getAccountAddress();

    }



    public int legacy_wallet_balance(String address) throws IOException, ParseException {
        int total_received;
        int unconfirmed_balance;

        JSONObject blockcypher_balance=this.get_blockcypher_balance(address);
        total_received=((Number)blockcypher_balance.get("total_received")).intValue();


        int final_balance=total_received;

        final_balance=final_balance+this.payment_balance(address);

        return final_balance;
    }
    int payment_balance(String address) throws IOException, ParseException {
        if(BitQuest.REDIS.exists("payment_balance:"+address)) {
            return Integer.parseInt(BitQuest.REDIS.get("payment_balance:"+address));
        } else {
            return 0;
        }
    }
    public Long getBalance(int confirmations) throws IOException, ParseException {
        JSONParser parser = new JSONParser();

        final JSONObject jsonObject=new JSONObject();
        jsonObject.put("jsonrpc","1.0");
        jsonObject.put("id","bitquest");
        jsonObject.put("method","getbalance");
        JSONArray params=new JSONArray();
        params.add(this.account_id);
        params.add(0);
        jsonObject.put("params",params);
        URL url = new URL("http://"+BitQuest.BITCOIN_NODE_HOST+":"+BitQuest.BITCOIN_NODE_PORT);
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
        JSONObject response_object= (JSONObject) parser.parse(response.toString());
        return new Double((double)response_object.get("result")*100000000).longValue();

    }
    String getAccountAddress() throws IOException, ParseException {
        JSONParser parser = new JSONParser();

        final JSONObject jsonObject=new JSONObject();
        jsonObject.put("jsonrpc","1.0");
        jsonObject.put("id","bitquest");
        jsonObject.put("method","getaccountaddress");
        JSONArray params=new JSONArray();
        params.add(this.account_id);
        System.out.println(params);

        jsonObject.put("params",params);
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
    JSONObject get_bitcore_balance() throws IOException, ParseException {

        System.out.println("[balance] "+this.getAccountAddress());
        URL url;
        url=new URL(BitQuest.BITCORE_HOST+"/insight-api/addr/"+getAccountAddress());
        System.out.println(url.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(response.toString());
    }


    
    public int getBlockchainHeight() {
        JSONObject jsonobj = this.makeBlockCypherCall("https://api.blockcypher.com/v1/"+BitQuest.BLOCKCHAIN);
        return ((Number) jsonobj.get("height")).intValue();
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
    int bitcore_balance(String host, String address, boolean confirmed) throws IOException {
        URL url;
        if(confirmed==true) {
            url=new URL("http://"+host+"/insight-api/addr/"+address+"/balance");
        } else {
            url=new URL("http://"+host+"/insight-api/addr/"+address+"/unconfirmedBalance");
        }

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
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

        return Integer.parseInt(response.toString());

    }
    public JSONObject get_blockcypher_balance(String address) throws IOException, ParseException {
        System.out.println("[balance] "+address);
        URL url;
        url=new URL("https://api.blockcypher.com/v1/"+BitQuest.BLOCKCHAIN+"/addrs/"+address+"/balance");
        System.out.println(url.toString());
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(response.toString());
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
    public boolean payment(int sat, String to) {
//        try {
//            if(this.getBalance()>=sat&&sat>=100) {
//
//                System.out.println("[payment] "+this.getBalance()+" -- "+sat+" -> "+address);
//                System.out.println(BitQuest.REDIS.decrBy("payment_balance:"+this.getAccountAddress(),sat));
//                System.out.println(BitQuest.REDIS.decrBy("final_balance:"+this.getAccountAddress(),sat));
//                System.out.println(BitQuest.REDIS.incrBy("payment_balance:"+address,sat));
//                System.out.println(BitQuest.REDIS.incrBy("final_balance:"+address,sat));
//
//                return this.move(to,sat);
//            } else {
//                return false;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        } catch (ParseException e) {
//            e.printStackTrace();
//            return false;
//        }
        return false;
    }
    public boolean move(String to,int sat) throws IOException, ParseException {
        int MAX_MOVE=10000000;

        if(sat>=100&&sat<=MAX_MOVE) {
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
            return (boolean)response_object.get("result");
        } else {
            System.out.println("[move] "+this.account_id+"-> "+sat+" --> "+to+": FAIL (must be between 100 & "+MAX_MOVE+")");
            return false;
        }
    }

    public String sendFrom(String address,int sat) throws IOException, ParseException {
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
    String sign_transaction(String tosign) throws InterruptedException, IOException {
        // Get runtime
        java.lang.Runtime rt = java.lang.Runtime.getRuntime();
        // Start a new process: UNIX command ls
        java.lang.Process p = rt.exec("/btcutils/signer/signer "+tosign+" "+BitQuest.WORLD_PRIVATE_KEY);
        // You can or maybe should wait for the process to complete
        p.waitFor();
        // System.out.println("Process exited with code = " + rt.exitValue());
        // Get process' output: its InputStream
        java.io.InputStream is = p.getInputStream();
        java.io.BufferedReader reader = new java.io.BufferedReader(new InputStreamReader(is));
        // And print each line
        String s = null;
        StringBuffer signature = new StringBuffer();
        while ((s = reader.readLine()) != null) {
            System.out.println(s);
            signature.append(s);
        }
        is.close();
        System.out.println(signature.toString());
        return signature.toString();
    }
    boolean send_blockcypher_transaction(String json) throws IOException {
        JSONParser parser = new JSONParser();

        final JSONObject jsonObject;
        try {
            // String access_token=(String) jsonobj.get("access_token");
            jsonObject= (JSONObject) parser.parse(json);
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
            return false;
        }
        int fees=((Number)((JSONObject)jsonObject.get("tx")).get("fees")).intValue();
        System.out.println("fees: "+fees);
        System.out.println(jsonObject);
        JSONArray tosign=(JSONArray) jsonObject.get("tosign");
        System.out.println(tosign);
        JSONArray signatures=new JSONArray();
        JSONArray pubkeys=new JSONArray();
        if(BitQuest.HD_ROOT_ADDRESS!=null) {
            System.out.println(BitQuest.REDIS.decrBy("payment_balance:"+BitQuest.HD_ROOT_ADDRESS,fees));
            System.out.println(BitQuest.REDIS.decrBy("final_balance:"+BitQuest.HD_ROOT_ADDRESS,fees));
        } else {
            System.out.println(BitQuest.REDIS.decrBy("payment_balance:"+BitQuest.WORLD_ADDRESS,fees));
            System.out.println(BitQuest.REDIS.decrBy("final_balance:"+BitQuest.WORLD_ADDRESS,fees));
        }

        try {
            for(int i=0;i<tosign.size();i++) {
                String signature=sign_transaction((String)tosign.get(i));
                signatures.add(signature);
                pubkeys.add(BitQuest.WORLD_PUBLIC_KEY);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();

            return false;
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }
        jsonObject.put("signatures",signatures);
        jsonObject.put("pubkeys",pubkeys);
        System.out.println(jsonObject);


        URL url = new URL("https://api.blockcypher.com/v1/"+BitQuest.BLOCKCHAIN+"/txs/send?token=" + BitQuest.BLOCKCYPHER_API_KEY);

        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        String inputLine = "";

        try {
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Payload : " + jsonObject.toString());
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
            out.write(jsonObject.toString());
            out.close();
            int responseCode = con.getResponseCode();

            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            if (responseCode == 200||responseCode==201) {
//                if(BitQuest.HD_ROOT_ADDRESS!=null) {
//                    System.out.println(BitQuest.REDIS.decrBy("payment_balance:"+BitQuest.HD_ROOT_ADDRESS,fees));
//                    System.out.println(BitQuest.REDIS.decrBy("final_balance:"+BitQuest.HD_ROOT_ADDRESS,fees));
//                } else {
//                    System.out.println(BitQuest.REDIS.decrBy("payment_balance:"+BitQuest.WORLD_ADDRESS,fees));
//                    System.out.println(BitQuest.REDIS.decrBy("final_balance:"+BitQuest.WORLD_ADDRESS,fees));
//                }

                return true;
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
    public boolean blockcypher_microtransaction(int sat, String address) throws IOException {
        JsonObject payload=new JsonObject();
        // payload.addProperty("from_private",this.privatekey);
        payload.addProperty("to_address",address);
        payload.addProperty("value_satoshis",sat);
        URL url = new URL("https://api.blockcypher.com/v1/"+BitQuest.BLOCKCHAIN+"/txs/micro?token=" + BitQuest.BLOCKCYPHER_API_KEY);
        String inputLine = "";
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

        try {
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Payload : " + payload.toString());
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
            out.write(payload.toString());
            out.close();
            int responseCode = con.getResponseCode();

            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            if (responseCode == 200||responseCode==201) {
                return true;
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
    public boolean email_transaction(int sat, String email) throws IOException {
        System.out.println("------------- tx "+this.address+" --> "+email+" -----------");
        // get xapo token
        String token=this.get_xapo_token();
        if(token!=null) {
            // get xapo accounts
            return this.xapo_transaction(token,email,sat);
        } else {
            System.out.println(" error: failed to get xapo token");
            System.out.println(" success: false");
            System.out.println("-------------------------------------------------------------");
            return false;
        }
        
    }
//    boolean emailTransaction(int sat,String email) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, ParseException {
//        // create payload
//        JSONObject obj = new JSONObject();
//        obj.put("to", email);
//        obj.put("currency", "SAT");
//        obj.put("amount", sat);
//        obj.put("subject", "BitQuest Withdrawal");
//        obj.put("timestamp", System.currentTimeMillis() / 1000L);
//        obj.put("unique_request_id", "BITQUEST" + System.currentTimeMillis());
//        String data = obj.toString();
//        int blocksize = 16;
//        Bukkit.getLogger().info("blocksize: " + blocksize);
//        int pad = blocksize - (data.length() % blocksize);
//        Bukkit.getLogger().info("pad: " + pad);
//
//        for (int i = 0; i < pad; i++) {
//            data = data + "\0";
//        }
//
//        Bukkit.getLogger().info("payload: " + data);
//        // encrypt payload
//        String key = System.getenv("XAPO_APP_KEY");
//        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
//        Cipher cipher = null;
//
//            cipher = Cipher.getInstance("AES/ECB/NoPadding");
//            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
//            String epayload = new String(Base64.encodeBase64(cipher.doFinal(data.getBytes())));
//
//
//            // post payload
//            String urlstring = "https://api.xapo.com/v1/credit/";
//            String query = "hash=" + URLEncoder.encode(epayload, "UTF-8") + "&appID=" + System.getenv("XAPO_APP_ID");
//
//            URL url = new URL(urlstring);
//            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
//
//            //add reuqest header
//            con.setRequestMethod("POST");
//            con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
//            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
//
//            String urlParameters = query;
//
//            // Send post request
//            con.setDoOutput(true);
//            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//            wr.writeBytes(urlParameters);
//            wr.flush();
//            wr.close();
//
//            int responseCode = con.getResponseCode();
//
//            BufferedReader in = new BufferedReader(
//                    new InputStreamReader(con.getInputStream()));
//            String inputLine;
//            StringBuffer response = new StringBuffer();
//
//            while ((inputLine = in.readLine()) != null) {
//                response.append(inputLine);
//            }
//            in.close();
//
//            //print result
//            Bukkit.getLogger().info(response.toString());
//            JSONParser parser = new JSONParser();
//            final JSONObject jsonobj = (JSONObject) parser.parse(response.toString());
//            Bukkit.getLogger().info("---------- XAPO TRANSACTION END ------------");
//        return true;
//    }
    public boolean getTestnetCoins() {

//
//        # Fund prior address with faucet
//        curl -d '{"address": "CFqoZmZ3ePwK5wnkhxJjJAQKJ82C7RJdmd", "amount": 100000}' https://api.blockcypher.com/v1/bcy/test/faucet?token=$YOURTOKEN
//        {
//            "tx_ref": "02dbf5585d438a1cba82a9041dd815635a6b0df684225cb5271e11397a759479"
//        }

        System.out.println("Getting testnet coins from faucet...");
        JsonObject payload=new JsonObject();
        payload.addProperty("address",this.address);
        payload.addProperty("amount",100000);
        URL url = null;
        try {
            url = new URL("https://api.blockcypher.com/v1/bcy/test/faucet?token=" + BitQuest.BLOCKCYPHER_API_KEY);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String inputLine = "";
        HttpsURLConnection con = null;
        try {
            con = (HttpsURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Payload : " + payload.toString());
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
            out.write(payload.toString());
            out.close();
            int responseCode = con.getResponseCode();

            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
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

        } catch(IOException ioe) {
            System.err.println("IOException: " + ioe);

            InputStream error = con.getErrorStream();

            int data = 0;
            try {
                data = error.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (data != -1) {
                //do something with data...
                inputLine = inputLine + (char)data;
                try {
                    data = error.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                error.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            System.out.println(inputLine);
            return false;

        }
    }
    public String url() {
        if(BitQuest.BLOCKCHAIN.equals("btc/main")) {
            return "live.blockcypher.com/btc/address/"+address;
        } else if(BitQuest.BLOCKCHAIN.equals("doge/main")) {
            return "live.blockcypher.com/doge/address/"+address;
        } else if(BitQuest.BLOCKCHAIN.equals("btc/test3")) {
            return "live.blockcypher.com/btc-testnet/address/"+address;
        } else {
            return null;
        }
    }
}
