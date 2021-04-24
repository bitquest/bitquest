jar:
	mvn clean compile assembly:single
lint:
	checkstyle -c checkstyle.xml .