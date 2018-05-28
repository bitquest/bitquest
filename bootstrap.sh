#!/usr/bin/env bash
export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get -y install python-software-properties
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections
add-apt-repository -y ppa:webupd8team/java
apt-get update
apt-get install -y oracle-java8-installer
rm -rf /var/lib/apt/lists/*
rm -rf /var/cache/oracle-jdk8-installer
apt-get -y install redis-server
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys E1DD270288B4E6030699E45FA1715D88E1DF1F24
echo 'deb http://ppa.launchpad.net/git-core/ppa/ubuntu trusty main' > /etc/apt/sources.list.d/git.list

apt-get update
apt-get -y install git
cd /tmp
wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar -O BuildTools.jar && java -jar BuildTools.jar --rev 1.9
cp /tmp/Spigot/Spigot-Server/target/spigot-1.9-R0.1-SNAPSHOT.jar /minecraft
chown vagrant /minecraft
