package com.bitquest.bitquest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Node {
  public String host = System.getenv("BITQUEST_NODE_HOST");
  public int port = System.getenv("BITQUEST_NODE_PORT") != null ? Integer.parseInt(System.getenv("BITQUEST_NODE_PORT")) : 44555;
  public String rpcUsername = System.getenv("BITQUEST_NODE_RPC_USER");
  public String rpcPassword = System.getenv("BITQUEST_NODE_RPC_PASSWORD");

  /*
      rpcCall
      Utility function for communicating with the node via RPC
  */
  private JSONObject rpcCall(String method, JSONArray params) throws Exception {
    final JSONObject jsonObject = new JSONObject();
    jsonObject.put("jsonrpc", "1.0");
    jsonObject.put("id", "bitquest");
    jsonObject.put("method", method);
    jsonObject.put("params", params);
    URL url = new URL("http://" + host + ":" + port);
    System.out.println(url);
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
    JSONObject error = (JSONObject) response.get("error");
    if (error != null) {
      String errorMessage = (String) error.get("message");
      throw(new Exception(errorMessage));
    }
    return (JSONObject) response;
  }

  private JSONObject rpcCall(String method) throws Exception {
    return rpcCall(method, new JSONArray());
  }

  public JSONObject getBlockchainInfo() throws Exception {
    JSONObject response = rpcCall("getblockchaininfo");
    JSONObject blockchainInfo = (JSONObject) response.get("result");
    return blockchainInfo;
  }
  
  
  public JSONObject getInfo() throws Exception {
    JSONObject response = rpcCall("getinfo");
    JSONObject blockchainInfo = (JSONObject) response.get("result");
    return blockchainInfo;
  }

  /*
      chain
      Returns the chain name this node is connected to
  */
  public String chain() throws Exception {
    return this.testnet() ? "tDOGE" : "DOGE";
  }

  /*
      accounts
      Returns a list of all the accounts in the node
  */
  public JSONObject listAccounts() throws Exception {
    JSONObject response = rpcCall("listaccounts");
    return (JSONObject) response.get("result");
  }

  /*
      listWallets
      Returns a list of all the wallets stored in the node
  */
  public JSONObject listWallets() throws Exception {
    JSONObject response = rpcCall("listwallets");
    return (JSONObject) response.get("result");
  }

  /*
      getAccount
      Returns the owner account of the specified address
  */
  public JSONObject getAccount(String address) throws Exception {
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
      throws Exception {
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
  public String getAccountAddress(String accountId) throws Exception {
    JSONArray params = new JSONArray();
    params.add(accountId);
    JSONObject response = rpcCall("getaccountaddress", params);
    return (String) response.get("result");
  }

  /*
      sendFrom
      Sends amount from account’s balance to address.
  */
  public boolean sendFrom(String fromAccount, String toAddress, Double amount) throws Exception {
    JSONArray params = new JSONArray();
    params.add(fromAccount);
    params.add(toAddress);
    params.add(amount);
    JSONObject response = rpcCall("sendfrom", params);
    // System.out.println(response);
    String transactionId = (String) response.get("result");
    BitQuest.log("tx",transactionId);
    return transactionId != null;
  }

  public Boolean testnet() throws Exception {
    return (Boolean) this.getInfo().get("testnet");
  }

}
