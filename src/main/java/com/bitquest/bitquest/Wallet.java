package com.bitquest.bitquest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.DatatypeConverter;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.ECKey.ECDSASignature;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Wallet {
  public String address;
  private String privateKey;
  private String publicKey;
  private String wif;

  public Wallet(String privateKey, String publicKey, String address, String wif) {
    this.publicKey = publicKey;
    this.privateKey = privateKey;
    this.address = address;
    this.wif = wif;
  }

  JSONObject txSkeleton(String outputAddress, Long sat) throws IOException, ParseException {
    // inputs
    final JSONArray inputs = new JSONArray();
    final JSONArray input_addresses = new JSONArray();
    final JSONObject input = new JSONObject();
    input_addresses.add(address);
    input.put("addresses", input_addresses);
    inputs.add(input);

    // outputs
    final JSONArray outputs = new JSONArray();
    final JSONArray output_addresses = new JSONArray();
    final JSONObject output = new JSONObject();
    output_addresses.add(outputAddress);
    ;
    output.put("addresses", output_addresses);
    output.put("value", sat);
    outputs.add(output);

    // parameters to be sent to API
    final JSONObject blockcypher_params = new JSONObject();
    blockcypher_params.put("inputs", inputs);
    blockcypher_params.put("outputs", outputs);
    blockcypher_params.put("fees", BitQuest.MINER_FEE);

    URL url = new URL("https://api.blockcypher.com/v1/" + BitQuest.BLOCKCYPHER_CHAIN + "/txs/new");
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setConnectTimeout(5000);
    con.setDoOutput(true);
    OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
    out.write(blockcypher_params.toString());
    System.out.println(url.toString() + " --> " + blockcypher_params.toString());
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

    return (JSONObject) parser.parse(response.toString());
  }

  public boolean payment(String toAddress, Long sat) {
    System.out.println("[payment] " + this.address + " -> " + sat);
    if (sat > 1) {
      try {
        // create skeleton tx to be signed
        JSONObject tx = this.txSkeleton(toAddress, sat);
        // obtain message (hash) to be signed with private key
        JSONArray tosign = (JSONArray) tx.get("tosign");
        JSONArray signatures = new JSONArray();
        JSONArray pubkeys = new JSONArray();

        // sign every output
        for (int i = 0; i < tosign.size(); i++) {
          String msg = tosign.get(i).toString();
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
          signatures.add(hex);
          // add my public key
          pubkeys.add(this.publicKey);
        }

        tx.put("signatures", signatures);
        tx.put("pubkeys", pubkeys);
        // go back to blockcypher with signed transaction
        URL url;
        if (System.getenv("BLOCKCYPHER_TOKEN") != null) {
          url =
              new URL(
                  "https://api.blockcypher.com/v1/"
                      + BitQuest.BLOCKCYPHER_CHAIN
                      + "/txs/send?token="
                      + System.getenv("BLOCKCYPHER_TOKEN"));
        } else {
          url =
              new URL("https://api.blockcypher.com/v1/" + BitQuest.BLOCKCYPHER_CHAIN + "/txs/send");
        }

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(5000);
        con.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        out.write(tx.toString());
        System.out.println(tx.toString());
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
        JSONObject responseObject = (JSONObject) parser.parse(response.toString());
        System.out.println("[payment] " + this.address + " -> " + sat + " -> " + toAddress);
        return true;
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }
    } else {
      return false;
    }

  }

  public Long importAddress(String account) throws IOException, ParseException {
    final JSONObject jsonObject = new JSONObject();
    jsonObject.put("jsonrpc", "1.0");
    jsonObject.put("id", "bitquest");
    jsonObject.put("method", "importaddress");
    JSONArray params = new JSONArray();
    params.add(this.address);
    params.add(account);
    System.out.println("[importaddress] " + this.address + " " + account);
    jsonObject.put("params", params);
    URL url =
        new URL(
            "http://"
                + System.getenv("BITCOIND_PORT_8332_TCP_ADDR")
                + ":"
                + System.getenv("BITCOIND_PORT_8332_TCP_PORT"));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setConnectTimeout(5000);
    String userPassword =
        System.getenv("BITCOIND_ENV_USERNAME") + ":" + System.getenv("BITCOIND_ENV_PASSWORD");
    String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
    con.setRequestProperty("Authorization", "Basic " + encoding);

    con.setRequestMethod("POST");
    con.setRequestProperty(
        "User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    con.setDoOutput(true);
    OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
    out.write(jsonObject.toString());
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
    JSONObject responseObject = (JSONObject) parser.parse(response.toString());
    return Long.valueOf(0);
  }

  public Long getBalance(int confirmations) throws IOException, ParseException {
    HttpsURLConnection c = null;
    URL u = new URL("https://blockchain.info/q/addressbalance/" + this.address);
    if (BitQuest.BLOCKCYPHER_CHAIN.equals("btc/test3")) {
      u = new URL(
          "https://testnet.blockchain.info/q/addressbalance/" + this.address + "?confirmations=" +
              confirmations);
    }
    if (BitQuest.BLOCKCYPHER_CHAIN.equals("doge/main")) {
      u = new URL("https://dogechain.info/chain/Dogecoin/q/addressbalance/" + this.address);
    }
    c = (HttpsURLConnection) u.openConnection();
    c.setRequestMethod("GET");
    c.setRequestProperty("Content-length", "0");
    c.setUseCaches(false);
    c.setAllowUserInteraction(false);
    c.connect();
    int status = c.getResponseCode();
    System.out.println("[balance] status:" + status);
    switch (status) {
      case 500:
        System.out.println(u.toString());
        throw new IOException("Internal Server Error");
      case 200:
      case 201:
        BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
          sb.append(line + "\n");
        }
        br.close();
        System.out.println("[balance] " + this.address + ": " + sb.toString().trim());
        if (BitQuest.BLOCKCYPHER_CHAIN.equals("doge/main")) {
          return Math.round(Double.parseDouble(sb.toString().trim()) * 100000000L);
        } else {
          return Math.round(Double.parseDouble(sb.toString().trim()));
        }
        // JSONParser parser = new JSONParser();

        // JSONObject response_object = (JSONObject) parser.parse(sb.toString());
        // return (Long) response_object.get("final_balance");
      case 429:
        throw new IOException("Rate limit");
      default:
        throw new IOException("Internal Server Error");
    }

  }

  public String url() {
    if (address.substring(0, 1).equals("N")
        || address.substring(0, 1).equals("n")
        || address.substring(0, 1).equals("m")) {
      return "live.blockcypher.com/btc-testnet/address/" + address;
    }
    if (address.substring(0, 1).equals("D")) {
      return "live.blockcypher.com/doge/address/" + address;
    } else {
      return "live.blockcypher.com/btc/address/" + address;
    }
  }

  public boolean save(UUID uuid) {
    BitQuest.REDIS.set("Wallet.privateKey." + uuid.toString(), this.privateKey);
    BitQuest.REDIS.set("Wallet.publicKey." + uuid.toString(), this.publicKey);
    BitQuest.REDIS.set("Wallet.address." + uuid.toString(), this.address);
    BitQuest.REDIS.set("Wallet.WIF." + uuid.toString(), this.wif);
    return true;
  }
}
