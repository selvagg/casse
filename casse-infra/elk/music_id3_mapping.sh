#!/bin/zsh

ELASTICSEARCH_HOST="http://localhost:9200"

# DELETE mapping if it exists
curl -X DELETE "$ELASTICSEARCH_HOST/music_id3"

# PUT mapping via curl
curl -X PUT "$ELASTICSEARCH_HOST/music_id3" -H 'Content-Type: application/json' -d @music_id3_mapping.json
