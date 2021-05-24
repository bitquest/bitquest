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
        RPCCall
        Utility function for communicating with the node via RPC
    */
    private JSONObject RPCCall(String method, JSONArray params) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
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
        JSONObject response = (JSONObject) parser.parse(responseJson.toString());
        return (JSONObject) response;
    }
    private JSONObject RPCCall(String method) throws IOException, ParseException {
        return RPCCall(method,new JSONArray());
    }
    /*
        blocks
        Return the last block synced in the Node. If the number is the same as the last block mined on the network, it means the node is fully synced up.
    */
    public Long blocks() throws IOException, ParseException {
        JSONObject response = RPCCall("getblockchaininfo");
        JSONObject blockhain_info = (JSONObject) response.get("result");

        Long blocks = (Long) blockhain_info.get("blocks");
        return blocks;
    }
    /*
        accounts
        Returns a list of all the wallets stored in the node
    */
    public JSONObject accounts() throws IOException, ParseException {
        JSONObject response = RPCCall("listaccounts");
        return (JSONObject) response.get("result");
    }
    /*
        account
        Returns the owner account of the specified address
    */
    public JSONObject account(String address) throws IOException, ParseException {
        JSONArray params = new JSONArray();
        params.add(address);
        JSONObject response = RPCCall("getaccount",params);
        return (JSONObject) response.get("result");
    }
    /*
        address
        Returns the address of specified account
    */
    public String address(String account_id) throws IOException, ParseException {
        JSONArray params = new JSONArray();
        params.add(account_id);
        JSONObject response = RPCCall("getaccountaddress",params);
        System.out.println(response);
        return (String)response.get("result");
    }
}
