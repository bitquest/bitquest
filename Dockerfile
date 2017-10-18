FROM debian:jessie

RUN apt-get update
RUN apt-get install -y wget
RUN apt-get install -y default-jre
RUN apt-get install -y default-jdk
RUN apt-get install -y git

RUN mkdir /bitquest
COPY . /bitquest/
RUN mkdir -p /spigot/plugins

WORKDIR /spigot
RUN wget http://ci.md-5.net/job/NoCheatPlus/lastSuccessfulBuild/artifact/target/NoCheatPlus.jar -O /spigot/plugins/NoCheatPlus.jar

# DOWNLOAD AND BUILD DOWNER
RUN export SHELL=/bin/bash
RUN cd /tmp && git clone https://github.com/bitquest/downer.git
RUN export SHELL=/bin/bash && cd /tmp/downer && ./gradlew setupWorkspace
RUN export SHELL=/bin/bash && cd /tmp/downer && ./gradlew build
RUN cp -rv /tmp/downer/build/libs/*.jar /spigot/plugins

# DOWNLOAD AND BUILD SPIGOT
RUN wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar -O /tmp/BuildTools.jar
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
