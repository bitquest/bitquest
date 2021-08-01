FROM maven:3.8.1-openjdk-16
ARG SPIGOT_VERSION=1.17.1
ENV DEBIAN_FRONTEND noninteractive
RUN mkdir -p /spigot/plugins
WORKDIR /build
# Download and build spigot
ADD https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar /build/BuildTools.jar
RUN cd /build && java -jar BuildTools.jar --rev $SPIGOT_VERSION
RUN cp /build/Spigot/Spigot-Server/target/spigot-*.jar /spigot/spigot.jar
WORKDIR /spigot
RUN echo "eula=true" > eula.txt
WORKDIR /bitquest
COPY . /bitquest/
RUN mvn clean compile assembly:single
RUN cp /bitquest/target/BitQuest.jar /spigot/plugins/BitQuest.jar
# Add the last version of NoCheatPlus
# ADD http://ci.md-5.net/job/NoCheatPlus/lastSuccessfulBuild/artifact/target/NoCheatPlus.jar /spigot/plugins/NoCheatPlus.jar
WORKDIR /spigot
EXPOSE 25565
COPY server.properties /spigot/
COPY bukkit.yml /spigot/
COPY spigot.yml /spigot/
CMD java -Xmx1024M -Xms1024M -jar spigot.jar
