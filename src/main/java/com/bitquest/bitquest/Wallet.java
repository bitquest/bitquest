package com.bitquest.bitquest;

import java.io.IOException;
import org.json.simple.parser.ParseException;

public class Wallet {
  public String accountName;
  public Node node;

  public Wallet(Node node, String accountName) {
    this.accountName = accountName;
    this.node = node;
  }

  public Double balance(Integer minimumConfirmations) throws Exception {
    return this.node.getBalance(accountName, minimumConfirmations);
  }

  public String address() throws Exception {
    return this.node.getAccountAddress(accountName);
  }

  public String addressUrl() throws Exception {
    if (node.testnet()) {
      return "https://sochain.com/address/DOGETEST/" + this.address();
    } else {
      return "https://chain.so/address/DOGE/" + this.address();
    }
  }

  public Boolean send(String toAddress, Double amount) throws Exception {
    return this.node.sendFrom(accountName, toAddress, amount);
  }
}
