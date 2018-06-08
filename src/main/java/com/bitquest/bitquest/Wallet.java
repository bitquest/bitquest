package com.bitquest.bitquest;

import java.io.*;
import java.net.*;
import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.Base64;

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

  public interface AddWitnessAddressCallback {
    void run(String address);
  }

  public interface SetAccountCallback {
    void run(Boolean success);
  }

  public Wallet(BitQuest plugin, String account_id) {
    this.account_id = account_id;
    this.bitQuest = plugin;
    if(this.bitQuest.BITCOIN_NODE_HOST!=null) {
      getAccountAddress(
              new GetAccountAddressCallback() {
                @Override
                public void run(String accountAddress) {
                  address = accountAddress;
                }
              });
    }

  }

  public void getBalance(int confirmations, final GetBalanceCallback callback) {
    final String account_id = this.account_id;
    System.out.println(account_id);
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            bitQuest,
            new Runnable() {
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
                  params.add(confirmations);
                  if (bitQuest.BITQUEST_ENV == "development")
                    System.out.println("[getbalance] " + account_id + " " + confirmations);
                  jsonObject.put("params", params);
                  URL url =
                      new URL(
                          "http://"
                              + BitQuest.BITCOIN_NODE_HOST
                              + ":"
                              + BitQuest.BITCOIN_NODE_PORT);
                  HttpURLConnection con = (HttpURLConnection) url.openConnection();
                  con.setConnectTimeout(5000);
                  String userPassword =
                      BitQuest.BITCOIN_NODE_USERNAME + ":" + BitQuest.BITCOIN_NODE_PASSWORD;
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
                  JSONObject response_object = (JSONObject) parser.parse(response.toString());
                  Double d =
                      Double.parseDouble(response_object.get("result").toString().trim())
                          * 100000000L;

                  if (bitQuest.BITQUEST_ENV == "development") System.out.println(response_object);
                  final Long balance = d.longValue();
                  Bukkit.getScheduler()
                      .runTask(
                          bitQuest,
                          new Runnable() {
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
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            bitQuest,
            new Runnable() {
              @Override
              public void run() {
                if(bitQuest.BITCOIN_NODE_HOST!=null) {
                  try {
                    JSONParser parser = new JSONParser();

                    final JSONObject jsonObject = new JSONObject();
                    jsonObject.put("jsonrpc", "1.0");
                    jsonObject.put("id", "bitquest");
                    jsonObject.put("method", "getaccountaddress");
                    JSONArray params = new JSONArray();
                    params.add(account_id);
                    if (bitQuest.BITQUEST_ENV == "development")
                      System.out.println("[getaccountaddress] " + account_id);
                    jsonObject.put("params", params);
                    URL url =
                            new URL(
                                    "http://"
                                            + BitQuest.BITCOIN_NODE_HOST
                                            + ":"
                                            + BitQuest.BITCOIN_NODE_PORT);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    String userPassword =
                            BitQuest.BITCOIN_NODE_USERNAME + ":" + BitQuest.BITCOIN_NODE_PASSWORD;
                    String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
                    con.setRequestProperty("Authorization", "Basic " + encoding);
                    con.setConnectTimeout(5000);
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
                    JSONObject response_object = (JSONObject) parser.parse(response.toString());
                    if (bitQuest.BITQUEST_ENV == "development") System.out.println(response_object);
                    callback.run(response_object.get("result").toString());
                  } catch (Exception e) {
                    System.out.println("Error on getAccountAddress");
                    e.printStackTrace();
                  }
                } else {
                  // use emeralds as currency (EXPERIMENTAL)
                  callback.run(account_id);
                }

              }
            });
  }

  void addWitnessAddress(String address, final AddWitnessAddressCallback callback) {
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            bitQuest,
            new Runnable() {
              @Override
              public void run() {
                try {
                  JSONParser parser = new JSONParser();

                  final JSONObject jsonObject = new JSONObject();
                  jsonObject.put("jsonrpc", "1.0");
                  jsonObject.put("id", "bitquest");
                  jsonObject.put("method", "addwitnessaddress");
                  JSONArray params = new JSONArray();
                  params.add(address);

                  jsonObject.put("params", params);
                  URL url =
                      new URL(
                          "http://"
                              + BitQuest.BITCOIN_NODE_HOST
                              + ":"
                              + BitQuest.BITCOIN_NODE_PORT);
                  HttpURLConnection con = (HttpURLConnection) url.openConnection();
                  String userPassword =
                      BitQuest.BITCOIN_NODE_USERNAME + ":" + BitQuest.BITCOIN_NODE_PASSWORD;
                  String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
                  con.setRequestProperty("Authorization", "Basic " + encoding);
                  con.setConnectTimeout(5000);
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
                  JSONObject response_object = (JSONObject) parser.parse(response.toString());
                  if (bitQuest.BITQUEST_ENV == "development") System.out.println(response_object);
                  callback.run(response_object.get("result").toString());
                } catch (Exception e) {
                  System.out.println("[addwitnessaddress] fail");
                  e.printStackTrace();
                }
              }
            });
  }

  void setAccount(String address, final SetAccountCallback callback) {
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            bitQuest,
            new Runnable() {
              @Override
              public void run() {
                try {
                  JSONParser parser = new JSONParser();
                  final JSONObject jsonObject = new JSONObject();
                  jsonObject.put("jsonrpc", "1.0");
                  jsonObject.put("id", "bitquest");
                  jsonObject.put("method", "setaccount");
                  JSONArray params = new JSONArray();
                  params.add(address);
                  params.add(account_id);

                  jsonObject.put("params", params);
                  URL url =
                      new URL(
                          "http://"
                              + BitQuest.BITCOIN_NODE_HOST
                              + ":"
                              + BitQuest.BITCOIN_NODE_PORT);
                  HttpURLConnection con = (HttpURLConnection) url.openConnection();
                  String userPassword =
                      BitQuest.BITCOIN_NODE_USERNAME + ":" + BitQuest.BITCOIN_NODE_PASSWORD;
                  String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
                  con.setRequestProperty("Authorization", "Basic " + encoding);
                  con.setConnectTimeout(5000);
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
                  JSONObject response_object = (JSONObject) parser.parse(response.toString());
                  if (bitQuest.BITQUEST_ENV == "development") System.out.println(response_object);
                  callback.run(true);
                } catch (Exception e) {
                  System.out.println("[setaccount] error");
                  e.printStackTrace();
                }
              }
            });
  }

  public boolean move(String to, Long sat) throws IOException, ParseException {

    if (sat >= 100 && sat <= Long.MAX_VALUE) {
      JSONParser parser = new JSONParser();

      final JSONObject jsonObject = new JSONObject();
      jsonObject.put("jsonrpc", "1.0");
      jsonObject.put("id", "bitquest");
      jsonObject.put("method", "move");
      JSONArray params = new JSONArray();
      params.add(this.account_id);
      params.add(to);
      Double double_sat = new Double(sat);

      params.add(double_sat / 100000000L);
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
      if (bitQuest.BITQUEST_ENV == "development") System.out.println(response_object);
      return (boolean) response_object.get("result");
    } else {
      System.out.println(
          "[move] "
              + this.account_id
              + "-> "
              + sat
              + " --> "
              + to
              + ": FAIL (must be between 100 & "
              + Long.MAX_VALUE
              + ")");
      return false;
    }
  }

  public String sendFrom(String address, Long sat) throws IOException, ParseException {
    JSONParser parser = new JSONParser();

    final JSONObject jsonObject = new JSONObject();
    jsonObject.put("jsonrpc", "1.0");
    jsonObject.put("id", "bitquest");
    jsonObject.put("method", "sendfrom");
    JSONArray params = new JSONArray();
    params.add(this.account_id);
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

  public String url() {
    if (address.substring(0, 1).equals("N") || address.substring(0, 1).equals("n")) {
      return "live.blockcypher.com/btc-testnet/address/" + address;
    }
    if (address.substring(0, 1).equals("D")) {
      return "live.blockcypher.com/doge/address/" + address;
    } else {
      return "live.blockcypher.com/btc/address/" + address;
    }
  }
  
  	// the isPvP function by @bitcoinjake09
public boolean isPvP(Location location) {
		if ((landPermissionCode(location).equals("v")==true)||(landPermissionCode(location).equals("pv")==true))
		//if(SET_PvP.equals("true"))
    {return true;}// returns true. it is a pvp or public pvp and if SET_PvP is true

               return false;//not pvp
    }
// end isPvP by @bitcoinjake09
public static int countEmeralds(Player player) {

        ItemStack[] items = player.getInventory().getContents();
        int amount = 0;
        for (int i=0; i<player.getInventory().getSize(); i++) {
	ItemStack TempStack = items[i];	
	if ((TempStack != null) && (TempStack.getType() != Material.AIR)){          
	if (TempStack.getType().toString() == "EMERALD_BLOCK") {
                amount += (TempStack.getAmount()*9);
            }
	else if (TempStack.getType().toString() == "EMERALD") {
                amount += TempStack.getAmount();
            }
		}
        }
        return amount;
    }//end count emerald in player inventory by @bitcoinjake09
public boolean removeEmeralds(Player player,int amount){
	 int EmCount = countEmeralds(player);
	 int LessEmCount = countEmeralds(player)-amount;
	 double TempAmount=(double)amount;
	int EmsBack=0;
	ItemStack[] items = player.getInventory().getContents();
	if (countEmeralds(player)>=amount){	
		while(TempAmount>0){		
		for (int i=0; i<player.getInventory().getSize(); i++) {
			ItemStack TempStack = items[i];	
			
			if ((TempStack != null) && (TempStack.getType() != Material.AIR)){          	
			
			if ((TempStack.getType().toString() == "EMERALD_BLOCK")&&(TempAmount>=9)) {
		    player.getInventory().removeItem(new ItemStack(Material.EMERALD_BLOCK, 1));	
        			TempAmount=TempAmount-9;
				}
			if ((TempStack.getType().toString() == "EMERALD_BLOCK")&&(TempAmount<9)) {
		    player.getInventory().removeItem(new ItemStack(Material.EMERALD_BLOCK, 1));	
				EmsBack=(9-(int)TempAmount);  //if 8, ems back = 1      		
				TempAmount=TempAmount-TempAmount;
				if (EmsBack>0) {player.getInventory().addItem(new ItemStack(Material.EMERALD, EmsBack));}
				}
			if ((TempStack.getType().toString() == "EMERALD")&&(TempAmount>=1)) {
      		          player.getInventory().removeItem(new ItemStack(Material.EMERALD, 1));		
        			TempAmount=TempAmount-1;
				}
			
			}//end if != Material.AIR
			
		
	}// end for loop
	}//end while loop
	}//end (EmCount>=amount)
	EmCount = countEmeralds(player);
	if ((EmCount==LessEmCount)||(TempAmount==0))
	return true;	
	return false;
}//end of remove emeralds
//start addemeralds to inventory
public boolean addEmeralds(Player player,int amount){
	int EmCount = countEmeralds(player);
	 int moreEmCount = countEmeralds(player)+amount;
	 double bits = (double)amount;
	 double TempAmount=(double)amount;
	int EmsBack=0;
		while(TempAmount>=0){		
			    	if (TempAmount>=9){		
				TempAmount=TempAmount-9;
				player.getInventory().addItem(new ItemStack(Material.EMERALD_BLOCK, 1));
				}
				if (TempAmount<9){	
				TempAmount=TempAmount-1;
				player.getInventory().addItem(new ItemStack(Material.EMERALD, 1));
				}
			EmCount = countEmeralds(player);
			if ((EmCount==moreEmCount))
			return true;
			}//end while loop
	return false;
}
  
}
