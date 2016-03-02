#!/usr/bin/env bash
export DEBIAN_FRONTEND=noninteractive
apt-get -y update
apt-get -y install python-software-properties
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections
add-apt-repository -y ppa:webupd8team/java
apt-get update
apt-get install -y oracle-java8-installer
rm -rf /var/lib/apt/lists/*
rm -rf /var/cache/oracle-jdk8-installer
apt-get -y install redis-server
apt-get -y install git-core
cd /tmp
wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar -O BuildTools.jar && java -jar BuildTools.jar --rev 1.9
mkdir /minecraft
