curl -s "http://localhost:9200/ecommerce/_search?search_pipeline=hybrid-search-pipeline" -H "Content-Type: application/json" -d'
{
  "query": {
    "hybrid": {
      "queries": [
        {
          "match": {
            "title": {
              "query": "shoes"
            }
          }
        },
        {
          "neural": {
            "title_embedding": {
              "query_text": "shoes",
              "k": "50"
            }
          }
        }
      ]
    }
  }
}' | jq