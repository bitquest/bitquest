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
You can send Bitcoin to an outside wallet or other players using the Minecraft console command:
```sh
/transfer <amount> <recipient-bitcoin-address>
```
![Player using transfer command](http://i.imgur.com/Vlf9C1F.png)
![Player notification](http://i.imgur.com/PHmomoS.png)
![Player's public transaction](http://i.imgur.com/JPO4AXt.png)  
Players can also send money using email instead of a bitcoin address using:
```sh
/transfer <amount> <recipient-email>
```
And also by username using:
```sh
/transfer <amount> <username>
```

With this method the recipient will receive an email notifying that a bitcoin transaction has been made to a [XAPO wallet](https://xapo.com/wallet/) linked to his email.

## Server address
The BitQuest server has it's own address, used for giving Loot to players

## About the back-end technology

All persistent data is saved in a redis database so the server can respond as quick as possible. 
All transactions in the game are on-chain using [Blockcypher](http://blockcypher.com/)'s microtransactions API or in case of player-to-email [XAPO API](http://docs.xapo.apiary.io/).

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

# Running a local BitQuest test server

Running locally via Docker is the fastest way to develop and test code. Docker is also the recommended way to run BitQuest in production, however configuration is different. Docker works by creating a "container" that runs your code that is very similar in concept to a Virtual machine. The file docker-compose.yml can be used to create this image and run a local server "synced" with your builds, so you won't need to restart the server to update changes.

1. Build BitQuest using the instructions above (./gradlew shadowJar).
2. Install Docker, the container runtime (For Windows and Mac, Beta version is recommended):
[Get Docker](http://docs.docker.com/mac/started/)
3. Install docker-compose, to orchestrate our dev environment: [Get docker-compose](http://docs.docker.com/mac/started/)
4. Create development.yml file, where your local variables will be. This is done to protect API and private keys you might want to use to test. (development.yml is in .gitignore so it won't be uploaded to github) A good starting point is:

````
spigot:
  environment:
    - BLOCKCYPHER_API_KEY=<your API key here>

````

5. Create a "bitquest" HD wallet in BlockCypher, using your API Key and a Bitcoin public key. You can use the testnet public key found on the docker-compose.yml:

```
curl -d '{"name": "bitquest", "extended_public_key": "tpubD6NzVbkrYhZ4Wwu2zXR4r2LAD87nLwqdKWBsH8qa2EkSbD5RSJARhCEKoBjuJAbig7aowS6gGJz9S6R77Yqf6DLE7qTFuT3ZV6ZZeKQGRs7", "subchain_indexes": [0]}' https://api.blockcypher.com/v1/btc/test3/wallets/hd?token=<Your API Key>
```

6. To start your test server with docker-compose you can now run:

````
docker-compose up
````
The server will run with a local volume pointing to your latest jar built with Gradle. That means you can /reload inside the server and watch changes without restarting the Spigot container.

# Running your local BitQuest server
To run BitQuest might want to do the same steps as with a local test server, but specify a Bitcoin address for your local loot wallet, a BlockCypher API key and (optional) your Mojang account UUID so you are admin in your own server (otherwise you won't have op). To do this, you'll need to create a development.yml file that docker-compose will use to configure your local BitQuest instance.

You will be able to connect to ````localhost```` in Minecraft, and every time you run the ````shadowJar```` gradle task, following a ````/reload```` command inside the game, you'll be playing in your newest compiled code, without restarting or rebuilding the container.  In order to shut down the server and save the state of the world use the ````/emergencystop```` command from inside the game before exiting the docker process.

# Environment variables reference
These environment variables are added as needed to your development.yml file in the same style as the BLOCKCYPHER_API_KEY shown in the example file above.

## BLOCKCYPHER_API_KEY
Your API key obtained by blockcypher. This is not optional

## BLOCKCHAIN
The Blockhain the server will run in. Options are btc/main for Bitcoin, btc/test3 for Testnet, doge/main for Dogecoin

## WORLD_ADDRESS
The address of the world wallet. World wallet is where purchases are going to, and where loot comes from.

## HD_TRANSFER_ADDRESS
if defined, the world will use HD wallets for users and only one private key (WORLD_PRIVATE_KEY) will be used for /transfer. Otherwise /transfer (and fees) will occur on each player's wallet.

## WORLD_PRIVATE_KEY
Private key of the world wallet. If TRANSFER_ADDRESS is defined, it should be the private key of HD_TRANSFER_ADDRESS.  To get the private key for a given address in bitcoin-qt open the console in your wallet and run the 'dumpprivkey' command with the address as the argument.

## WORLD_PUBLIC_KEY
Public key of the world wallet. If TRANSFER_ADDRESS is defined, it should be the public key of HD_TRANSFER_ADDRESS.  To get the public key for a given address in bitcoin-qt open the console in your wallet and run the 'validateaddress' command with the address as the argument.

## ADMIN_UUID
The UUID of the user to be granted admin rights over the server.  Currently if this is not set you may get a java exception when bringing up the server.

# More info

More info about [BitQuest](https://bitquest.co/) at
https://bitquest.co/
