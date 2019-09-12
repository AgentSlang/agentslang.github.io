
# Installation
	for running this component you need to install 
	Java JDK 1.8 (jdk-8u191-windows-x64)
	Python 3
	gradle-4.10.2-bin
	chromedriver
 
# Linux installation
	How to build “agentslang” on Ubuntu? 
	1. Update
		apt-get update & apt-get upgrade
	2. Install JDK8 using the following command. 
		sudo apt-get install openjdk-8-jdk
	3. Install Git using the following command.
		sudo apt-get install git
	4. Install Gradle using the following command.
		sudo apt install gradle
	5. Create a folder in order to clone the agentslang source code and go to that folder.
	6. Clone agentslang sourcecode from INSA git using following command (enter your username and password when you asked for).
		git clone https://github.com/AgentSlang/agentslang.github.io
	7. Go to the following folder inside cloned items.
		in the folder {your path}/agemtslang.github.io/pages/src you can find the source code
	8. Give permission to run gradlew bash using the following command.
		chmod u+x gradlew
	9. Build it using the following command.
		./gradlew installDist 
	10. The output execuatble version will be in the following folder.
		{Path}/nareca_final_version/code/AgentSlang/AgentSlang/build/install/AgentSlang

# How to run Automatic Version of agentslang (Auto1muser) on Linux (Ubuntu)? 
	1. Download agentslang executables from the following URL.
		TODO: a link for downloading should be put here.
		Point: If you already have built agentslang from source code, you can directly use the executable you built.
	2. Download MARC Toolkit Version 14.3.0 from the following URL.
		TODO: a link for downloading should be put here.
	3. Install JRE8 using the following command. 
		sudo apt-get install openjdk-8-jre
	4. Install Gstreamer 1.0 using the following command.
		sudo apt-get install libgstreamer1.0-0 gstreamer1.0-plugins-base gstreamer1.0-plugins-good gstreamer1.0-plugins-bad gstreamer1.0-plugins-ugly gstreamer1.0-libav gstreamer1.0-doc gstreamer1.0-tools
	5. In order to use Google ASR it is necessary to follow these steps.
		Install Google Chrome Browser.
		The following command can be used to create the certificate to give to Chrome. When you asked for the certificate information, enter “localhost” for the “Common Name/CN”; the other values do not matter as much.
		openssl req -new -newkey rsa:4096 -days 5000 -nodes -x509 -sha512 -out cert.crt -keyout cert.key
		The resulting files must then be converted into a PKCS #12 file. This can be done by issuing the following command and entering an “empty password” (adjust the output path as needed):
		openssl pkcs12 -export -in cert.crt -inkey cert.key -out {Path}/agentslang/config/cert.p12
		Open Chrome, go to its Settings, then Advanced → HTTPS/SSL → Manage Certificates, go to the "Authorities" tab and import “cert.crt”. 
		Once AgentSlang is running with a configuration file that makes use of this component, the URL “https://localhost:8149/” should be opened in Chrome (this should happen automatically) and permission to use the microphone should be granted, if Chrome asks.
		Download “Chrome driver” from this address. You should download a version of ChromeDriver that corresponds to your Google chrome browser version; otherwise it won’t work.
		http://chromedriver.chromium.org/downloads
		Copy “chromedriver” to the following folder and allow it to be executed as program.
		{Path}/agentslang/bin
	6. Allow “senna-linux64” executable in the following folder to be executed as program.
		{Path}/agentslang/bin/senna/
	7. Run MARC toolkit and launch ”Real time Animation”.
	8. Go to the following folder.
		cd {Path}/agentslang/bin
	9. Give permission to run AgentSlang bash using the following command.
		chmod u+x AgentSlang
	10. Now run “agentslang” using the following command.
		./AgentSlang -config ../config/config-nareca-auto-1m-user.xml -profile profile1
		If you encountered the following execption:
		Exception in thread "main" java.awt.AWTError: Assistive Technology not found: org.GNOME.Accessibility.AtkWrapper
		First use this command.
		sudo sed -i -e '/^assistive_technologies=/s/^/#/' /etc/java-*-openjdk/accessibility.properties
		Then re-run “agentslang”. 
	Recommendation: It is better to have a sound output device to hear the sound of the agent, an input audio device to interact orally with the agent and a webcam+microphone in order to capture voice and video of your experiment.
	 
	 
# How to build “agentslang” on Windows? 
	1.Download and install JDK8.
		https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
	2.Define JAVA_HOME variable for JDK path in “Environment Variables”.
		JAVA_HOME = C:\Program Files\Java\jdk1.8.0_191
	3.Download and install TortoiseGit.
		https://tortoisegit.org/download/
	4.Create a folder in order to clone the agentslang source code.
	5.Clone agentslang sourcecode from INSA git using the following URL (enter your username and password when you asked for).
		https://github.com/AgentSlang/agentslang.github.io
	6.Download Gradle and extract it in your system.
		https://gradle.org/releases/
	7.Configure Gradle bin path in “Environment Variables”.
		{Path}\gradle-4.10.2\bin
	8.Open a Command prompt and go to the following folder.
		in the folder {your path}/agemtslang.github.io/pages/src you can find the source code
	9.Build it using the following command.
		gradlew installDist
	10.The output execuatble version will be in the following folder.
		{Path}/nareca_final_version/code/AgentSlang/AgentSlang/build/install/AgentSlang


# How to run Automatic Version of agentslang (Auto1muser) on Windows? 

	1.Download agentslang executables from the following URL.
		TODO: a link for downloading should be put here.
		Point: If you already have built agentslang from source code, you can directly use the executable you built.
	2.Download and install MARC Toolkit Version 14.3.0 from the following URL. 
		TODO: a link for downloading should be put here.
	3.Download and install JRE8.
		https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
	4.Configure JRE8 bin path in “Environment Variables”.
		{Path}\jre1.8.0_191\bin
	5.Download and install Gstreamer 1.0 (a Complete installation is recommended).
		https://gstreamer.freedesktop.org/data/pkg/windows/1.6.4/
	6.Configure Gstreamer bin path in “Environment Variables”.
		{Path}\gstreamer\1.0\x86_64\bin
	7.In order to use Google ASR it is necessary to follow these steps.
		Install Google Chrome Browser.
		Open Chrome, go to its Settings, then Advanced → HTTPS/SSL → Manage Certificates, go to the "Authorities" tab and import “cert.crt” from the following folder.
		{Path}/agentslang/config/
		Once AgentSlang is running with a configuration file that makes use of this component, the URL “https://localhost:8149/” should be opened in Chrome (this should happen automatically) and permission to use the microphone should be granted, if Chrome asks.
		Download “ChromeDriver” from the following URL. You should download a version of ChromeDriver that corresponds to your Google chrome browser version; otherwise it won’t work.
		http://chromedriver.chromium.org/downloads
		Copy “chromedriver.exe” to the following folder.
		{Path}/agentslang/bin
		Set “chromedriver.exe” path in “Environment Variables”.
		{Path}/agentslang/bin
	8.Run MARC toolkit and launch “Real time Animation”.
	9.Go to the following folder.
		cd {Path}/agentslang/bin
	10.Now run “agentslang” using the following command.
		AgentSlang -config ../config/config-nareca-auto-1m-user.xml -profile profile1

	Recommendation: It is better to have a sound output device to hear the sound of the agent, an input audio device to interact orally with the agent and a webcam+microphone in order to capture voice and video of your experiment.
