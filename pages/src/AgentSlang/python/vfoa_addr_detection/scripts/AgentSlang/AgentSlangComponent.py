import time
from AgentSlang.ZmqSockets import ZmqRecvSocket, ZmqSendSocket

# Similar to JAVA AgentSlang mixed component
class AgentSlangComponent():
    def __init__(self, in_ip, in_port, in_topic, out_ip, out_port, out_topic):
        self.zmq_recv_socket = ZmqRecvSocket(in_ip, in_port, in_topic)
        self.zmq_send_socket = ZmqSendSocket(out_ip, out_port, out_topic)

    def handleData(self, data):
        pass

    def run(self):
        while (1):
            message = self.zmq_recv_socket.recvMessage()
            if message != None:
                self.handleData(message)
            else:
                time.sleep(0.1) # TODO Add time sleep parameter, here 0.1s

            


