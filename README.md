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
Windows: The Windows Subsystem for Linux enables the bash shell which will be used to download the Spigot API. After [installing WSL](https://docs.microsoft.com/en-us/windows/wsl/install-win10) you can follow the Linux instructions below.

You must install Java JRE, JDK and Maven

```sh
sudo apt install -y openjdk-8-jre openjdk-8-jdk maven
```

## 2. Compile BitQuest and generate a JAR file
There is a maven project that will download the spigot 1.12.2 (downloading the latest version will be automated in future):

```sh
maven compile -B
```

## 3.1 Compile BitQuest and generate a JAR file (FOR DEV)
You can easly compile the code by doing (the script will format the code using ./format.sh and) (you can exec this script from anywhere (you can do ../../../../../build.sh if you where on src/java/main/bitquest/bitquest/)):

```sh
./build.sh
```

### 3.2 format the code to google java style (FOR DEV)
You can easly format the code by doing (you can exec this script from anywhere (you can do ../../../../../build.sh if you where on src/java/main/bitquest/bitquest/)):

```sh
./format.sh
```

# Requirements for running

A [Bitcoin Core](https://bitcoin.org/) testnet node running in your computer or local network with the json-rpc interface activated.

# Running a local BitQuest test server with docker (OUTADED)

Running locally via Docker is the fastest way to develop and test code. [Docker](http://docker.com) and [Docker Compose](https://docs.docker.com/compose/) can be used for testing the compiled plugin on spigot.

1. Build BitQuest using the instructions above (maven compile).
2. Install [Docker](https://docs.docker.com/engine/installation/), and [Docker Compose](https://docs.docker.com/compose/install/) if you haven't yet.
3. Create a docker-compose.yml file with your configuration. A good idea is to create a volume on spigot's 'plugins' pointing to the local directory where .jar files are compiled. Or you can use the following example:

```yalm
spigot:
  container_name: bitquest
  environment:
    - BITQUEST_ENV=development
    - BITCOIN_NODE_HOST=127.0.0.1 # or the IP of your Bitcoin Core node
    - BITCOIN_NODE_USERNAME=alice # the username for the RPC-JSON interface
    - BITCOIN_NODE_PASSWORD=secret # the password for RPC-JSON
  build: .
  volumes:
    - "./build/libs/:/spigot/plugins"
    - ".:/bitquest"
  ports:
    - "25565:25565"
  links:
    - redis
redis:
  image: redis
````

4. Use docker-compose to spawn a test server

```sh
docker-compose up
```
# Troubleshooting
## I'm getting a JedisConnectionException error when starting
Please make sure you have redis installed. If you are running via docker please make sure you have a redis container linked to the bitquest container.

# More info

https://bitquest.co/
