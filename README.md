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
/transfer <recipient-email>
```

With this method the recipient will receive an email notifying that a bitcoin transaction has been made to a [XAPO wallet](https://xapo.com/wallet/) linked to his email.

## Server address
The BitQuest server has it's own address, used for giving Loot to players

## About the back-end technology

All persistent data is saved in a redis database so the server can respond as quick as possible. 
All transactions in the game are on-chain using [Blockcypher](http://blockcypher.com/)'s microtransactions API or in case of player-to-email [XAPO API](http://docs.xapo.apiary.io/).

Everybody is welcome to contribute. :D

Here are the instructions to modify, install and run the server as localhost.


# Building BitQuest

## Setup Workspace
There is a gradle task that will download and compile the latest Spigot API and other tools needed to compile the project. Using a terminal, go to the project directory and run:

````
./gradlew setupWorkspace
````
(Linux and OSX)

````
gradlew.bat setupWorkspace
````
(Windows Powershell)

## Compile BitQuest and generate a JAR file
After the workspace is set up, we can compile using the gradle shadowJar task that will create a file under build/libs. Since this folder is "synced" with docker, you won't need to restart the server to update changes. (Just use /reload) inside the game

# Linux and OSX

````
./gradlew shadowJar
````
(Linux and OSX)

````
gradlew.bat shadowJar
````
(Windows Powershell)

# Running BitQuest locally with Docker

Running locally via Docker is the fastest way to develop and test code. Docker is also the recommended way to run BitQuest in production, however configuration is different.

1. Build BitQuest using the instructions above.
2. Install Docker, the container runtime (For Windows and Mac, Beta version is recommended):
[Get Docker](http://docs.docker.com/mac/started/)
3. Install docker-compose, to orchestrate our dev environment: [Get docker-compose](http://docs.docker.com/mac/started/)
4. Create development.yml file, where your local variables are. A good starting point is:
````
spigot:
  environment:
    - SPIGOT_ENV=development
````

5. Run your test server with docker-compose:

````
docker-compose up
````
The server will run with a local volume pointing to your latest jar built with Gradle. That means you can /reload inside the server and watch changes without restarting the Spigot server.

# Running a BitQuest server
To run BitQuest might want to do the same steps as with a local test server, but specify a Bitcoin address for your local loot wallet, a BlockCypher API key and (optional) your Mojang account UUID so you are admin in your own server (otherwise you won't have op). To do this, you'll need to create a development.yml file that docker-compose will use to configure your local BitQuest instance.

Here's an example of a development.yml file (please note we use spaces instead of tabs):

````
spigot:
  environment:
    - BITQUEST_ENV=development
    - BITCOIN_ADDRESS=1ERWGdhjHpmanu2ftScpvM8KM4P8Yrxct2
    - BITCOIN_PRIVATE_KEY=a2a2f8b8308e699901d60c567a15633a88362c8f67c9f8a2dc02720c2e18d8a2
    - BLOCKCYPHER_API_KEY=some_api_key
    - ADMIN_UUID=921baf7a-893b-4249-b6a7-ae010ff75551
```` 




You will be able to connect to ````localhost```` in Minecraft, and every time you run the ````shadowJar```` gradle task, following a ````/reload````command inside the game, you'll be playing in your newest compiled code, without restarting or rebuilding the container.

# More info

More info about [BitQuest](https://bitquest.co/) at
https://bitquest.co/
