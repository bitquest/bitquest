package com.bitquest.bitquest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Node {
  public String host;
  public int port;
  public String rpcUsername;
  public String rpcPassword;

  /*
      rpcCall
      Utility function for communicating with the node via RPC
  */
  private JSONObject rpcCall(String method, JSONArray params) throws IOException, ParseException {
    final JSONObject jsonObject = new JSONObject();
    jsonObject.put("jsonrpc", "1.0");
    jsonObject.put("id", "bitquest");
    jsonObject.put("method", method);
    jsonObject.put("params", params);
    URL url = new URL("http://" + host + ":" + port);
    System.out.println(url.toString());
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setConnectTimeout(1000);
    String userPassword =
        rpcUsername + ":" + rpcPassword;
    String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
    con.setRequestProperty("Authorization", "Basic " + encoding);
    con.setRequestMethod("POST");
    con.setDoOutput(true);
    OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
    System.out.println(jsonObject.toString());
    out.write(jsonObject.toString());
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
    StringBuffer responseJson = new StringBuffer();

    while ((inputLine = in.readLine()) != null) {
      responseJson.append(inputLine);
    }
    in.close();
    System.out.println(responseJson.toString());
    JSONParser parser = new JSONParser();
    JSONObject response = (JSONObject) parser.parse(responseJson.toString());
    return (JSONObject) response;
  }

  private JSONObject rpcCall(String method) throws IOException, ParseException {
    return rpcCall(method, new JSONArray());
  }

  /*
      blocks
      Return the last block synced in the Node. 
      If the number is the same as the last block mined on the network, 
      it means the node is fully synced up.
  */
  public JSONObject getBlockchainInfo() throws IOException, ParseException {
    JSONObject response = rpcCall("getblockchaininfo");
    JSONObject blockchainInfo = (JSONObject) response.get("result");
    return blockchainInfo;
  }

  /*
      accounts
      Returns a list of all the accounts in the node
  */
  public JSONObject listAccounts() throws IOException, ParseException {
    JSONObject response = rpcCall("listaccounts");
    return (JSONObject) response.get("result");
  }

  /*
      listWallets
      Returns a list of all the wallets stored in the node
  */
  public JSONObject listWallets() throws IOException, ParseException {
    JSONObject response = rpcCall("listwallets");
    return (JSONObject) response.get("result");
  }


  /*
      getAccount
      Returns the owner account of the specified address
  */
  public JSONObject getAccount(String address) throws IOException, ParseException {
    JSONArray params = new JSONArray();
    params.add(address);
    JSONObject response = rpcCall("getaccount", params);
    return (JSONObject) response.get("result");
  }

  /*
      getBalance
      Returns the balance of account
  */
  public Double getBalance(String accountName, Integer minimumConfirmations)
      throws IOException, ParseException {
    JSONArray params = new JSONArray();
    params.add(accountName);
    params.add(minimumConfirmations);
    JSONObject response = rpcCall("getbalance", params);
    return (Double) response.get("result");
  }

  /*
      getAccountAddress
      Returns the address of specified account
  */
  public String getAccountAddress(String accountId) throws IOException, ParseException {
    JSONArray params = new JSONArray();
    params.add(accountId);
    JSONObject response = rpcCall("getaccountaddress", params);
    System.out.println(response);
    return (String) response.get("result");
  }
}
