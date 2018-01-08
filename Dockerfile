FROM debian:stretch
ENV DEBIAN_FRONTEND noninteractive

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
ADD http://ci.md-5.net/job/NoCheatPlus/lastSuccessfulBuild/artifact/target/NoCheatPlus.jar /spigot/plugins/NoCheatPlus.jar

# DOWNLOAD AND BUILD SPIGOT
ADD https://hub.spigotmc.org/jenkins/job/BuildTools/64/artifact/target/BuildTools.jar /tmp/BuildTools.jar
RUN export SHELL=/bin/bash && cd /tmp && java -jar BuildTools.jar --rev 1.12.2
RUN cp /tmp/Spigot/Spigot-Server/target/spigot-*.jar /spigot/spigot.jar
RUN cd /spigot && echo "eula=true" > eula.txt
COPY server.properties /spigot/
COPY bukkit.yml /spigot/
COPY spigot.yml /spigot/


RUN export SHELL=/bin/bash && cd /bitquest/ && ./gradlew setupWorkspace
RUN cd /bitquest/ && ./gradlew shadowJar
RUN cp /bitquest/build/libs/bitquest-2.0-all.jar /spigot/plugins/

CMD java -jar spigot.jar
