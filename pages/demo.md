# Basic demos

## Basic working test

To ensure AgentSlang is correctly installed, you can use the basic_test.xml test configuration.

```
# Inside bin directory of AgentSlang installation folder
cd ${AGENTSLANGINSTALLDIR}/bin

# In GNU/Linux
./AgentSlang -config ../config/test_configurations/basic_test.xml -profile profile1

# In Windows
AgentSlang -config ../config/test_configurations/basic_test.xml -profile profile1
```

If everything's working fine, you should have this output in your terminal
```
(INFORM)[org.ib.bricks.Test2] {id=1, language=none, data='Hello-t2:1'}
(INFORM)[org.ib.bricks.Test2] {id=0, language=none, data='Hello-t1:0'}
(INFORM)[org.ib.bricks.Test2] {id=1, language=none, data='Hello-t1:1'}
(INFORM)[org.ib.bricks.Test2] {id=2, language=none, data='Hello-t2:2'}
(INFORM)[org.ib.bricks.Test2] {id=2, language=none, data='Hello-t1:2'}
```

# Advanced demos

##  Addressee Detector and VFOA Prediction
Currently the code can work to predict addressee and visual focus of attention during an  interaction that involves up to 4 participants and up to three objects. Another category `other` is also introduced for the focus when none of the participants and objects are in focus. The participants names are PM, UI, ME and ID where as the objects are table, whiteboard and side screen.

These two components require dependencies:
```
python3 -m pip install xgboost==0.90
python3 -m pip install scikit-learn==0.22.2
```

### **1. Addressee detector**
The addressee predictor is a stand alone component which predicts the addressee of the current utterance. The addressee predictor is implemented in the `addressee_predictor.py` file.  The `addressee_predictor.py`   file containing the addressee predictor module consists of one class named `AddrPredictor` with the following functions.

#### 1.1. load_models()

The function loads the pickle object containing machine learning module for addressee detection. The function stores the loaded machine learning module in the `self.addressee_predictor` variable and returns void. 

The parameters of the `load_model()` function are as follows.

| Parameter Name (type) | Description                      | Possible Values                        |
| --------------------- | -------------------------------- | -------------------------------------- |
| tc_predictor (string) | string path to the pickle object | Any string value containing file path. |



#### 1.2. data_fv_converter()

Converts individual  input parameters to a complete feature vector that can be passed to machine learning module for addressee prediction. The function returns a one dimensional numpy array representing all the features. The parameters of the  `data_fv_converter()` function are as follows. 

**Note:** These attributes should be passed in order of their presence in the table.



| Parameter Name (type)      | Description                                                  | Possible Values                                              |
| -------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| you_usage (binary)         | whether or not a sentence contains the word you              | 1 For True<br />0 For False                                  |
| duration_ms (floating)     | duration of utterance in seconds                             | any floating value                                           |
| sentence_length (integer)  | number of words in the utterance                             | any integer value                                            |
| focus_speaker (list)       | A list of 8 floating items where each item shows ratio of speaker focus towards participants and objects.  The index order is [0 = UI, 1 = ME, 2 = PM, , 3 = ID , 4 =table , 5 = slide screen, 6 = whiteboard , 7 = other  ] | Any floating  point list of 8 items.  For example [0.14227, 0, 0, 0, 0.186047, 0.61683, 0, 0]. |
| focus_listener_pm (list)   | A list of 7 floating items where each item shows ratio of PM focus while listening towards participants and objects. The index order is  [0 = UI, 1 = ME, 2 = ID,  3 =table , 4 = slide screen, 5 = whiteboard , 6 = other  ] | Any floating  point list of 7 items.  For example [0, 0, 0, 0, 1, 0, 0] which means that PM looks  at  the whiteboard for the whole utterance since index 5 is 1. |
| focus_listener_ui (list)   | A list of 7 floating items where each item shows ratio of UI focus while listening towards participants and objects. The index order is  [0 = ME, 1 = PM, 2 = ID,  3 =table , 4 = slide screen, 5 = whiteboard , 6 = other  ] | Any floating  point list of 7 items.  For example [0, 1, 0, 0, 0, 0, 0] which means that UI looks  at PM for the whole utterance since index 1 is 1. |
| focus_listener_id (list)   | A list of 7 floating items where each item shows ratio of ID focus while listening towards participants and objects. The index order is  [0 = UI, 1 = ME, 2 = PM,  3 =table , 4 = slide screen, 5 = whiteboard , 6 = other  ] | Any floating  point list of 7 items.  For example [0.5, 0.5, 0, 0, 0, 0, 0] which means that ID is looking at UI and MI for 50% each of the duration of the utterance since index 0 and 1. |
| focus_listener_me (list)   | A list of 7 floating items where each item shows ratio of ME focus while listening towards participants and objects. The index order is  [0 = UI, 1 = PM, 2 = ID,  3 =table , 4 = slide screen, 5 = whiteboard , 6 = other  ] | Any floating  point list of 7 items.  For example [0, 0, 1, 0, 0, 0, 0] which means that ME  looks  at ID  for the whole utterance since index 2 is 1. |
| speaker_role (string)      | Represents role of the speaker of the current utterance      | "pm", "ui", "me", or "id"                                    |
| prev_speaker_role (string) | Represents role of the speaker of the previous utterance     | "pm", "ui", "me", or "id"                                    |
| prev_addr_role             | Represents role of the addressee of the previous utterance   | "group", pm", "ui", "me", or "id"                            |
| da                         | dialogue act of the current utterance                        | Dialogue acts from AMI dataset: 'ass','be.neg', 'be.pos', 'el.ass', 'el.inf', 'el.sug', 'el.und', 'inf', 'off', 'sug', 'und', |
| prev_da                    | dialogue act of the previous utterance                       | Dialogue acts from AMI dataset: 'ass','be.neg', 'be.pos', 'el.ass', 'el.inf', 'el.sug', 'el.und', 'inf', 'off', 'sug', 'und', |



#### 1.3. predict_addr()

The `predict_addr()` function ultimately predicts the addressee of the current utterance.  The parameters of the `predict_addr()` function are as follows.



| Parameter Name (type)        | Description                                                  | Possible Values                                              |
| ---------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| input_sequence (numpy array) | This is the numpy array containing feature vector returned  by `data_fv_converter()` method | numpy array of numbers containing numeric values for features |

#### **1.4. AgentSlang Component**

An AgentSlang called `AddresseePredictorComponent.py` can be used and can be launched using a script:

```
# Go into AgentSlang installation directory
cd ${AGENTSLANGINSTALLDIR}
cd config/test_configurations

# Run Python module
./launch_python_addressee_predictor_component.sh

# Run AgentSlang program
cd ../../bin
./AgentSlang -config ../config/test_configurations/config_addressee_predictor.xml -profile profile1
```
An AgentSlang text promp will appear on your screen, you need to respect input conventions presented in the **1.2.** part. This input must be a json string such as the one presented below:
```
{"you_usage": 0, "duration_ms": 7.31, "sentence_length": 20, "focus_speaker": [0.14227, 0, 0, 0, 0.186047, 0.61683, 0, 0], "focus_listener_pm": [0, 0, 0, 0, 0, 0, 0], "focus_listener_ui": [0, 0, 0, 0, 1, 0, 0], "focus_listener_id": [0, 0, 1, 0, 0, 0, 0], "focus_listener_me": [0, 0, 0, 0.32695, 0.0383035, 0, 0], "speaker_role": "pm", "prev_speaker_role": "pm", "prev_addr_role": "id", "da": "sug", "prev_da": "ass"}
```

The text prompt should give you this answer:
```
{id=1, language=en-US, data='group'}
```

You can try with other values according to your interaction data.

## 2. Speaker VFOA Behavior Generator

Speaker VFOA Behavior Generator is a stand alone component which predicts the complete VFOA of speaker during an utterance including number of VFOA turns per utterance, duration per VFOA turn, target per VFOA turn and scheduling of VFOA turns.

The VFOA behaviour generator  is implemented in the `speaker_VFOA_predictor.py` file.  The `speaker_VFOA_predictor.py`   file containing the addressee predictor module consists of one class named `SpkVFOAPredictor` with the following functions.

#### 2.1. load_models()

The function loads the pickle object containing machine learning models for number of VFOA turns prediction, VFOA duration per turn prediction, and VFOA target per turn detection. The function stores the loaded machine learning models respectively in the `self.num_vfoa_turns_pred` , ` self.vfoa_dur_pred` ,  and, `self.vfoa_dir_pred` ,  variables and returns void. 

The parameters of the `load_model()` function are as follows.

**Note:** These arguments should be passed in order of their presence in the table.

| Parameter Name (type)        | Description                                                  | Possible Values                        |
| ---------------------------- | ------------------------------------------------------------ | -------------------------------------- |
| num_vfoa_turns_pred (string) | string path to the pickle object containing the machine learning model for number of VFOA turns prediction | Any string value containing file path. |
| vfoa_dur_pred (string)       | string path to the pickle object containing the machine learning model for prediction duration per VFOA turn | Any string value containing file path. |
| vfoa_dir_pred (string)       | string path to the pickle object containing the machine learning model for prediction target per VFOA turn | Any string value containing file path. |

#### 2.2. data_fv_converter()

Converts individual  input parameters to a complete feature vector that can be passed to machine learning module for addressee prediction. The function returns a one dimensional numpy array representing all the features. The parameters of the  `data_fv_converter()` function are as follows: 

**Note:** These attributes should be passed in order of their presence in the table.



| Parameter Name (type)   | Description                                                  | Possible Values                                              |
| ----------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| start_time (float)      | start time of utterance in milliseconds                      | any floating value                                           |
| end_time (float)        | end time of utterance in milliseconds                        | any floating value                                           |
| duration_ms (floating)  | duration of utterance in seconds                             | any floating value                                           |
| speaker_role (string)   | Represents role of the speaker of the current utterance      | "pm", "ui", "me", or "id"                                    |
| addressee_role (string) | Represents role of the addressee of the current utterance utterance. This value is known to the agent who is also speaker of the current utterance. | "group", pm", "ui", "me", or "id"                            |
| prev_addressee (string) | Represents role of the addressee of the previous utterance. This value can be predicted by Addressee predictor if the speaker of the previous utterance is not an agent. | "group", pm", "ui", "me", or "id"                            |
| prev_speaker (string)   | Represents role of the speaker of the previous utterance     | "pm", "ui", "me", or "id"                                    |
| da                      | dialogue act of the current utterance.                       | Dialogue acts from AMI dataset: 'ass','be.neg', 'be.pos', 'el.ass', 'el.inf', 'el.sug', 'el.und', 'inf', 'off', 'sug', 'und', |



#### 2.3. schedule_vfoa()

The `schedule_vfoa()` function is responsible for scheduling the VFOA. The output of `schedule_vfoa()` function is a string which contains the name of the duration and targets of turns in a sequence. 

 This function is called from the `generate_vfoa()` function (explained in next section). Hence, all the parameters to the `schedule_vfoa()` are passed through the `generate_vfoa()`method. The parameters of the  `schedule_vfoa()` function are as follows. 

**Note:** You do not need to pass these parameters manually to the `schedule_vfoa()` as they are passed via the `generate_vfoa()` function.

| Parameter Name (type)  | Description                                                  | Possible Values                                              |
| ---------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| total_duration(float)  | duration of the utterance in milliseconds                    | any floating value                                           |
| total_predicted (int)  | number of predicted targets.                                 | 1-8 (depending upon the number of participants and objects in sight). |
| predicted_index (list) | List containing indexes of targets predicted in the current utterance. List cannot contain more than 8 items since the sum of all the participants and objects is 8. | List item values should not repeat and should be between 0-7. |
| c_spk (string)         | Represents role of the speaker of the current utterance      | "pm", "ui", "me", or "id"                                    |
| c_adr (string)         | Represents role of the addressee of the current utterance utterance. This value is known to the agent who is also speaker of the current utterance. | "group", pm", "ui", "me", or "id"                            |
| p_spk  (string)        | Represents role of the speaker of the previous utterance     | "pm", "ui", "me", or "id"                                    |
| p_adr (string)         | Represents role of the addressee of the previous utterance. This value can be predicted by Addressee predictor if the speaker of the previous utterance is not an agent. | "group", pm", "ui", "me", or "id"                            |



#### 2.4. generate_vfoa()

The `generate_vfoa()` function is the function which predicts the final gaze including the turns, targets, duration and scheduling of VFOA. The parameters of the `generate_vfoa()` function are as follows: 

**Note:** These attributes should be passed in order of their presence in the table.



| Parameter Name (type)        | Description                                                  | Possible Values                                              |
| ---------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| total_duration(float)        | duration of the utterance in milliseconds                    | any floating value                                           |
| input_sequence (numpy array) | This is the numpy array containing feature vector returned  by `data_fv_converter()` method | numpy array of numbers containing numeric values for features |
| predicted_index (list)       | List containing indexes of targets predicted in the current utterance. List cannot contain more than 8 items since the sum of all the participants and objects is 8. | List item values should not repeat and should be between 0-7. |
| spk_role (string)            | Represents role of the speaker of the current utterance      | "pm", "ui", "me", or "id"                                    |
| add_role (string)            | Represents role of the addressee of the current utterance utterance. This value is known to the agent who is also speaker of the current utterance. | "group", pm", "ui", "me", or "id"                            |
| pspk_role  (string)          | Represents role of the speaker of the previous utterance     | "pm", "ui", "me", or "id"                                    |
| padd_role (string)           | Represents role of the addressee of the previous utterance. This value can be predicted by Addressee predictor if the speaker of the previous utterance is not an agent. | "group", pm", "ui", "me", or "id"                            |

#### **2.5. AgentSlang Component**

An AgentSlang called `SpeakerVFOAPredictorComponent.py` can be used and can be launched using a script:

```
# Go into AgentSlang installation directory
cd ${AGENTSLANGINSTALLDIR}
cd config/test_configurations

# Run Python module
./launch_python_speaker_vfoa_predictor_component.sh

# Run AgentSlang program
cd ../../bin
./AgentSlang -config ../config/test_configurations/config_vfoa_predictor.xml -profile profile1
```
An AgentSlang text promp will appear on your screen, you need to respect input conventions presented in the **2.2.** part. This input must be a json string such as the one presented below:
```
{"start_time": 1000120, "end_time": 1008790, "duration_ms": 3053, "speaker_role": "id", "addressee_role": "group", "prev_addressee": "group", "prev_speaker": "id", "da": "sug"}
```

The text prompt should give you this answer:
```
{id=1, language=en-US, data='ME:1220.0462758372528 , UI:1644.9864549903666 , Others:187.96726917238047 , '}
```

You can try with other values according to your interaction data.