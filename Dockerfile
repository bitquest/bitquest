FROM debian:jessie
# Default 'httpredir' debian source creates all sorts of crazy problems when your closest mirror is shit
RUN sed -i 's%httpredir.debian.org%ftp.debian.org%' /etc/apt/sources.list
RUN echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list
RUN echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN apt-get update
RUN apt-get install -y oracle-java8-installer
RUN apt-get install -y oracle-java8-set-default
RUN apt-get install -y git
RUN mkdir /bitquest
RUN mkdir /spigot/
RUN mkdir /spigot/plugins
WORKDIR /spigot
RUN cd /spigot/plugins/ && wget http://ci.md-5.net/job/NoCheatPlus/lastSuccessfulBuild/artifact/target/NoCheatPlus.jar
# DOWNLOAD AND BUILD DOWNER
RUN export SHELL=/bin/bash
RUN cd /tmp && git clone https://github.com/bitquest/downer.git
RUN export SHELL=/bin/bash && cd /tmp/downer && ./gradlew setupWorkspace
RUN export SHELL=/bin/bash && cd /tmp/downer && ./gradlew build
RUN cp -rv /tmp/downer/build/libs/*.jar /spigot/plugins
# DOWNLOAD AND BUILD SPIGOT
RUN cd /tmp && wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
RUN export SHELL=/bin/bash && cd /tmp && java -jar BuildTools.jar
RUN cp /tmp/Spigot/Spigot-Server/target/spigot-*.jar /spigot/spigot.jar
RUN cd /spigot && echo "eula=true" > eula.txt
# COPY server-icon.png /spigot/
COPY server.properties /spigot/
COPY bukkit.yml /spigot/
COPY spigot.yml /spigot/
# Include region fixer in /utils/ to fix corrupted worlds
RUN mkdir /utils/
RUN cd /utils/ && git clone https://github.com/Fenixin/Minecraft-Region-Fixer
COPY . /bitquest/
RUN export SHELL=/bin/bash && cd /bitquest/ && ./gradlew setupWorkspace
RUN cd /bitquest/ && ./gradlew shadowJar
RUN cp /bitquest/build/libs/bitquest-2.0-all.jar /spigot/plugins/
CMD java -jar spigot.jar