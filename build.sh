#!/bin/bash

#Setting pwd to our path
DIR=$0
if [[ $DIR != /* ]] ; then
  cd $(pwd)/$(dirname $0)
fi

#if script is called as */build.sh clean, clean
if [ $# == 1 ] ; then
  if [ $1 == "clean" ] ; then
    rm -f google-java-format-1.5-all-deps.jar*
    if [ $? != 0 ] ; then
      exit 30
    fi
    mvn clean -B
    if [ $? != 0 ] ; then
      exit 31
    fi
  fi
fi

#Code format
./format.sh
if [ $? != 0 ] ; then
  exit 10
fi
#Code build
mvn package -B
if [ $? != 0 ] ; then
  exit 11
fi

exit 0
