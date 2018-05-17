#!/bin/bash

DIR=$0
if [[ $DIR != /* ]] ; then
  cd $(pwd)/$(dirname $0)
fi

./format.sh
if [ $? != 0 ] ; then
  exit 10
fi
mvn package -B
if [ $? != 0 ] ; then
  exit 20
fi

exit 0
