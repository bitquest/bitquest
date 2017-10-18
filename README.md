# BitQuest

[BitQuest](http://bitquest.co/) is a Minecraft server with a Bitcoin-denominated currency and MMORPG elements. This repository is the open source code running on the server.

# Play BitQuest
To play in the official BitQuest server you must own the official Minecraft game for PC/Mac/Linux and add this server address: 
```sh
play.bitquest.co
```

# Important Note
This server is still under development, and bugs can, and most likely will, happen. Please report any bugs to a moderator promptly, to ensure they are fixed. Also note that your wallet is not guaranteed, and you may experience issues with your balance. For this reason, it is highly recommended that you do not deposit large amounts, or anything you do not wish to place at risk by being tied to the server. In addition, if you are caught breaking rules and are banned, you forfeit your access to your server wallet, and all funds tied to it. With this said, rules are non-negotiable, and will be strictly enforced. This server is a fair server, and anyone abusing any system set in place will be punished. Please play fair, have fun, and enjoy the server


# How it works?
## Everyone is a Bitcoin wallet
The [BitQuest](https://bitquest.co/) server and every player has a bitcoin address. Any player can receive and send bitcoin to any address inside or outside the game. This is useful for buying materials, selling crafts, trading, tipping, etcetera.
![A player just joined the server](http://i.imgur.com/1A6wkaB.png)
![The playercan see it's bitcoin balance](http://i.imgur.com/5g5pBXB.png)
Thanks to the open nature of Bitcoin, all transactions in the server can be seen on the Blockchain using tools like [Insight](https://insight.bitpay.com/) or [Blockchain.Info](https://blockchain.info/). This is helpful for debugging and transparency.

## And there's loot!
Every time a player kills an enemy (mob) there is a chance to get loot. If that is the case the server makes a transaction directly from the server address to the player address and the player is notified.
![A player got loot](http://i.imgur.com/cxqXmt2.png)

## Everyone can send money anywhere
You can send Bitcoin to an external wallet with /transfer:
```sh
/transfer <amount> <recipient-bitcoin-address>
```
![Player using transfer command](http://i.imgur.com/Vlf9C1F.png)
![Player notification](http://i.imgur.com/PHmomoS.png)
![Player's public transaction](http://i.imgur.com/JPO4AXt.png)  
Players can also send Bitcoin using email instead of a Bitcoin address using:
```sh
/transfer <amount> <recipient-email>
```
Additionally, players can send Bitcoin to other players via /send:
```sh
/send <amount> <username>
```

With this method the recipient will receive an email notifying that a bitcoin transaction has been made to a [XAPO wallet](https://xapo.com/wallet/) linked to his email.

## Server address
The BitQuest server has it's own address, used for giving Loot to players

## About the back-end technology

All persistent data is saved in a redis database so the server can respond as quick as possible. 

Everybody is welcome to contribute. :D

Here are the instructions to modify, install and run the server as localhost.


# Building the BitQuest Java Plugin

## Install bash (Windows only)
To setup the workspace you need to run a gradle script that only runs on bash. You can get a distribution of bash by installing git from the [git-scm](https://git-scm.com/) website.

Warning: building BitQuest is not currently supoported on Windows 10 Anniversary edition bash. If you have that feature installed, your build will fail. If you are building using Windows 10 Anniversary edition, it's recommended to uninstall the Windows Subsytem for Linux feature first.

## Setup Workspace
There is a gradle task that will download and compile the latest Spigot API and other tools needed to compile the project. Using a terminal, go to the project directory and run:

````
./gradlew setupWorkspace
````

## Compile BitQuest and generate a JAR file
After the workspace is set up, we can compile using the shadowJar task that will create a file under build/libs. This should be dropped on the plugins folder of your Spigot server, but you can automate the process for testing using Docker (instructions below)

````
./gradlew shadowJar
````
# Requirements for development

A [Bitcoin Core](https://bitcoin.org/) testnet node running in your computer or local network with the json-rpc interface activated.

# Running a local BitQuest test server

Running locally via Docker is the fastest way to develop and test code. [Docker](http://docker.com) and [Docker Compose](https://docs.docker.com/compose/) can be used for testing the compiled plugin on spigot.

1. Build BitQuest using the instructions above (./gradlew shadowJar).
2. Install [Docker](https://docs.docker.com/engine/installation/), and [Docker Compose](https://docs.docker.com/compose/install/) if you haven't yet.
3. Create a docker-compose.yml file with your configuration. A good idea is to create a volume on spigot's 'plugins' pointing to the local directory where .jar files are compiled. Or you can use the following example:

````
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

```
docker-compose up
```

# More info

More info about [BitQuest](https://bitquest.co/) at
https://bitquest.co/
