#!/bin/zsh

ELASTICSEARCH_HOST="http://localhost:9200"

function run_data_generator_and_cleanup() {
  echo "Starting data generator..."
  docker-compose up --build -d data-generator

  echo "Waiting for data generator to complete..."
  docker wait data-generator

  echo "Removing data generator container..."
  docker-compose rm -f data-generator

  echo "Removing dangling images..."
  docker rmi $(docker images -f "dangling=true" -q)

  echo "Cleanup complete."
}

# DELETE mapping if it exists. We don't check for failure here,
# as it will fail if the index doesn't exist, which is an acceptable outcome.
echo "Deleting existing mapping (if any)..."
curl -X DELETE "$ELASTICSEARCH_HOST/music_id3"

# PUT mapping via curl. If successful, then run the data generator.
# We use the -f flag to ensure that the command fails on server errors.
echo "Creating new mapping..."
if curl -f -X PUT "$ELASTICSEARCH_HOST/music_id3" -H 'Content-Type: application/json' -d @music_id3_mapping.json; then
  echo "Mapping created successfully."
  run_data_generator_and_cleanup
else
  echo "Failed to create Elasticsearch mapping. Aborting data generation." >&2
  exit 1
fi
