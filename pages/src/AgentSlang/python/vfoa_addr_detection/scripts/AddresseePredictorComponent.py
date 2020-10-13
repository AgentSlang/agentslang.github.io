import argparse
import time
import json
from AgentSlang.AgentSlangComponent import AgentSlangComponent
from addressee_predictor import AddrPredictor

class AddresseePredictorComponent(AgentSlangComponent):
    def __init__(self, in_ip, in_port, in_topic, out_ip, out_port, out_topic):
        # Init ZMQ sockets
        super().__init__(in_ip, in_port, in_topic, out_ip, out_port, out_topic)

        # Init Addressee predictor class
        self.__addr_predictor = AddrPredictor()
        self.__addr_predictor.load_models("data_files/addr_predictor.sav")
    
    def handleData(self, data):
        # Load json 
        # TODO: Add exceptions if some data are missing or wrong
        data_dict = json.loads(data)

        # Addressee Predictor 
        feature_vector = self.__addr_predictor.data_fv_converter(data_dict['you_usage'], 
                    data_dict['duration_ms'],
                    data_dict['sentence_length'], 
                    data_dict['focus_speaker'],
                    data_dict['focus_listener_pm'],
                    data_dict['focus_listener_ui'],
                    data_dict['focus_listener_id'],
                    data_dict['focus_listener_me'],
                    data_dict['speaker_role'],
                    data_dict['prev_speaker_role'],
                    data_dict['prev_addr_role'],
                    data_dict['da'],
                    data_dict['prev_da'])

        addressee = self.__addr_predictor.predict_addr(feature_vector)
        print('AddresseePredictorComponent addressee: %s' % addressee[0])

        # Send processed addressee output
        self.zmq_send_socket.sendMessage(addressee[0])
        

def parseComponentArgs():
    parser = argparse.ArgumentParser()
    parser.add_argument('-in_ip', type = str, dest = 'in_ip', help = 'IP of the ingoing message from the ZeroMQ socket')
    parser.add_argument('-in_port', type = str, dest = 'in_port', help = 'port of the ingoing message from the ZeroMQ socket')
    parser.add_argument('-in_topic_name', type = str, dest = 'in_topic_name', help = 'topic name of the ingoing message from the ZeroMQ socket')
    parser.add_argument('-out_ip', type = str, dest = 'out_ip', help = 'IP of the outgoing message from the ZeroMQ socket')
    parser.add_argument('-out_port', type = str, dest = 'out_port', help = 'port of the outgoing message from the ZeroMQ socket')
    parser.add_argument('-out_topic_name', type = str, dest = 'out_topic_name', help = 'topic name of the outgoing message from the ZeroMQ socket')
    parser_args, unknown = parser.parse_known_args()
    return parser_args

if __name__ == '__main__':
    args = parseComponentArgs()
    # TODO Add error message to indicate why the program has crashed when wrong/lack of command line arguments
    addressee_predictor_component = AddresseePredictorComponent(args.in_ip, args.in_port, args.in_topic_name, args.out_ip, args.out_port, args.out_topic_name)
    addressee_predictor_component.run()
    


    
