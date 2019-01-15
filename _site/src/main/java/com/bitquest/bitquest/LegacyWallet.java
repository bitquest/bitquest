package com.bitquest.bitquest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LegacyWallet {
  String account_id;

  public LegacyWallet(String _account_id) {
    this.account_id = _account_id;
  }

  public String sendFrom(String address, Long sat) throws IOException, ParseException {
    JSONParser parser = new JSONParser();

    final JSONObject jsonObject = new JSONObject();
    jsonObject.put("jsonrpc", "1.0");
    jsonObject.put("id", "bitquest");
    jsonObject.put("method", "sendfrom");
    JSONArray params = new JSONArray();
    params.add(account_id);
    params.add(address);
    System.out.println(sat);
    Double double_sat = new Double(sat);
    System.out.println(double_sat);

    params.add(double_sat / 100000000L);
    System.out.println(params);
    jsonObject.put("params", params);
    System.out.println("Checking blockchain info...");
    URL url = new URL("http://" + BitQuest.BITCOIN_NODE_HOST + ":" + BitQuest.BITCOIN_NODE_PORT);
    System.out.println(url.toString());
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    String userPassword = BitQuest.BITCOIN_NODE_USERNAME + ":" + BitQuest.BITCOIN_NODE_PASSWORD;
    String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
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
    JSONObject response_object = (JSONObject) parser.parse(response.toString());
    System.out.println(response_object);
    return (String) response_object.get("result");
  }

  public String getAccountAddress() throws IOException, ParseException {

    JSONParser parser = new JSONParser();

    final JSONObject jsonObject = new JSONObject();
    jsonObject.put("jsonrpc", "1.0");
    jsonObject.put("id", "bitquest");
    jsonObject.put("method", "getaccountaddress");
    JSONArray params = new JSONArray();
    params.add(account_id);
    if (BitQuest.BITQUEST_ENV == "development")
      System.out.println("[getaccountaddress] " + account_id);
    jsonObject.put("params", params);
    URL url = new URL("http://" + BitQuest.BITCOIN_NODE_HOST + ":" + BitQuest.BITCOIN_NODE_PORT);
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    String userPassword = BitQuest.BITCOIN_NODE_USERNAME + ":" + BitQuest.BITCOIN_NODE_PASSWORD;
    String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
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
    if (BitQuest.BITQUEST_ENV == "development") System.out.println(response_object);
    return response_object.get("result").toString();
  }

  public Long getBalance(int confirmations) {
    System.out.println(account_id);
    try {
      JSONParser parser = new JSONParser();
      final JSONObject jsonObject = new JSONObject();
      jsonObject.put("jsonrpc", "1.0");
      jsonObject.put("id", "bitquest");
      jsonObject.put("method", "getbalance");
      JSONArray params = new JSONArray();
      params.add(this.account_id);
      params.add(confirmations);
      jsonObject.put("params", params);
      URL url = new URL("http://" + BitQuest.BITCOIN_NODE_HOST + ":" + BitQuest.BITCOIN_NODE_PORT);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setConnectTimeout(5000);
      String userPassword = BitQuest.BITCOIN_NODE_USERNAME + ":" + BitQuest.BITCOIN_NODE_PASSWORD;
      String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
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
      Double d = Double.parseDouble(response_object.get("result").toString().trim()) * 100000000L;

      final Long balance = d.longValue();
      return balance;
    } catch (Exception e) {
      System.out.println(e);
      return Long.valueOf(0);
    }
  }
}
