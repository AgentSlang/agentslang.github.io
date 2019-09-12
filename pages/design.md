# Data Oriented design

Even if the formats do not have to be strictly identical between linked components, the compatibility has to be ensured at least. There are two main directions in this area:

- Design data to have a small transfer size and memory footprint
- Generic data representation, written in standard formats (i.e. JSON or XML)

[Google Protocol Buffers](https://developers.google.com/protocol-buffers/) presents a comparison between their own serialization format and the XML parsing and conclude that their format is 10 to 100 times faster than XML. [MsgPack](http://msgpack.org/) follows the same direction of small memory footprint data representation, by offering better performance than Google Protocol Buffers, while maintaining the multi-language binding and multi-platform support.

We therefore propose to define our own Object-Oriented data representation. Thanks to object hierarchies, objects are extensible and independent from the data serialization level. This allows us to change the serialization level in the future if needed. Currently, the serialization is done with MsgPack due to the best performance. The data has a small memory footprint than in the case of XML or JSON, has very fast serialization mechanisms and offers the advantages of working with native Object Structures rather than XML trees or JSON maps.

## MyBlock Components

A component is an atomic structure for the MyBlock platform. The component processes a given set of data types and forward the output to the next component in the chain. The internal flow of a component can have two different aspects: either it is a reactive output to the input, or an active component that can produce output based on the internal states without any input. Special components are elements which only consume (Sink) or produce (Source), without any mixed function.

At this level, data types are an important aspect. The data exchanged need to be compatible between linked elements. A component formally defines preconditions and post-conditions in terms of data types, in order to be linked with other components to form complex processing pipes. The communication protocol between two components is a simple publish-subscribe architecture to ease data exchange.

## MyBlock Services

Similarly to the component, we define the services. A service is designed is to respond to requests that can be triggered by any component or other service. The communication protocol used for services is a synchronous request-reply.
AgentSlang Components

![System diagram](/assets/images/system-diag.svg)

Most of the basic components are based on existing libraries, such as:

- [Google Speech API](https://dvcs.w3.org/hg/speech-api/raw-file/tip/speechapi.html) for Automatic Speech Recognition
- [CereProc Voice](http://www.cereproc.com/) for speech synthesis
- various Part-of-Speech Taggers ([SENNA](http://ronan.collobert.com/senna/), [TreeTagger](http://www.cis.uni-muenchen.de/~schmid/tools/TreeTagger/))
- [MARC](http://marc.limsi.fr/) as an Embodied virtual Agent

Our proposal includes:

- Syn!bad for Synonym-based keyword extraction
- Simple Dialogue Manager for narrative models
- Python component, you can send and recive data via UDP connection and run some component in the Python 
- Video Conferencing, for recive streaming video from another machine
- Audio Player, for play a audio in Java
- Eye Tracking with using Toobi eye tracker
- Head & Eye position with using OpenFace 
- Face Reader for using Face reader software to extract the Emotion and some feature from the face

