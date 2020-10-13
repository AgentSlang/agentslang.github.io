# -*- coding: utf-8 -*-

import pickle
import numpy as np

class SpkVFOAPredictor():
    
    def load_models(self, num_vfoa_turns_pred, vfoa_dur_pred, vfoa_dir_pred):
        
        self.num_vfoa_turns_pred = pickle.load(open(num_vfoa_turns_pred, 'rb'))
        self.vfoa_dur_pred = pickle.load(open(vfoa_dur_pred, 'rb'))
        self.vfoa_dir_pred = pickle.load(open(vfoa_dir_pred, 'rb'))
   
    
    def generate_vfoa(self, total_duration, input_sequence, spk_role, add_role, pspk_role, padd_role):
        
        m1 = self.num_vfoa_turns_pred.predict(input_sequence)
        m2 = self.vfoa_dur_pred.predict(input_sequence)
        m2 = np.true_divide(m2, m2.sum(axis=1, keepdims=True))
        m3 = self.vfoa_dir_pred.predict(input_sequence)
    
        print("Number of turns:", m1)
        print("VFOA Durations:", m2)
        print("VFOA Directions:", m3)
        
        pred_turnsr = round(m1[0])
        pred_duration = m2.reshape(1,6)
        total_predicted = np.count_nonzero(m3 == 1)
        predicted_index = np.where(m3 == 1)
        
        final_gaze = self.schedule_vfoa(total_duration, total_predicted,  predicted_index[1], pred_duration, pred_turnsr, spk_role, add_role, pspk_role, padd_role)
        print(final_gaze)
        return final_gaze
        
        
    def schedule_vfoa(self, total_duration, total_predicted,  predicted_index, pred_dur, pred_turnsr, c_spk, c_adr, p_spk, p_adr ):
    
        print(total_duration)
        print(predicted_index)
        print(pred_dur)
        print(pred_turnsr)
        index_user = {0:'PM', 1:'ME', 2:'UI', 3:'ID', 4:'Object', 5:'Others'}
        
        #total_duration =  x[0][2]
        
        final_gaze = ""
        if total_predicted == 1:
            focus_object = index_user[predicted_index[0]]
    
            final_gaze = focus_object + ":" + str(total_duration) + " , "
            return final_gaze
        
        
        user_focus_dur = {}
        user_focus_dur_per = {}
       
        for ind in predicted_index:
            
            item = index_user[ind]
            duration = pred_dur[0][ind]
         
            user_focus_dur.update({item:duration})
    
    
            user_focus_dur_per = {k: v / total for total in (sum(user_focus_dur.values()),) for k, v in  user_focus_dur.items()}
            #print(user_focus_dur_per)
            
            final_dictionary = {k:v for k, v in user_focus_dur_per.items() if v > 0.05}
            user_focus_dur_per = {k: v / total for total in (sum(final_dictionary.values()),) for k, v in  final_dictionary.items()}
            #print(user_focus_dur_per)
            
            for key in user_focus_dur_per:
                user_focus_dur_per[key] = user_focus_dur_per[key] * total_duration
       
            
        if (c_spk == p_spk) and (c_adr == p_adr):
            if p_adr in user_focus_dur_per:
                print("previous_addressee_found")
      
                final_gaze = final_gaze + p_adr  + ":" + str(user_focus_dur_per[p_adr]) + " , "
                del user_focus_dur_per[p_adr]
                
      
    
        for key, values in user_focus_dur_per.items():
                final_gaze = final_gaze + key + ":" + str(values) + " , "
                
        
        return final_gaze

      
        
    def data_fv_converter(self, 
                  start_time, 
                  end_time,
                  duration_ms, 
                  speaker_role,
                  addressee_role,
                  prev_addressee, 
                  prev_speaker,
                  da 
                  ):
 
         speaker_role_list = ["id", "me", "pm", "ui"]
         addressee_role_list = ["id", "me", "pm", "ui", "group"]
         
         prev_speaker_role_list = ["id", "me", "pm", "ui"]
         prev_addressee_role_list = ["id", "me", "pm", "ui", "group"]
         
         da_list = ['ass', 'be.neg', 'be.pos', 'el.ass', 'el.inf',
                'el.sug', 'el.und', 'inf', 'off', 'sug', 'und']
         
         
    
         speaker_index =  speaker_role_list.index(speaker_role) + 3
         addressee_index = addressee_role_list.index(addressee_role) + 18
         prev_speaker_index = prev_speaker_role_list.index(prev_speaker) + 28
         prev_addressee_index = prev_addressee_role_list.index(prev_addressee) + 23
         da_index = da_list.index(da) + 7
         
         feature_list = [0] * 32
         feature_list [0] = start_time
         feature_list [1] = end_time
         feature_list [2] = duration_ms
         
         feature_list[speaker_index] = 1
         feature_list[addressee_index] = 1
         feature_list[prev_speaker_index] = 1
         feature_list[prev_addressee_index] = 1
         feature_list[da_index] = 1
         
         feature_vector = np.asarray(feature_list).reshape(1,-1)
      
         return feature_vector