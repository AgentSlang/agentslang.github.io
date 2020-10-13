import zmq

class ZmqRecvSocket():
    def __init__(self, ip, port, topic):
        self.__context = zmq.Context()
        self.__subscriber = self.__context.socket(zmq.SUB)
        self.__subscriber.connect('tcp://%s:%s' % (ip, port))
        self.__subscriber.subscribe(topic)
        print('ZmqRecvSocket using ip = %s, port = %s and topic = %s READY' % (ip, port, topic))

    def recvMessage(self):
        message = ''
        try:
            message = self.__subscriber.recv_string(zmq.NOBLOCK)
        
        # If no message received, return None
        except zmq.ZMQError:
            message = None
        
        # If message received, return the message
        else:
            message = message.split(' ', 1)[1]
            print('ZmqRecvSocket message received: %s' % message)

        return message

class ZmqSendSocket():
    def __init__(self, ip, port, topic):
        self.__topic = topic

        # Init ZMQ
        self.__context = zmq.Context()
        self.__publisher = self.__context.socket(zmq.PUB)
        self.__publisher.bind("tcp://%s:%s" % (ip, port))
        print('ZmqSendSocket using ip = %s, port = %s and topic = %s READY' % (ip, port, topic))

    def __del__ (self):
        self.__publisher.unbind(self.__publisher.LAST_ENDPOINT)

    def sendMessage(self, msg):
        self.__publisher.send_string("%s %s" % (self.__topic, msg))
        print("ZmqSendSocket message sent: %s" % msg)