#!/bin/zsh

# PUT mapping via curl
curl -X PUT "http://localhost:9200/music_id3" -H 'Content-Type: application/json' -d'
{
  "settings": {
    "number_of_shards":   1,
    "number_of_replicas": 1,
    "analysis": {
      "tokenizer": {
        "autocomplete_tok": {
          "type":       "edge_ngram",
          "min_gram":   2,
          "max_gram":   20,
          "token_chars":["letter","digit"]
        }
      },
      "analyzer": {
        "autocomplete": {
          "tokenizer": "autocomplete_tok",
          "filter":    ["lowercase"]
        }
      },
      "normalizer": {
        "lowercase_fold": {
          "type":   "custom",
          "filter": ["lowercase","asciifolding"]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "title": {
        "type":     "text",
        "analyzer": "standard",
        "fields": {
          "autocomplete": {
            "type":           "text",
            "analyzer":       "autocomplete",
            "search_analyzer":"standard"
          },
          "keyword": {
            "type":       "keyword",
            "normalizer": "lowercase_fold"
          }
        }
      },
      "artists": {
        "type":       "keyword",
        "normalizer": "lowercase_fold"
      },
      "album": {
        "type":     "text",
        "analyzer": "standard",
        "fields": {
          "keyword": {
            "type":       "keyword",
            "normalizer": "lowercase_fold"
          }
        }
      },
      "composer": {
        "type":     "text",
        "analyzer": "standard",
        "fields": {
          "keyword": {
            "type":       "keyword",
            "normalizer": "lowercase_fold"
          }
        }
      },
      "tags": {
        "type":       "keyword",
        "normalizer": "lowercase_fold"
      },
      "email": {
        "type":       "keyword",
        "normalizer": "lowercase_fold"
      }
    }
  }
}
'
