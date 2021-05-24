package com.bitquest.bitquest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class BlockCypher {
  public static final JSONObject post(String method, JSONObject params)
      throws ParseException, IOException {
    try {
      URL url = new URL(
          "https://api.blockcypher.com/v1/" + BitQuest.BLOCKCYPHER_CHAIN + "/" + method +
              "?token=" + System.getenv("BLOCKCYPHER_TOKEN"));
      System.out.println(url);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setConnectTimeout(5000);
      con.setDoOutput(true);
      OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
      out.write(params.toString());
      System.out.println(url.toString() + " --> " + params.toString());
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
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();
      System.out.println(response.toString());
      JSONParser parser = new JSONParser();

      return (JSONObject) parser.parse(response.toString());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      System.out.println("[blockcypher] error " + method);
      throw (e);
    }
  }

  public static final JSONObject get(String method) throws IOException, ParseException {
    HttpsURLConnection c = null;
    URL url = new URL(
        "https://api.blockcypher.com/v1/" + BitQuest.BLOCKCYPHER_CHAIN + "/" + method + "?token=" +
            System.getenv("BLOCKCYPHER_TOKEN"));
    System.out.println(url);
    c = (HttpsURLConnection) url.openConnection();
    c.setRequestMethod("GET");
    c.setRequestProperty("Content-length", "0");
    c.setUseCaches(false);
    c.setAllowUserInteraction(false);
    c.connect();
    int status = c.getResponseCode();
    switch (status) {
      case 500:
        System.out.println(url.toString());
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
        JSONParser parser = new JSONParser();
        JSONObject responseObject = (JSONObject) parser.parse(sb.toString());
        return responseObject;
      case 429:
        throw new IOException("Rate Limit");
      default:
        throw new IOException("Internal Server Error");
    }
  }
}
