
# Installation
	we use specific versions of these softwares in AgentSlang 
		Java JDK 1.8 (jdk-8u191-windows-x64)
		Python 3
		gradle-4.10.2-bin
		chromedriver
 
# Linux installation
	How to build “agentslang” on Ubuntu? 
	1. Update
		sudo apt-get update & sudo apt-get upgrade
	2. Install dependencies using the following command. 
		sudo apt-get install openjdk-8-jdk git gradle
	5. Create a folder in order to clone the agentslang source code and go to that folder.
	    cd {WORKSPACE}
		mkdir agentslang
		cd agentslang
	6. Clone agentslang sourcecode from INSA git using following command (enter your username and password when you asked for).
		git clone https://github.com/AgentSlang/agentslang.github.io
	7. Go to the following folder inside cloned items.
		# In Workspace
		cd agentslang/agentslang.github.io/pages/src
	8. Give permission to run gradlew bash using the following command.
		chmod u+x gradlew
	9. Build it using the following command.
		./gradlew installDist 
	10. The output execuatble version will be in the following folder.
		{WORKSPACE}/agentslang/agentslang.github.io/pages/src/AgentSlang/build/install/AgentSlang
	 
	 
# Windows installation
	1. Download and install JDK8.
		https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
	2. Define JAVA_HOME variable for JDK path in “Environment Variables”.
		JAVA_HOME = C:\Program Files\Java\jdk1.8.0_191
	3. Download and install TortoiseGit.
		https://tortoisegit.org/download/
	4. Create a folder in order to clone the agentslang source code.
	5. Clone agentslang sourcecode from INSA git using the following URL (enter your username and password when you asked for).
		https://github.com/AgentSlang/agentslang.github.io
	6. Download Gradle and extract it in your system.
		https://gradle.org/releases/
	7. Configure Gradle bin path in “Environment Variables”.
		{Path}\gradle-4.10.2\bin
	8. Open a Command prompt and go to the following folder.
		cd agentslang/agentslang.github.io/pages/src
	9. Build it using the following command.
		gradlew installDist
	10. The output execuatble version will be in the following folder.
		{WORKSPACE}/agentslang/agentslang.github.io/pages/src/AgentSlang/build/install/AgentSlang

# Basic working test
You can verify AgentSlang is working properly by doing
	cd {WORKSPACE}/agentslang/agentslang.github.io/pages/src/AgentSlang/build/install/AgentSlang/bin
	# In GNU/Linux
	./AgentSlang -config ../config/test_configurations/basic_test.xml -profile profile1
	# In Windows
	AgentSlang -config ../config/test_configurations/basic_test.xml -profile profile1

These information should be printed in your terminal
	(INFORM)[org.ib.bricks.Test2] {id=1, language=none, data='Hello-t2:1'}
	(INFORM)[org.ib.bricks.Test2] {id=0, language=none, data='Hello-t1:0'}
	(INFORM)[org.ib.bricks.Test2] {id=1, language=none, data='Hello-t1:1'}
	(INFORM)[org.ib.bricks.Test2] {id=2, language=none, data='Hello-t2:2'}
	(INFORM)[org.ib.bricks.Test2] {id=2, language=none, data='Hello-t1:2'}