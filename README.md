# BitQuest

[BitQuest](https://bitquest.co/) is an initiative to make a Bitcoin-denominated Minecraft server with MMORPG elements, as the form of a plugin.


Everybody is welcome to contribute. :D

# Installation
## OSX
### Framework
You can use [eclipse](https://eclipse.org/downloads/) or [intellij](https://www.jetbrains.com/idea/) to open and modify this project

### Installing Dependencies
- Be sure you have installed the [last version of java](http://www.java.com/en/download) version 
- Install [brew](http://brew.sh/)
- Install gradle running
```sh
$ brew install gradle
```
- Install redis running
```sh
$ brew install redis
```

### Compile project
To compile project go to the bitquest directory and run
```sh
$ ./gradlew shadowJar
````
This will generate a new file at ```bitquest/build/libs/bitquest-all.jar``` that you can use as a plugin in your localhost minecraft server.

### Run server in localhost
#### Spigot
- Make a new directory called ```spigot``` and download there the last [bitquest's spigot](http://jenkins.bitquest.co/job/spigot/) .
- Go to this directory and run spigot
```sh
$ java -jar spigot-1.8.8-R0.1-SNAPSHOT.jar
```
If is the first time this will generate a bunch of files and directories.
- Open ```spigot/eula.txt``` and change 
```sh
eula=false
``` 
to
```sh
eula=true
```

- Copy or move your ```bitquest/build/libs/bitquest-all.jar``` file to ```spigot/plugins/```

- Run spigot again
```sh
$ java -jar spigot-1.8.8-R0.1-SNAPSHOT.jar
````

#### Redis
In the bitquest directory
- Run the redis server
```sh
$ redis-server
```
Now you will be able to enter to your localhost bitquest minecraft server adding ```localhost``` as server

![Alt text](http://i.imgur.com/4ZPm0d9.png)


More info about [BitQuest](https://bitquest.co/) at
https://bitquest.co/
