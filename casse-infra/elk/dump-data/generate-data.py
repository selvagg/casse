#!/usr/bin/env python3
from elasticsearch import Elasticsearch
from elasticsearch.helpers import parallel_bulk
from faker import Faker
import random
import uuid
import sys

ES_HOST = "http://elasticsearch1:9200" # Changed from localhost to elasticsearch1 service name
INDEX   = "music_id3"
TOTAL   = 500
CHUNK   = 5_000
THREADS    = 4     # number of worker threads
QUEUE_SIZE = 8     # prefetch queue size per thread

fake = Faker()
artists_pool = ["The Beatles", "Queen", "Michael Jackson", "Taylor Swift", "Coldplay"]
tags_pool    = ["Rock", "Pop", "Classic", "Dance", "Jazz", "Hip-Hop", "Blues"]

print(f"Connecting to Elasticsearch at {ES_HOST}...", file=sys.stderr)
try:
    es = Elasticsearch(ES_HOST)
    # Ping to check connection
    if not es.ping():
        raise ValueError("Connection to Elasticsearch failed!")
    print("Successfully connected to Elasticsearch.", file=sys.stderr)
except Exception as e:
    print(f"Error connecting to Elasticsearch: {e}", file=sys.stderr)
    sys.exit(1)


def generate_actions():
    for _ in range(TOTAL):
        doc_id = str(uuid.uuid4())                # random alphanumeric ID
        release = fake.date_between(start_date='-50y', end_date='today')
        yield {
            "_index": INDEX,
            "_id":    doc_id,
            "_source": {
                "title":        fake.sentence(nb_words=3).rstrip("."),
                "artists":      ", ".join(random.sample(artists_pool, k=random.randint(1, 2))),
                "album":        fake.sentence(nb_words=2).rstrip("."),
                "release_date": release.isoformat(),
                "year":         release.year,
                "composer":     fake.name(),
                "lyrics":       fake.text(max_nb_chars=200),
                "duration_ms":  random.randint(120_000, 300_000),
                "comment":      fake.sentence(nb_words=6),
                "tags":         ", ".join(random.sample(tags_pool, k=random.randint(1, 3))),
                "email":        fake.email()
            }
        }

print(f"Starting data generation and indexing {TOTAL} documents into index '{INDEX}'...", file=sys.stderr)
success_count = 0
try:
    for ok, info in parallel_bulk(
            client        = es,
            actions       = generate_actions(),
            thread_count  = THREADS,
            chunk_size    = CHUNK,
            queue_size    = QUEUE_SIZE,
            request_timeout=60
    ):
        if not ok:
            print(f"Failed to index document: {info}", file=sys.stderr)
        else:
            success_count += 1
    print(f"Data generation complete. Successfully indexed {success_count} documents.", file=sys.stderr)
except Exception as e:
    print(f"An error occurred during bulk indexing: {e}", file=sys.stderr)
    sys.exit(1)
