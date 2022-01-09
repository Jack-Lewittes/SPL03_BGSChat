# SPL03_BGSChat

1) HOW TO RUN CODE:

	a. SERVER:
		1.  Open terminal from folder "Server"
		2.  Run following commands:
			2.1. TPC:  mvn clean , mvn compile, mvn exec:java -Dexec.mainClass="bgu.spl.net.srv.TPCMain" -Dexec.args="7777"
			2.2. Reactor;  mvn clean, mvn compile, mvn exec:java -Dexec.mainClass="bgu.spl.net.srv.ReactorMain" -Dexec.args="7777 5"
	
	b. Client:
		1. Open terminal from folder "Client"
		2. Run the following commands: make, ./bin/BGSclient 127.0.0.1 7777


2) Filtered words set: can be found in: /Server/src/main/java/bgu/spl/net/srv/msg/Filter



3) Need to know

	a. all commands are written in caps (e.g. REGISTER, LOGIN...), names do not need to be in caps.

	b. birthday dates are dd-mm-yyyy (e.g. 01-01-2022)
