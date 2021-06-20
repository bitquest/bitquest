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

  public Boolean send(String toAddress, Double amount) throws Exception {
    return this.node.sendFrom(accountName, toAddress, amount);
  }
}
