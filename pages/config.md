#	Basic Config File

basic-config.xml
```xml
<project>
    <profile name="profile1" hostname="machine1">
        <scheduler>
            <port>1222</port>
            <timeout>1000</timeout>
        </scheduler>
 
        <services>
            <service name="org.ib.service.cns.CNService">
                <port>1221</port>
                <config>cnsService-basic.xml</config>
            </service>
            <service name="org.ib.service.topic.TopicService">
                <port>1220</port>
            </service>
        </services>
 
        <clients>
            <client name="org.ib.service.cns.CNClient">
                <host>127.0.0.1</host>
                <port>1221</port>
            </client>
            <client name="org.ib.service.topic.TopicClient">
                <host>machine1</host>
                <port>1220</port>
            </client>
        </clients>
 
        <components>
            <component name="org.ib.gui.monitor.MonitorComponent">
                <port>1233</port>
                <scheduler>machine1:1222</scheduler>
                <subscribe>org.ib.bricks.Test1.debug@machine1:1234</subscribe>
                <subscribe>org.ib.bricks.Test2.debug@machine1:1235</subscribe>
                <subscribe>org.ib.bricks.Test2.debug@machine1:1236</subscribe>
                <subscribe>org.ib.bricks.Test1.heartbeat@machine1:1234</subscribe>
                <subscribe>org.ib.bricks.Test2.heartbeat@machine1:1235</subscribe>
                <subscribe>org.ib.bricks.Test2.heartbeat@machine1:1236</subscribe>
            </component>
 
            <component name="org.ib.bricks.Test1">
                <port>1234</port>
                <scheduler>machine1:1222</scheduler>
                <publish>StringData.Test1@test1</publish>
                <publish>StringData.Test2@test2</publish>
            </component>
 
            <component name="org.ib.bricks.Test2">
                <port>1235</port>
                <scheduler>machine1:1222</scheduler>
                <subscribe>StringData.Test1@machine1:1234</subscribe>
            </component>
 
            <component name="org.ib.bricks.Test2">
                <port>1236</port>
                <scheduler>machine1:1222</scheduler>
                <subscribe>StringData.Test2@machine1:1234</subscribe>
            </component>
 
            <component name="org.ib.component.SystemMonitorComponent">
                <port>1237</port>
                <scheduler>machine1:1222</scheduler>
                <subscribe>StringData.Test2@machine1:1234</subscribe>
            </component>
        </components>
    </profile>
</project>
```

# 2.	Notes
Agentslang uses a message passing mechanism in order to exchange data inside different modules as well as interacting with external world. Each component receives data using a subscription to a topic. Afterward, that component processes received data and publishes its output. This output can be used by another component or external application using a subscription to that topic as mentioned previously. 
There are 4 important parts of the config file :

1.The profile tag, defined under the name attribute. The profile has one hostname attached. For now, we consider just a single machine configuration. For multiple machines and advanced configurations of the CNService please refer to “How to run WoZ 2 Machines Version of agentslang for windows or Linux” tutorials.
```xml
<profile name="profile1" hostname="machine1">
```
2.The profile name (profile1) is used by the command line tool start.sh (start.bat).
3.The scheduler is one important component that provides the system with a synchronized heartbeat. It has a port associated so that the other components can connect.
```xml
<scheduler>
  <port>1222</port>
  <timeout>1000</timeout>
</scheduler>
```
4.	Services / Clients: We’ll leave the services and clients to the basic configuration for now. The CNService advanced configuration is described in the “How to run WoZ 2 Machines Version of agentslang for windows or Linux” tutorials.
5.	Components

```xml
<component name="Component1">
  <port>pubPortID</port>
  <scheduler>schedMachineName:schedPort</scheduler>
  <subscribe>…</subscribe>
  <publish>pubTopicName@OutputChannel</publish>
</component>
<component name="Component2">
  <port>subPortID</port>
  <scheduler>schedMachineName:schedPort</scheduler>
  <subscribe>pubTopicName@schedMachineName:pubPortID</subscribe>
  <publish>…</publish>
</component>
```
•	component name is the exact name of the Java class implementing the component, including the package name.

•	port is the unique port identifier of the component. It has to be a valid unused TCP port.

•	scheduler is the identification of the scheduler where the component should connect.

•	subscribe refers to the subscription topics. The subscription topics have to be previously publish by another component. As you can see in before mentioned example, it consists of the following format:
a topic name which is published by another component before+ “@” + Publisher machine name + “:” + Port ID of the publisher machine. 

•	publish refers to all the external topics that are published by a component. It includes a publishing topic name and an output channel. 

3.	Components 

Here you can find the list of available components of agentslang with their potential input and output topics. You can make your own configuration of agentslang just by putting right components together to publish and receive data from each other in order to reach your goal.
In some components there are some mandatory or optional configuration parameters in addition to regular input and output parameters. Setting values for the optional ones is optional and mandatory for mandatory ones. Take the abstract example for more clarifications.
```
org.agent.slang.test.Component1
```
Mandatory Configuration Parameters: 
```
manParam1,
manParam2
```
Optional Configuration Parameters: 
```
opParam1
```
Input Data Types: 
```java
Output Channels	Output Data Types
```

Their values can be defined like the following example:

```xml
<component name=" org.agent.slang.test.Component1">
  <port>PortID</port>
  <scheduler>schedMachineName:schedPort</scheduler>
  <manParam1>value_manParam1</manParam1>
  <manParam2>value_manParam2</manParam2>
  <opParam1>value_opParam1</opParam1>
  <subscribe>…</subscribe>
  <publish>…</publish>
</component>
```
Note: In order to have a better understanding of how to create a new configuration, it is recommended to read “How to run …” tutorials first.

Here is the list of our components.
org.agent.slang.annotation.MELtComponent
Mandatory Configuration Parameters: 
meltPath

Optional Configuration Parameters: 
```java
meltParams

Input Data Types: 

StringData.class   
     
Output Channels	Output Data Types
melt.data	GenericTextAnnotation.class
org.agent.slang.annotation.MetaphoneEncodingComponent
Input Data Types: 
StringData.class, 
GenericTextAnnotation.class  
   
Output Channels	Output Data Types
metaphone.data	GenericTextAnnotation.class
org.agent.slang.annotation.MorfetteComponent
Mandatory Configuration Parameters: 
morfettePath

Optional Configuration Parameters: 
morfetteParams

Input Data Types: 
StringData.class 

Output Channels	Output Data Types
morfette.data	GenericTextAnnotation.class
org.agent.slang.annotation.SennaComponent
Mandatory Configuration Parameters: 
sennaPath

Optional Configuration Parameters: 
sennaParams

Input Data Types: 
StringData.class 

Output Channels	Output Data Types
senna.data	GenericTextAnnotation.class
org.agent.slang.annotation.TreeTaggerComponent
Mandatory Configuration Parameters: 
treeTaggerPath

Optional Configuration Parameters: 
treeTaggerParams

Input Data Types: 
StringData.class 

Output Channels	Output Data Types
treeTagger.data	GenericTextAnnotation.class
org.agent.slang.decision.DecisionMakerComponent
Mandatory Configuration Parameters: 
modelPath

Input Data Types:
StringData.class,
GenericTextAnnotation.class,
StateChangeData.class,
PlayerEvent.class,
CommandData.class,
BmlData.class

Output Channels	Output Data Types
text.data
senna.data
stateChange.data
audioPlayer.data
command.data
bml.data	StringData.class,
GenericTextAnnotation.class,
StateChangeData.class,
PlayerEvent.class,
CommandData.class,
BmlData.class
org.agent.slang.dm.narrative.HandleCommandComponent
Mandatory Configuration Parameters: 
modelPath

Input Data Types:
StateChangeData.class, 
CommandData.class, 
MousePositionData.class

Output Channels	Output Data Types
command.data,
bml.data, 
mouse.data	StringData.class, 
BmlData.class, 
MousePositionData.class
org.agent.slang.dm.narrative.PatternMatchingComponent
Mandatory Configuration Parameters: 
modelPath,
dictionaryConfig

Input Data Types:
GenericTextAnnotation.class, 
PlayerEvent.class, 
SystemEvent.class, 
StateChangeData.class, 
StringData.class

Output Channels	Output Data Types
response.data,
command.data,
stateChange.data	StringData.class, 
CommandData.class, 
StateChangeData.class
org.agent.slang.dm.narrative.graph.OutOfContextComponent
Mandatory Configuration Parameters: 
modelPath

Output Channels	Output Data Types
stateChange.data	StateChangeData.class
org.agent.slang.dm.narrative.graph.StoryGraphComponent
Mandatory Configuration Parameters: 
modelPath

Input Data Types:
StateChangeData.class

Output Channels	Output Data Types
stateChange.data	StateChangeData.class
org.agent.slang.feedback.ValenceExctractorComponent
Input Data Types:
GenericTextAnnotation.class

Output Channels	Output Data Types
valence.data	ValenceData.class
org.agent.slang.in.EyeTracking.TobiiEyeTrackingComponent
Output Channels	Output Data Types
tobiiEyeTracking.data	StringData.class
org.agent.slang.in.facereader.FaceReaderComponent
Optional Configuration Parameters: 
FRaddress,
FRport

Output Channels	Output Data Types
facereader.data	ClassificationData.class
org.agent.slang.in.google.GoogleASRComponent
Mandatory Configuration Parameters: 
language,
certificate
Input Data Types:
PlayerEvent.class, 
StateChangeData.class

Output Channels	Output Data Types
voice.data	StringData.class
org.agent.slang.in.proxy.VoiceProxyComponent
Optional Configuration Parameters: 
voiceProxy,
voiceBTuuid, 
voiceBTmac
 
Output Channels	Output Data Types
voice.data	StringData.class
org.agent.slang.in.SpeechRecognizer.SpeechRecognitionComponent
Optional Configuration Parameters: 
SpeechRecognizerHostname, 
SpeechRecognizerInPort, 
SpeechRecognizerOutPort

Input Data Types:
PlayerEvent.class

Output Channels	Output Data Types
voice.data	StringData.class
org.agent.slang.in.videoconferencing.VCStreamer
Mandatory Configuration Parameters: 
targets

Optional Configuration Parameters: 
ipcamerafrom, 
ipcamerato, 
saveTo
org.agent.slang.inout.TerminalIOComponent
Optional Configuration Parameters: 
inputFilename, 
outputFilename, 
audioCache, 
inputMultiline

Input Data Types:
GenericData.class

Output Channels	Output Data Types
terminal.data	StringData.class

org.agent.slang.inout.TerminalIOComponentHack
Mandatory Configuration Parameters: 
inputFilename, 
audioCache 

Input Data Types:
GenericData.class

Output Channels	Output Data Types
text.data	StringData.class
org.agent.slang.inout.TextComponent
Input Data Types:
GenericData.class

Output Channels	Output Data Types
text.data, 
audioPlayer.data	StringData.class, 
PlayerEvent.class
org.agent.slang.Learning.Dialogue.ClassifierComponent
Mandatory Configuration Parameters: 
classifierPath 

Input Data Types:
StringData.class,
PlayerEvent.class

Output Channels	Output Data Types
text.data,
classifierSignal.data	StringData.class,
StringData.class

org.agent.slang.out.bml.marc.MarcBMLAutonomousComponent
Mandatory Configuration Parameters: 
bmlFilePath

Input Data Types:
StringData.class, 
StateChangeData.class

Output Channels	Output Data Types
bmlCommand.data	StringData.class
org.agent.slang.out.bml.marc.MarcBMLSTranslationComponent
Input Data Types:
AudioData.class, 
StringData.class

Output Channels	Output Data Types
audioPlayer.data,
bmlCommand.data	PlayerEvent.class, 
StringData.class
org.agent.slang.out.bml.marc.MarcBMLTranslationComponent
Optional Configuration Parameters: 
MARCSocketType, 
MARCHostname, 
MARCInPort, 
MARCOutPort

Input Data Types:
BmlData.class,
AudioData.class, 
StateChangeData.class, 
StringData.class

Output Channels	Output Data Types
audioPlayer.data 	PlayerEvent.class
org.agent.slang.out.cereproc.CereProcTTSComponent
Mandatory Configuration Parameters: 
voice, 
licenseFile 

Input Data Types:
StringData.class,
GenericTextAnnotation.class

Output Channels	Output Data Types
voice.data	AudioData.class

org.agent.slang.out.ispeech.ISpeechTTSComponent
Mandatory Configuration Parameters: 
voice, 
apiKey

Input Data Types:
StringData.class,
GenericTextAnnotation.class

Output Channels	Output Data Types
voice.data	AudioData.class
org.agent.slang.out.marytts.MaryComponent
Optional Configuration Parameters: 
Locale

Input Data Types:
StringData.class,
GenericTextAnnotation.class

Output Channels	Output Data Types
voice.data	AudioData.class
org.agent.slang.out.videoconferencing.VCDisplay
Mandatory Configuration Parameters: 
basePort


```