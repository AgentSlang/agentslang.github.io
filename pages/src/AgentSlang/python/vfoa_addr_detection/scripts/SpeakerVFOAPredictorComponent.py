import argparse
import time
import json
from AgentSlang.AgentSlangComponent import AgentSlangComponent
from speaker_VFOA_predictor import SpkVFOAPredictor

class SpeakerVFOAPredictorComponent(AgentSlangComponent):
    def __init__(self, in_ip, in_port, in_topic, out_ip, out_port, out_topic):
        # Init ZMQ sockets
        super().__init__(in_ip, in_port, in_topic, out_ip, out_port, out_topic)

        # Init Speaker VFOA predictor class
        self.__spk_vfoa_predictor = SpkVFOAPredictor()
        self.__spk_vfoa_predictor.load_models("data_files/spk_num_vfoa_turns.sav","data_files/spk_vfoa_dur.sav","data_files/spk_vfoa_dir.sav")
    
    def handleData(self, data):
        # Load json 
        # TODO: Add exceptions if some data are missing or wrong
        data_dict = json.loads(data)
        start_time = data_dict['start_time']
        end_time = data_dict['end_time']
        duration_ms = data_dict['duration_ms']
        speaker_role = data_dict['speaker_role']
        addressee_role = data_dict['addressee_role']
        prev_addressee = data_dict['prev_addressee']
        prev_speaker = data_dict['prev_speaker']
        da = data_dict['da']
        
        # Gaze data generation 
        feature_vector = self.__spk_vfoa_predictor.data_fv_converter(start_time, end_time, duration_ms, speaker_role, addressee_role, prev_addressee, prev_speaker, da)
        final_gaze = self.__spk_vfoa_predictor.generate_vfoa(duration_ms, feature_vector, speaker_role, addressee_role, speaker_role, prev_addressee)
        print('SpeakerVFOAPredictorComponent processed_gaze: %s' % final_gaze)
        
        # Send final_gaze_info
        self.zmq_send_socket.sendMessage(final_gaze)

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
    speaker_vfoa_predictor_component = SpeakerVFOAPredictorComponent(args.in_ip, args.in_port, args.in_topic_name, args.out_ip, args.out_port, args.out_topic_name)
    speaker_vfoa_predictor_component.run()
