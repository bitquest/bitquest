FROM debian:stretch
ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update
RUN apt-get install -y software-properties-common dirmngr openjdk-8-jre-headless maven git build-essential
RUN mkdir -p /spigot/plugins
WORKDIR /build
# DOWNLOAD AND BUILD SPIGOT
ADD https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar /build/BuildTools.jar
RUN cd /build && java -jar BuildTools.jar --rev latest
RUN cp /build/Spigot/Spigot-Server/target/spigot-*.jar /spigot/spigot.jar
WORKDIR /spigot
RUN echo "eula=true" > eula.txt
COPY server.properties /spigot/
COPY bukkit.yml /spigot/
COPY spigot.yml /spigot/
WORKDIR /bitquest
RUN apt-get install -y openjdk-8-jdk
COPY . /bitquest/
RUN mvn clean compile assembly:single
RUN cp /bitquest/target/BitQuest.jar /spigot/plugins/
# Add the last version of NoCheatPlus
ADD http://ci.md-5.net/job/NoCheatPlus/lastSuccessfulBuild/artifact/target/NoCheatPlus.jar /spigot/plugins/NoCheatPlus.jar
ADD https://hub.spigotmc.org/jenkins/job/spigot-essentials/lastSuccessfulBuild/artifact/Essentials/target/Essentials-2.x-SNAPSHOT.jar /spigot/plugins/Essentials.jar
WORKDIR /spigot
CMD java -jar spigot.jar
