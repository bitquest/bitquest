@echo off
rem Must translate this code
rem DIR=$0
rem if [[ $DIR != /* ]] ; then
rem   cd $(pwd)/$(dirname $0)
rem fi

echo For execute this script your pwd must be in the same place than the script.

rem #if script is called as */build.sh clean, clean
rem if [ $# == 1 ] ; then
rem   if [ $1 == "clean" ] ; then
rem     rm -f google-java-format-1.5-all-deps.jar*
rem     if [ $? != 0 ] ; then
rem       exit 30
rem     fi
rem     mvn clean -B
rem     if [ $? != 0 ] ; then
rem       exit 31
rem     fi
rem     exit 0
rem   fi
rem fi

format.bat
mvn package -B
exit /b 0
