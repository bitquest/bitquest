# BitQuest

[BitQuest](http://bitquest.co/) is a Minecraft server with a Bitcoin-denominated currency and MMORPG elements. This repository is the open source code running on the server.

# Play BitQuest
To play in the official BitQuest server you must own the official Minecraft game for PC/Mac/Linux and add this server address:
```
play.bitquest.co
```

# Important Note
This server is still under development, and bugs can, and most likely will, happen. Please report any bugs to a moderator promptly, to ensure they are fixed. Also note that your wallet is not guaranteed, and you may experience issues with your balance. For this reason, it is highly recommended that you do not deposit large amounts, or anything you do not wish to place at risk by being tied to the server. In addition, if you are caught breaking rules and are banned, you forfeit your access to your server wallet, and all funds tied to it. With this said, rules are non-negotiable, and will be strictly enforced. This server is a fair server, and anyone abusing any system set in place will be punished. Please play fair, have fun, and enjoy the server


# How it works?
## Everyone is a Bitcoin wallet
The [BitQuest](https://bitquest.co/) server and every player has a bitcoin address. Any player can receive and send bitcoin to any address inside or outside the game. This is useful for buying materials, selling crafts, trading, tipping, etcetera.
![A player just joined the server](http://i.imgur.com/1A6wkaB.png)
![The playercan see it's bitcoin balance](http://i.imgur.com/5g5pBXB.png)

## And there's loot!
Every time a player kills an enemy (mob) there is a chance to get loot. If that is the case the server makes a transaction directly from the server address to the player address and the player is notified.
![A player got loot](http://i.imgur.com/cxqXmt2.png)

## Everyone can send money anywhere
You can send Bitcoin to an external wallet with /transfer:
```
/transfer <amount> <recipient-bitcoin-address>
```
![Player using transfer command](http://i.imgur.com/Vlf9C1F.png)
![Player notification](http://i.imgur.com/PHmomoS.png)
![Player's public transaction](http://i.imgur.com/JPO4AXt.png)  

Additionally, players can send Bitcoin to other players via /send:
```
/send <amount> <username>
```

## Server address
The BitQuest server has it's own address, used for giving Loot to players

## About the back-end technology

All persistent data is saved in a redis database so the server can respond as quick as possible.

Everybody is welcome to contribute. :D

Here are the instructions to modify, install and run the server as localhost.


# Building the BitQuest Java Plugin

## 1. Install Requirements

You must install Java JRE, JDK and Maven

### 1.1 Linux

```sh
sudo apt install -y openjdk-8-jre openjdk-8-jdk maven
```

### 1.2 Windows

1. [Install JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
2. [Install Maven and set paths](https://tecadmin.net/install-apache-maven-on-windows/)

## 2. Compile BitQuest and generate a JAR file
There is a maven project that will download the spigot 1.12.2 (downloading the latest version will be automated in future):

```sh
mvn package -B
```

Or using all of your cpu core (fastest)
(If you have low ram your build can crash)
```sh
mvn package -B -T 1C
```

## 3. format the code to google java style
You can easly format the code by doing (you can exec this script from anywhere (you can do ../../../../../build.sh if you where on src/java/main/bitquest/bitquest/)):

```sh
./format.sh
```

# Requirements for running

A [Bitcoin Core](https://bitcoin.org/) testnet node running in your computer or local network with the json-rpc interface activated.

# Running a BitQuest server with Docker

Tou can use Docker to run a BitQuest server. There's also an [official BitQuest docker image](https://hub.docker.com/r/bitquest/bitquest/). The recommended way to configure the image is using a docker-compose.yml file that can link to a directory where the worlds are stored. An example is included here. Configuration can be done via enviroment variables:

| environment variable   | description                                          |
|------------------------|------------------------------------------------------|
| BITQUEST_NODE_USERNAME | username for Bitcoin node                            |
| BITCOIN_NODE_PASSWORD  | password for Bitcoin node                            |
| BITQUEST_NODE_HOST     | IP to a bitcoin node with JSON-RPC interface enabled |
| ADMIN_UUID             | Minecraft user ID for the main administrator         |

# Troubleshooting
## I'm getting a JedisConnectionException error when starting
Please make sure you have redis installed. If you are running via docker please make sure you have a redis container linked to the bitquest container.

# More info

https://bitquest.co/
