FROM debian:jessie
RUN echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list
RUN echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN apt-get update
RUN apt-get install -y oracle-java8-installer
RUN apt-get install -y oracle-java8-set-default
RUN apt-get install -y git
RUN mkdir /spigot
RUN mkdir /spigot/plugins
WORKDIR /spigot
RUN cd /spigot/plugins/ && wget http://ci.md-5.net/job/NoCheatPlus/lastSuccessfulBuild/artifact/target/NoCheatPlus.jar
# DOWNLOAD AND BUILD DOWNER
RUN cd /spigot/plugins/ && wget http://jenkins.bitquest.co/job/downer/lastSuccessfulBuild/artifact/build/libs/downer-1.0.jar
# DOWNLOAD AND BUILD SPIGOT
RUN cd /tmp && wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
RUN cd /tmp && java -jar BuildTools.jar --rev 1.9
RUN cp /tmp/Spigot/Spigot-Server/target/spigot-1.9-R0.1-SNAPSHOT.jar /spigot/spigot.jar
RUN cd /spigot && echo "eula=true" > eula.txt
# COPY server-icon.png /spigot/
COPY server.properties /spigot/
COPY build/libs/bitquest-all.jar /spigot/plugins/
CMD java -jar spigot.jar