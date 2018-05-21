FROM debian:stretch
ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update
RUN apt-get install -y wget
RUN apt-get install -y git
RUN apt-get install -y software-properties-common dirmngr default-jdk default-jre

RUN mkdir /bitquest
COPY . /bitquest/
RUN mkdir -p /spigot/plugins

WORKDIR /spigot

# DOWNLOAD AND BUILD SPIGOT
ADD https://hub.spigotmc.org/jenkins/job/BuildTools/64/artifact/target/BuildTools.jar /tmp/BuildTools.jar
RUN export SHELL=/bin/bash && cd /tmp && java -jar BuildTools.jar --rev 1.12.2
RUN cp /tmp/Spigot/Spigot-Server/target/spigot-*.jar /spigot/spigot.jar
RUN cd /spigot && echo "eula=true" > eula.txt
COPY server.properties /spigot/
COPY bukkit.yml /spigot/
COPY spigot.yml /spigot/
RUN apt-get install -y gradle
RUN export SHELL=/bin/bash && cd /bitquest/ && gradle setupWorkspace
RUN cd /bitquest/ && gradle shadowJar
RUN cp /bitquest/build/libs/bitquest-2.0-all.jar /spigot/plugins/
# Add the last version of NoCheatPlus
ADD http://ci.md-5.net/job/NoCheatPlus/lastSuccessfulBuild/artifact/target/NoCheatPlus.jar /spigot/plugins/NoCheatPlus.jar

CMD java -jar spigot.jar
