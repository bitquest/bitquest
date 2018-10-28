class:
	maven compile -B -T 1C
jar:
	mvn clean compile assembly:single

format:
	./format.sh

clean:
	./maven clean -B -T 1C
full-clean:
	./format_and_build.sh clean

all: jar

format-build:
	make format && make jar
