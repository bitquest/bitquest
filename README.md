BitQuest A Minecraft mod that connects the game economics to a Bitcoin or other cryptocurrency nodes. It creates a Bitcoin wallet for every user that connects to the server, plus a common "loot" wallet used for rewards to players when killing mobs.

Players also can purchase land plots and stacks of items, transferring coins to the loot wallet, thus redistributing the spent cryptocurrency within the players.

You can use BitQuest to host your own Minecraft crypto server.

# Important Note
This project is an experiment and not a commercial product, bugs can, and most likely will happen. Please report any bugs to a moderator promptly, to ensure they are fixed. Also note that your wallet is not guaranteed, and you may experience issues with your balance. For this reason, it is highly recommended that you do not deposit large amounts, or anything you do not wish to place at risk by being tied to the server. In addition, if you are caught breaking rules and are banned, you forfeit your access to your server wallet, and all funds tied to it. With this said, rules are non-negotiable, and will be strictly enforced. This server is a fair server, and anyone abusing any system set in place will be punished. Please play fair, have fun, and enjoy the server

# Features
## Bitcoin Wallet
The [BitQuest](https://bitquest.co/) server and every player has a bitcoin address. Any player can receive and send bitcoin to any address inside or outside the game. This is useful for buying materials, selling crafts, trading, tipping, etc.
![A player just joined the server](http://i.imgur.com/1A6wkaB.png)
![The player can see it's bitcoin balance](http://i.imgur.com/5g5pBXB.png)

## Loot
Every time a player kills an enemy (mob) there is a chance to get loot. If that is the case the server makes a transaction directly from the server address to the player address.
![A player got loot](http://i.imgur.com/cxqXmt2.png)

## Player to player transactions
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

# Play Bitquest

Official BitQuest server is not online anymore, but you can start your own server using Docker or Bukkit.

# Start a BitQuest server
There are two methods to create a Bitquest server: One involves running a Docker image with everything ready, and the other is to build the plugin from source and adding it to a Spigot instance.

## Using Docker
You can download the a (https://hub.docker.com/repository/docker/explodi/bitquest)[official BitQuest docker image]. 
This image builds a bukkit server with the latest BitQuest version plugin installed.

You must also have a node running, and the connection info (RPC login, host, port) set in the environment variables. (See: configuration)

## Building the BitQuest Java Plugin
You can build the bitquest Java plugin that you can drop in to a Bukkit server. This will enable all BitQuest features on your server. 

### 1. Requirements

1. Java JRE+SDK (Version 1.8)
2. Maven


### 2. Compile BitQuest and generate a JAR file

```
make -B jar
```

This will create a BitQuest.jar in the target folder.

# Contributing
Before submitting a pull request, please format the code using checkstyle. To run checkstyle and check if there are warnings:

````
make -B lint
````

# Running a BitQuest server with Docker

Tou can use Docker to run a BitQuest server. There's also an [official BitQuest docker image](https://hub.docker.com/r/bitquest/bitquest/). The recommended way to configure the image is using a docker-compose.yml file that can link to a directory where the worlds are stored. An example is included here. 

## Configuration

Before running the server, the following environment variables must be set:

| Environment variable       | Description                         |
|----------------------------|-------------------------------------|
| BITQUEST_ADMIN_UUID        | Minecraft user ID for the root user |
| BITQUEST_NAME              | The name of your server             |
| BITQUEST_NODE_HOST         | Host of the Node                    |
| BITQUEST_NODE_PORT         | Port of the Node                    |
| BITQUEST_NODE_RPC_USER     | Node RPC username                   |
| BITQUEST_NODE_RPC_PASSWORD | Node RPC password                   |

# More info

https://bitquest.co/
