package com.bitquest.bitquest;

import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.SystemUtils;
import org.bukkit.Bukkit;
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
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Created by cristian on 12/15/15.
 */
public class Wallet {
    public int balance;
    public int confirmedBalance;
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
    void updateBalance() {
        try {
            System.out.println("updating balance...");
            URL url = new URL("https://api.blockcypher.com/v1/btc/main/addrs/"+address+"/balance?token=" + BitQuest.BLOCKCYPHER_API_KEY);

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
            this.balance = ((Number) jsonobj.get("final_balance")).intValue();
            this.confirmedBalance = ((Number) jsonobj.get("balance")).intValue();
        } catch (IOException e) {
            System.out.println("problem updating balance");
            System.out.println(e);
            // wallet might be new and it's not listed on the blockchain yet
        } catch (ParseException e) {
            // There is a problem with the balance API
        }

    }
    boolean transaction(int sat, Wallet wallet) throws IOException {
        JsonObject payload=new JsonObject();
        payload.addProperty("from_private",this.privatekey);
        payload.addProperty("to_address",wallet.address);
        payload.addProperty("value_satoshis",sat);
        URL url = new URL("https://api.blockcypher.com/v1/btc/main/txs/micro?token=" + BitQuest.BLOCKCYPHER_API_KEY);
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

}
