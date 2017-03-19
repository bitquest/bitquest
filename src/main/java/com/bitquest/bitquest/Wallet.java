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

/**
 * Created by cristian on 12/15/15.
 */
public class Wallet {
    public int balance;
    public int unconfirmedBalance;
    public String path;
    public String public_key;
    public Wallet(String address, String path, String public_key) {
        this.address=address; this.path=path; this.public_key=public_key;
    }

    public Wallet(String address,String privatekey) {
        this.address=address;
        this.privatekey=privatekey;
    }

    public Wallet(String address) {
        this.address=address;
    }
    public String address=null;
    private String privatekey=null;
    int balance() {
        this.updateBalance();
        return this.balance;
    }
    int final_balance() throws IOException, ParseException {

        JSONObject blockcypher_balance=this.get_blockcypher_balance();
        int total_received=((Number)blockcypher_balance.get("total_received")).intValue();
        int final_balance=total_received;
        int unconfirmed_balance=((Number)blockcypher_balance.get("unconfirmed_balance")).intValue();
        if(unconfirmed_balance>0) {
            final_balance=final_balance+unconfirmed_balance;
        }
        final_balance=final_balance+this.payment_balance();

        BitQuest.REDIS.set("final_balance:"+this.address,String.valueOf(final_balance));
        return final_balance;
    }
    int payment_balance() {
        if(BitQuest.REDIS.exists("payment_balance:"+this.address)) {
            return Integer.parseInt(BitQuest.REDIS.get("payment_balance:"+this.address));
        } else {
            return 0;
        }
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
    JSONObject get_blockcypher_balance() throws IOException, ParseException {
        URL url;
        if(BitQuest.BLOCKCYPHER_API_KEY!=null) {
            url=new URL("https://api.blockcypher.com/v1/"+BitQuest.BLOCKCHAIN+"/addrs/"+address+"/balance?token="+BitQuest.BLOCKCYPHER_API_KEY);
        } else {
            url=new URL("https://api.blockcypher.com/v1/"+BitQuest.BLOCKCHAIN+"/addrs/"+address+"/balance");
        }
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
    // TODO: this method is deprecated in favour of get_blockcypher_balance
    void updateBalance() {
        try {
            if(BitQuest.BLOCKCHAIN.equals("btc/main")==true && BitQuest.BITCORE_HOST!=null) {
                this.balance=bitcore_balance(BitQuest.BITCORE_HOST,this.address,true);
                this.unconfirmedBalance=bitcore_balance(BitQuest.BITCORE_HOST,this.address,false);
            } else {
                URL url;
                if(BitQuest.BLOCKCYPHER_API_KEY!=null) {
                    url=new URL("https://api.blockcypher.com/v1/"+BitQuest.BLOCKCHAIN+"/addrs/"+address+"/balance?token="+BitQuest.BLOCKCYPHER_API_KEY);
                } else {
                    url=new URL("https://api.blockcypher.com/v1/"+BitQuest.BLOCKCHAIN+"/addrs/"+address+"/balance");
                }
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
                final JSONObject jsonobj = (JSONObject) parser.parse(response.toString());
                this.balance = ((Number) jsonobj.get("balance")).intValue();
                this.unconfirmedBalance = ((Number) jsonobj.get("unconfirmed_balance")).intValue();
            }

        } catch (IOException e) {
            System.out.println("[balance] problem updating balance for address "+address);
            System.out.println(e);
            // wallet might be new and it's not listed on the blockchain yet
        } catch (ParseException e) {
            // There is a problem with the balance API
        }

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
    boolean xapo_transaction(String token, String address, int sat) throws IOException {
            URL url = new URL("https://v2.api.xapo.com/accounts/"+this.get_xapo_primary_account_id(token)+"/transactions?to="+address+"&amount="+sat+"&currency=SAT&notes=&type=pay");
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            String inputLine="";
            try {
                System.out.println("\nSending 'POST' request to URL : " + url);
                String urlParameters  = "grant_type=client_credentials&redirect_uri=http://bitquest.co/xapo";

                byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
                int    postDataLength = postData.length;

                con.setRequestMethod("POST");
                con.setRequestProperty("Authorization", "Bearer "+token);
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
    boolean payment(int sat, String address) {
        try {
            if(this.final_balance()>=sat&&sat>100) {
                System.out.println("[payment] "+this.address+" -- "+sat+" -> "+address);
                System.out.println(BitQuest.REDIS.decrBy("payment_balance:"+this.address,sat));
                System.out.println(BitQuest.REDIS.decrBy("final_balance:"+this.address,sat));
                System.out.println(BitQuest.REDIS.incrBy("payment_balance:"+address,sat));
                System.out.println(BitQuest.REDIS.incrBy("final_balance:"+address,sat));

                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
    String sign_transaction(String tosign) throws InterruptedException, IOException {
        // Get runtime
        java.lang.Runtime rt = java.lang.Runtime.getRuntime();
        // Start a new process: UNIX command ls
        java.lang.Process p = rt.exec("/btcutils/signer/signer "+tosign+" "+BitQuest.CASHOUT_PRIVATE_KEY);
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

        try {
            for(int i=0;i<tosign.size();i++) {
                String signature=sign_transaction((String)tosign.get(i));
                signatures.add(signature);
                pubkeys.add(BitQuest.CASHOUT_PUBLIC_KEY);

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
                System.out.println(BitQuest.REDIS.decrBy("payment_balance:"+this.address,fees));
                System.out.println(BitQuest.REDIS.decrBy("final_balance:"+this.address,fees));
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
    boolean create_blockcypher_transaction(int sat, String address) throws IOException, ParseException {
        if(this.final_balance()>=sat) {


            // inputs
            JSONArray input_addresses = new JSONArray();
            input_addresses.add(BitQuest.CASHOUT_ADDRESS);
            JSONObject input = new JSONObject();
            input.put("addresses", input_addresses);
            JSONArray inputs = new JSONArray();
            inputs.add(input);
            // outputs
            JSONArray output_addresses = new JSONArray();
            output_addresses.add(address);
            JSONObject output = new JSONObject();
            output.put("addresses", output_addresses);
            output.put("value", sat);
            JSONArray outputs = new JSONArray();
            outputs.add(output);


            JSONObject payload = new JSONObject();
            payload.put("inputs", inputs);
            payload.put("outputs", outputs);
            System.out.println("Payload : " + payload.toString());

            URL url = new URL("https://api.blockcypher.com/v1/" + BitQuest.BLOCKCHAIN + "/txs/new");
            String inputLine = "";
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

            try {
                System.out.println("\nSending 'POST' request to URL : " + url);
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

                if (responseCode == 200 || responseCode == 201) {
                    BitQuest.REDIS.set("transaction:" + this.address, response.toString());
                    System.out.println(BitQuest.REDIS.get("transaction:" + this.address));
                    if (this.send_blockcypher_transaction(BitQuest.REDIS.get("transaction:" + this.address)) == true) {
                        System.out.println(BitQuest.REDIS.decrBy("payment_balance:" + this.address, sat));
                        System.out.println(BitQuest.REDIS.decrBy("final_balance:" + this.address, sat));
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } catch (IOException ioe) {
                System.err.println("IOException: " + ioe);

                InputStream error = con.getErrorStream();

                int data = error.read();
                while (data != -1) {
                    //do something with data...
                    inputLine = inputLine + (char) data;
                    data = error.read();
                }
                error.close();


                System.out.println(inputLine);


                return false;
            }
        } else {
            return false;
        }
    }
    boolean blockcypher_microtransaction(int sat, String address) throws IOException {
        JsonObject payload=new JsonObject();
        payload.addProperty("from_private",this.privatekey);
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
    boolean transaction(int sat, Wallet wallet) throws IOException {
        System.out.println("------------- tx "+this.address+" --> "+wallet.address+" -----------");
        // get xapo token
        String token=this.get_xapo_token();
        if(token!=null) {
            // get xapo accounts
            this.xapo_transaction(token,wallet.address,sat);
            return false;
        } else {
            System.out.println(" error: failed to get xapo token");
            System.out.println(" success: false");
            System.out.println("-------------------------------------------------------------");
            return false;
        }
        
    }
    boolean emailTransaction(int sat,String email) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, ParseException {
        // create payload
        JSONObject obj = new JSONObject();
        obj.put("to", email);
        obj.put("currency", "SAT");
        obj.put("amount", sat);
        obj.put("subject", "BitQuest Withdrawal");
        obj.put("timestamp", System.currentTimeMillis() / 1000L);
        obj.put("unique_request_id", "BITQUEST" + System.currentTimeMillis());
        String data = obj.toString();
        int blocksize = 16;
        Bukkit.getLogger().info("blocksize: " + blocksize);
        int pad = blocksize - (data.length() % blocksize);
        Bukkit.getLogger().info("pad: " + pad);

        for (int i = 0; i < pad; i++) {
            data = data + "\0";
        }

        Bukkit.getLogger().info("payload: " + data);
        // encrypt payload
        String key = System.getenv("XAPO_APP_KEY");
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = null;

            cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            String epayload = new String(Base64.encodeBase64(cipher.doFinal(data.getBytes())));


            // post payload
            String urlstring = "https://api.xapo.com/v1/credit/";
            String query = "hash=" + URLEncoder.encode(epayload, "UTF-8") + "&appID=" + System.getenv("XAPO_APP_ID");

            URL url = new URL(urlstring);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

            //add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            String urlParameters = query;

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            Bukkit.getLogger().info(response.toString());
            JSONParser parser = new JSONParser();
            final JSONObject jsonobj = (JSONObject) parser.parse(response.toString());
            Bukkit.getLogger().info("---------- XAPO TRANSACTION END ------------");
        return true;
    }
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
}
