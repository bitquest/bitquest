#!/bin/bash

#Setting pwd to our path
DIR=$0
if [[ $DIR != /* ]] ; then
  cd $(pwd)/$(dirname $0)
fi

# First step check if user have google format downloaded
if [ -e google-java-format-1.5-all-deps.jar ] ; then
  echo "google java format all ready downloaded"
else
  wget "https://github.com/google/google-java-format/releases/download/google-java-format-1.5/google-java-format-1.5-all-deps.jar"
  if [ $? != 0 ] ; then
    exit 10
  fi
fi

java -jar google-java-format-1.5-all-deps.jar -r src/main/java/bitquest/bitquest/*
if [ $? != 0 ] ; then
  exit 20
fi
java -jar google-java-format-1.5-all-deps.jar -r src/main/java/bitquest/bitquest/commands/*
if [ $? != 0 ] ; then
  exit 30
fi
exit 0
