#!/bin/bash

#Exit code :
# 0 work
# 1x fail building
# 10 fail formating
# 11 fail building
# 2x fail clean
# 20 fail removing google java format
# 21 fail mvn clean

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
      exit 20
    fi
    mvn clean -B -T 1C
    if [ $? != 0 ] ; then
      exit 21
    fi
    exit 0
  fi
fi

#Code format
./format.sh
if [ $? != 0 ] ; then
  exit 10
fi
#Code build
mvn package -B -T 1C
if [ $? != 0 ] ; then
  exit 11
fi

exit 0
