#!/bin/bash

# First step check if user have google format downloaded
if [ -e google-java-format-1.5-all-deps.jar ] ; then
  echo "google java format all ready downloaded"
else
  wget "https://github.com/google/google-java-format/releases/download/google-java-format-1.5/google-java-format-1.5-all-deps.jar"
fi

if [ $(sha512sum -b google-java-format-1.5-all-deps.jar | cut -d" " -f1) == "456a66ec4bc319f843ff05677c1daab9ec6047caebf38fe06c2d427180dd9de4fe3b7ef412e7ff397811dd256678fabf2c578a37a8b062e92d16b704b158f6ed"] ; then
  echo "google java format hash seams good"
else
  echo "google java format hash seams NOT good"
  echo "moving old file to google-java-format-1.5-all-deps.jar.old"
  mv google-java-format-1.5-all-deps.jar google-java-format-1.5-all-deps.jar.old
  wget "https://github.com/google/google-java-format/releases/download/google-java-format-1.5/google-java-format-1.5-all-deps.jar"
fi

java -jar google-java-format-1.5-all-deps.jar -r src/main/bitquest/bitquest/*
java -jar google-java-format-1.5-all-deps.jar -r src/main/bitquest/bitquest/commands/*
