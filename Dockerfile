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
RUN cd /tmp && git clone https://github.com/bitquest/downer.git
RUN cd /tmp/downer && ./gradlew setupWorkspace && ./gradlew build
RUN cp -rv /tmp/downer/build/libs/*.jar /spigot/plugins
# DOWNLOAD AND BUILD SPIGOT
RUN cd /tmp && wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
RUN cd /tmp && java -jar BuildTools.jar --rev 1.10
RUN cp /tmp/Spigot/Spigot-Server/target/spigot-1.10-R0.1-SNAPSHOT.jar /spigot/spigot.jar
RUN cd /spigot && echo "eula=true" > eula.txt
# COPY server-icon.png /spigot/
COPY server.properties /spigot/
COPY bukkit.yml /spigot/
COPY spigot.yml /spigot/
COPY build/libs/bitquest-2.0-all.jar /spigot/plugins/
CMD java -Xmx8G -Xms8G -jar spigot.jar
