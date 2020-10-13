cd ../../python/vfoa_addr_detection
python3 scripts/AddresseePredictorComponent.py -in_ip localhost -in_port 6668 -in_topic_name addressee_context_data -out_ip '*' -out_port 6669 -out_topic_name addressee_processed_data
