#!/usr/bin/env python3
from elasticsearch import Elasticsearch
from elasticsearch.helpers import parallel_bulk
from faker import Faker
import random
import uuid

ES_HOST = "http://localhost:9200"
INDEX   = "music_id3"
TOTAL   = 1_000_000
CHUNK   = 5_000
THREADS    = 4     # number of worker threads
QUEUE_SIZE = 8     # prefetch queue size per thread

fake = Faker()
artists_pool = ["the_beatles", "queen", "michael_jackson", "taylor_swift", "coldplay"]
tags_pool    = ["rock", "pop", "classic", "dance", "ballad", "jazz", "hiphop"]
genre_pool   = ["rock", "pop", "classic", "dance", "jazz", "hiphop", "blues"]

es = Elasticsearch(ES_HOST)

def generate_actions():
    for _ in range(TOTAL):
        doc_id = str(uuid.uuid4())                # random alphanumeric ID
        release = fake.date_between(start_date='-50y', end_date='today')
        yield {
            "_index": INDEX,
            "_id":    doc_id,
            "_source": {
                "song_id":      doc_id,           # or keep separate if you prefer
                "title":        fake.sentence(nb_words=3).rstrip("."),
                "artists":      [random.choice(artists_pool)],
                "album":        fake.sentence(nb_words=2).rstrip("."),
                "release_date": release.isoformat(),
                "year":         release.year,
                "genre":        random.choice(genre_pool),
                "composer":     fake.name(),
                "lyrics":       fake.text(max_nb_chars=200),
                "duration_ms":  random.randint(120_000, 300_000),
                "comment":      fake.sentence(nb_words=6),
                "tags":         random.sample(tags_pool, k=random.randint(1, 3))
            }
        }

# Stream directly in batches; existing docs remain untouched
# helpers.bulk(es, generate_actions(), chunk_size=CHUNK, request_timeout=60)


# parallel_bulk returns a generator of (ok, info) tuples
for ok, info in parallel_bulk(
        client        = es,
        actions       = generate_actions(),
        thread_count  = THREADS,
        chunk_size    = CHUNK,
        queue_size    = QUEUE_SIZE,
        request_timeout=60
):
    if not ok:
        print("failed:", info)