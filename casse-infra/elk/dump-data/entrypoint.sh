#!/bin/sh

# Wait for Elasticsearch to be up and running
until curl -s "http://elasticsearch1:9200/_cluster/health?wait_for_status=yellow&timeout=50s"; do
  echo "Waiting for Elasticsearch..."
  sleep 5
done

# Create the index with the mapping
curl -X PUT "http://elasticsearch1:9200/music_id3" -H 'Content-Type: application/json' -d @./../music_id3_mapping.json

# Run the data generator
python generate-data.py
