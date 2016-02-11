#!/bin/sh
echo 'Installing gradle and redis if necessary'
sudo apt-get install -y gradle redis-server

# make sure we're running from the scripts directory
if [[ $(echo `pwd` | grep setup_scripts | wc -l) -ne 0 ]] then

	echo 'Running gradle'
	cd ..
	./gradlew shadowJar

	$spigot_file='spigot-1.8.8-R0.1-SNAPSHOT.jar'
	
	echo 'Downloading $spigot_file'

	# place spigot alongside bitquest
	mkdir ../spigot
	cd ../spigot
	wget 'http://jenkins.bitquest.co/job/spigot/lastSuccessfulBuild/artifact/Spigot/Spigot-Server/target/$spigot_file'

	# automatically agree to the eula so we don't have to run spigot twice during setup
	echo 'Agreeing to the Mojang EULA'
	echo '#By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).' > eula.txt
	echo `date` >> eula.txt
	echo 'eula=true' >> eula.txt

	# create the plugins directory
	echo 'Creating the plugins directory and linking the bitquest plugin'
	mkdir plugins
	ln -s ../bitquest/build/libs/bitquest-all.jar plugins/bitquest-all.jar

	cd ..
	echo 'First time setup is complete'

	echo 'Starting spigot and redis to finish file population and check the environment'
	java -jar $spigot_file &
	cd ../bitquest
	redis-server &

	echo "Done. Don't forget to kill spigot and redis when you are finished."

else
	echo 'This script must be run from the setup_scripts directory. Change directories and try again.'
fi