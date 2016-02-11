# BitQuest

[BitQuest](http://bitquest.co/) is an initiative to make a Bitcoin-denominated Minecraft server with MMORPG elements, as the form of a plugin. To play the last version of BitQuest go to ```Multiplayer->Add Server``` and add this server address 
```sh
beta.bitquest.co
```


# How it works?
## Everyone have a wallet
The [BitQuest](https://bitquest.co/) server and every player and have it's own bitcoin wallet with one address. Any player can receive and send bitcoin to any address (even outside the game) making on-chain transactions.
![A player just joined the server](http://i.imgur.com/1A6wkaB.png)
![The playercan see it's bitcoin balance](http://i.imgur.com/5g5pBXB.png)
Thanks to the open nature of bitcoin players can even watch it's transaction history looking for they address in any blockchain explorer like [Insight](https://insight.bitpay.com/) or [Blockchain.Info](https://blockchain.info/).

## Everyone can get loot
Every time a player kills an enemy (mob) there is a chance to get loot. If that is the case the server makes a transaction directly from the server address to the player address and the player is notified.
![A player got loot](http://i.imgur.com/cxqXmt2.png)

## Everyone can send money anywhere
To send transactions players can press ```t``` to open minecraft's command line and type:
```sh
/transfer <recipient-bitcoin-address>
```
![Player using transfer command](http://i.imgur.com/Vlf9C1F.png)
This will make a bitcoin transaction to the recipient's address. That address could be the player personal one, another BitQuest player's one, the server's one or any other.

![Player notification](http://i.imgur.com/PHmomoS.png)
![Player's public transaction](http://i.imgur.com/JPO4AXt.png)
Players can also send money using email instead of a bitcoin address using:
```sh
/transfer <recipient-email>
```

With this method the recipient will receive an email notifying that a bitcoin transaction has been made to a [XAPO wallet](https://xapo.com/wallet/) linked to his email.

## Server address
Server address should be ideally always with bitcoin to transfer loot constantly to players. This address is also used to receive donations.

## About the back-end technology

All persistent data is saved in a redis database so the server can respond as quick as possible. 
All transactions in the game are on-chain using [Blockcypher](http://blockcypher.com/)'s microtransactions API or in case of player-to-email [XAPO API](http://docs.xapo.apiary.io/).

Everybody is welcome to contribute. :D

Here are the instructions to modify, install and run the server as localhost.

# Installation
### Framework
You can use [eclipse](https://eclipse.org/downloads/) or [intellij](https://www.jetbrains.com/idea/) to open and modify this project

### Installing Dependencies
- Be sure you have installed the [last version of java](http://www.java.com/en/download) version 

Note: if you are running a Debian-based Linux distribution like Ubuntu, you can run the setup script included instead of following the rest of these instructions:
#### Ubuntu
```sh
$ cd setup_scripts
$ ./ubuntu.sh
```
Make sure you are in the setup_scripts directory before running the script.

- Install gradle and redis

#### OSX
Install [brew](http://brew.sh/), then run
```sh
$ brew install gradle redis
```
#### Ubuntu
```sh
$ sudo apt-get install gradle redis-server
```


### Compile project
#### OSX and Ubuntu
To compile project go to the bitquest directory and run
```sh
$ ./gradlew shadowJar
````
This will generate a new file at ```bitquest/build/libs/bitquest-all.jar``` that you can use as a plugin in your localhost minecraft server.

### Run server in localhost
#### Spigot
- Make a new directory called ```spigot``` and download there the last [BitQuest's spigot](http://jenkins.bitquest.co/job/spigot/) . If you get an error regarding a missing main manifest attribute, make sure you downloaded the actual spigot jar and not the spigot-api jar.
- Go to this directory and run spigot
#### OSX and Ubuntu
```sh
$ java -jar spigot-1.8.8-R0.1-SNAPSHOT.jar
```
#### OSX and Ubuntu
The first time it runs it will generate a bunch of files and directories.
- Open ```spigot/eula.txt``` and change 
```sh
eula=false
``` 
to
```sh
eula=true
```
- Run spigot again
#### OSX and Ubuntu
```sh
$ java -jar spigot-1.8.8-R0.1-SNAPSHOT.jar
```
Now you should have a new directory ```spigot/plugins/```
- Close the server with ```cmd + C```
- Copy or move ```bitquest/build/libs/bitquest-all.jar``` to ```spigot/plugins/``` or make a symbolic link:

#### OSX and  Ubuntu
```sh
$ ln -s $bitquest/bitquest/build/libs/bitquest-all.jar $spigot/plugins/bitquest-all.jar
```
where ```$bitquest``` is your bitquest root directory and ```$spigot``` is the directory containing your spigot jar.
- Run spigot again to run the server with bitquest plugin


#### OSX and Ubuntu
```sh
$ java -jar spigot-1.8.8-R0.1-SNAPSHOT.jar
```

#### Redis
In the bitquest directory
- Run the redis server


#### OSX and Ubuntu
```sh
$ redis-server
```
Now you will be able to enter to your localhost bitquest minecraft server adding ```localhost``` as server

![Adding localhost as server](http://i.imgur.com/4ZPm0d9.png)


More info about [BitQuest](https://bitquest.co/) at
https://bitquest.co/
