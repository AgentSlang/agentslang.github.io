cd ../../python/vfoa_addr_detection
python3 scripts/SpeakerVFOAPredictorComponent.py -in_ip localhost -in_port 6666 -in_topic_name vfoa_gaze_context_data -out_ip '*' -out_port 6667 -out_topic_name vfoa_processed_gaze_data
