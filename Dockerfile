FROM debian:stretch

RUN apt-get update
RUN apt-get install -y wget
RUN apt-get install -y git
RUN apt-get install -y software-properties-common dirmngr
RUN add-apt-repository "deb http://ppa.launchpad.net/webupd8team/java/ubuntu xenial main"
RUN apt-get update
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys C2518248EEA14886
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN apt-get install -y --allow-unauthenticated --no-install-recommends oracle-java8-installer

RUN mkdir /bitquest
COPY . /bitquest/
RUN mkdir -p /spigot/plugins

WORKDIR /spigot
RUN wget http://ci.md-5.net/job/NoCheatPlus/lastSuccessfulBuild/artifact/target/NoCheatPlus.jar -O /spigot/plugins/NoCheatPlus.jar

# DOWNLOAD AND BUILD SPIGOT
RUN wget https://hub.spigotmc.org/jenkins/job/BuildTools/64/artifact/target/BuildTools.jar -O /tmp/BuildTools.jar
RUN export SHELL=/bin/bash && cd /tmp && java -jar BuildTools.jar --rev 1.12.2
RUN cp /tmp/Spigot/Spigot-Server/target/spigot-*.jar /spigot/spigot.jar
RUN cd /spigot && echo "eula=true" > eula.txt
COPY server.properties /spigot/
COPY bukkit.yml /spigot/
COPY spigot.yml /spigot/

# Include blockcypher's bcutils
ENV DEBIAN_FRONTEND noninteractive
RUN mkdir /go/
ENV GOPATH /go/
RUN apt-get -y install golang
RUN cd / && git clone https://github.com/blockcypher/btcutils.git
RUN cd / && go get github.com/btcsuite/btcd/btcec
RUN cd /btcutils/signer && go build
RUN chmod +x /btcutils/signer/signer

RUN export SHELL=/bin/bash && cd /bitquest/ && ./gradlew setupWorkspace
RUN cd /bitquest/ && ./gradlew shadowJar
RUN cp /bitquest/build/libs/bitquest-2.0-all.jar /spigot/plugins/

CMD java -jar spigot.jar
