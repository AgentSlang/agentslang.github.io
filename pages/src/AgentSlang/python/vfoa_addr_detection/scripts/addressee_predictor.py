# -*- coding: utf-8 -*-

import pickle
import numpy as np

class AddrPredictor():
    def test_func(self):
        print("This is a test function")
        
    def load_models(self, tc_predictor):
        
        self.addressee_predictor = pickle.load(open(tc_predictor, 'rb'))

    def predict_addr(self, input_sequence):
        
        addressee  =  self.addressee_predictor.predict(input_sequence)
        return addressee

    def data_fv_converter(self, 
                  you_usage, 
                  duration_ms,
                  sentence_length, 
                  focus_speaker,
                  focus_listener_pm,
                  focus_listener_ui,
                  focus_listener_id,
                  focus_listener_me,
                  speaker_role,
                  prev_speaker_role,
                  prev_addr_role,
                  da,
                  prev_da
                  ):
 
         speaker_role_list = ["id", "me", "pm", "ui"]
         prev_speaker_role_list = ["id", "me", "pm", "ui"]
         
         prev_addressee_role_list = ["id", "me", "pm", "ui", "group"]
          
         da_list = ['ass','be.neg', 'be.pos', 'el.ass', 'el.inf', 'el.sug',
                    'el.und', 'inf', 'off', 'sug', 'und',]
         
         prev_da_list = ['ass','be.neg', 'be.pos', 'el.ass', 'el.inf', 'el.sug',
                    'el.und', 'inf', 'off', 'sug', 'und',]
         
         speaker_index =  speaker_role_list.index(speaker_role) + 39
         
         prev_speaker_index =   prev_speaker_role_list.index(prev_speaker_role) + 43
         
         prev_addressee_index = prev_addressee_role_list.index(prev_addr_role) + 47
         
         da_index = da_list.index(da) + 52
         
         prev_da_index = prev_da_list.index(prev_da) + 63
         
         
         focus_speaker_index = 3
         focus_listener_pm_index = 11
         focus_listener_ui_index = 18
         focus_listener_id_index = 25
         focus_listener_me_index = 32
         

         feature_list = [0] * 74
         feature_list [0] = you_usage
         feature_list [1] = duration_ms
         feature_list [2] = sentence_length
         
         feature_list[focus_speaker_index:focus_speaker_index] = focus_speaker
         feature_list[focus_listener_pm_index:focus_listener_pm_index] = focus_listener_pm
         feature_list[focus_listener_ui_index:focus_listener_ui_index] = focus_listener_ui
         feature_list[focus_listener_id_index:focus_listener_id_index] = focus_listener_id
         feature_list[focus_listener_me_index:focus_listener_me_index] = focus_listener_me
         
         feature_list[speaker_index] = 1
         feature_list[prev_speaker_index] = 1
         feature_list[prev_addressee_index] = 1
         feature_list[da_index] = 1
         feature_list[prev_da_index] = 1
         
         feature_list = feature_list[:74]
         feature_vector = np.asarray(feature_list).reshape(1,-1)
      
         return feature_vector