class:
   maven compile -B -T 1C
jar:
   maven package -B -T 1C

format:
   ./format.sh

clean:
   ./maven clean -B -T 1C
full-clean:
   ./format_and_build.sh clean

all: jar

format-build:
   make format && make jar
