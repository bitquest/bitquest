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

  public Double balance(Integer minimumConfirmations) throws IOException, ParseException {
    return this.node.getBalance(accountName, minimumConfirmations);
  }

  public String address() throws IOException, ParseException {
    return this.node.getAccountAddress(accountName);
  }

  public Boolean payment(String toAddress, Double amount) {
    return true;
  }
}
