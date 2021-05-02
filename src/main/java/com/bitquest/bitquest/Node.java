package com.bitquest.bitquest;

public class Node {
    public String host;
    public int port;
    public String rpcUsername;
    public String rpcPassword;
    public Node() {
        this.host="localhost";
        this.port=18333;
        this.rpcUsername="testuser";
        this.rpcPassword="testpass";
    }
}
