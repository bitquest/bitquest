@echo off
rem Must translate this code
rem DIR=$0
rem if [[ $DIR != /* ]] ; then
rem   cd $(pwd)/$(dirname $0)
rem fi

echo For execute this script your pwd must be in the same place than the script.

if exist google-java-format-1.5-all-deps.jar (
    echo google java format all ready downloaded
) else (
    powershell -Command "iwr -outf google-java-format-1.5-all-deps.jar http://github.com/google/google-java-format/releases/download/google-java-format-1.5/"
    echo If you got an error please download and place it here http://github.com/google/google-java-format/releases/download/google-java-format-1.5/google-java-format-1.5-all-deps.jar manualy
)

java -jar google-java-format-1.5-all-deps.jar -r src/main/java/com/bitquest/bitquest/*
java -jar google-java-format-1.5-all-deps.jar -r src/main/java/com/bitquest/bitquest/commands/*
java -jar google-java-format-1.5-all-deps.jar -r src/main/java/com/bitquest/bitquest/events/*
exit /b 0
