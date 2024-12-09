curl -s -X DELETE http://localhost:9200/ubi_events,ubi_queries

curl -s -X POST http://localhost:9200/_plugins/ubi/initialize

curl -s -X PUT http://localhost:9200/ubi_events/_doc/1 -H "Content-Type: application/json" -d'
{
  "application": "esci_ubi_sample",
  "action_name": "impression",
  "query_id": "bd43b377-67ff-4165-8753-58bbdb3392c5",
  "session_id": "fdb13692-d42c-4d1d-950b-b8814c963de2",
  "client_id": "28ccfb32-fbd7-4514-9051-cea719db42de",
  "timestamp": "2024-12-11T04:56:49.419Z",
  "user_query": "tv",
  "message_type": null,
  "message": null,
  "event_attributes": {
    "object": {
      "object_id": "B07JW53H22",
      "object_id_field": "product_id"
    },
    "position": {
      "ordinal": 1
    }
  }
}' | jq

curl -s -X PUT http://localhost:9200/ubi_events/_doc/2 -H "Content-Type: application/json" -d'
{
  "application": "esci_ubi_sample",
  "action_name": "impression",
  "query_id": "bd43b377-67ff-4165-8753-58bbdb3392c5",
  "session_id": "fdb13692-d42c-4d1d-950b-b8814c963de2",
  "client_id": "28ccfb32-fbd7-4514-9051-cea719db42de",
  "timestamp": "2024-12-11T04:56:49.419Z",
  "user_query": "tv",
  "message_type": null,
  "message": null,
  "event_attributes": {
    "object": {
      "object_id": "B07JW53H22",
      "object_id_field": "product_id"
    },
    "position": {
      "ordinal": 1
    }
  }
}' | jq

curl -s -X PUT http://localhost:9200/ubi_events/_doc/3 -H "Content-Type: application/json" -d'
{
  "application": "esci_ubi_sample",
  "action_name": "impression",
  "query_id": "bd43b377-67ff-4165-8753-58bbdb3392c5",
  "session_id": "fdb13692-d42c-4d1d-950b-b8814c963de2",
  "client_id": "28ccfb32-fbd7-4514-9051-cea719db42de",
  "timestamp": "2024-12-11T04:56:49.419Z",
  "user_query": "tv",
  "message_type": null,
  "message": null,
  "event_attributes": {
    "object": {
      "object_id": "B07JW53H22",
      "object_id_field": "product_id"
    },
    "position": {
      "ordinal": 1
    }
  }
}' | jq

curl -s -X PUT http://localhost:9200/ubi_events/_doc/4 -H "Content-Type: application/json" -d'
{
  "application": "esci_ubi_sample",
  "action_name": "impression",
  "query_id": "bd43b377-67ff-4165-8753-58bbdb3392c5",
  "session_id": "fdb13692-d42c-4d1d-950b-b8814c963de2",
  "client_id": "28ccfb32-fbd7-4514-9051-cea719db42de",
  "timestamp": "2024-12-11T04:56:49.419Z",
  "user_query": "tv",
  "message_type": null,
  "message": null,
  "event_attributes": {
    "object": {
      "object_id": "B07JW53H22",
      "object_id_field": "product_id"
    },
    "position": {
      "ordinal": 1
    }
  }
}' | jq

curl -s -X PUT http://localhost:9200/ubi_events/_doc/5 -H "Content-Type: application/json" -d'
{
  "application": "esci_ubi_sample",
  "action_name": "click",
  "query_id": "bd43b377-67ff-4165-8753-58bbdb3392c5",
  "session_id": "fdb13692-d42c-4d1d-950b-b8814c963de2",
  "client_id": "28ccfb32-fbd7-4514-9051-cea719db42de",
  "timestamp": "2024-12-11T04:56:49.419Z",
  "user_query": "tv",
  "message_type": null,
  "message": null,
  "event_attributes": {
    "object": {
      "object_id": "B07JW53H22",
      "object_id_field": "product_id"
    },
    "position": {
      "ordinal": 1
    }
  }
}' | jq

curl -s -X PUT http://localhost:9200/ubi_events/_doc/6 -H "Content-Type: application/json" -d'
{
  "application": "esci_ubi_sample",
  "action_name": "impression",
  "query_id": "dc6872a3-1f4c-46b2-ad84-7add603b4c73",
  "session_id": "a8f7d668-12b9-4cf3-a56f-22700b9e9b89",
  "client_id": "a654b87b-a8cd-423b-996f-a169de13d4fb",
  "timestamp": "2024-12-11T00:16:42.278Z",
  "user_query": "airpods",
  "message_type": null,
  "message": null,
  "event_attributes": {
    "object": {
      "object_id": "B088FVYG44",
      "object_id_field": "product_id"
    },
    "position": {
      "ordinal": 1
    }
  }
}' | jq

curl -s -X PUT http://localhost:9200/ubi_events/_doc/7 -H "Content-Type: application/json" -d'
{
  "application": "esci_ubi_sample",
  "action_name": "impression",
  "query_id": "dc6872a3-1f4c-46b2-ad84-7add603b4c73",
  "session_id": "a8f7d668-12b9-4cf3-a56f-22700b9e9b89",
  "client_id": "a654b87b-a8cd-423b-996f-a169de13d4fb",
  "timestamp": "2024-12-11T00:16:42.278Z",
  "user_query": "airpods",
  "message_type": null,
  "message": null,
  "event_attributes": {
    "object": {
      "object_id": "B088FVYG44",
      "object_id_field": "product_id"
    },
    "position": {
      "ordinal": 1
    }
  }
}' | jq

curl -s -X PUT http://localhost:9200/ubi_events/_doc/8 -H "Content-Type: application/json" -d'
{
  "application": "esci_ubi_sample",
  "action_name": "impression",
  "query_id": "dc6872a3-1f4c-46b2-ad84-7add603b4c73",
  "session_id": "a8f7d668-12b9-4cf3-a56f-22700b9e9b89",
  "client_id": "a654b87b-a8cd-423b-996f-a169de13d4fb",
  "timestamp": "2024-12-11T00:16:42.278Z",
  "user_query": "airpods",
  "message_type": null,
  "message": null,
  "event_attributes": {
    "object": {
      "object_id": "B088FVYG44",
      "object_id_field": "product_id"
    },
    "position": {
      "ordinal": 1
    }
  }
}' | jq

curl -s -X PUT http://localhost:9200/ubi_events/_doc/9 -H "Content-Type: application/json" -d'
{
  "application": "esci_ubi_sample",
  "action_name": "impression",
  "query_id": "dc6872a3-1f4c-46b2-ad84-7add603b4c73",
  "session_id": "a8f7d668-12b9-4cf3-a56f-22700b9e9b89",
  "client_id": "a654b87b-a8cd-423b-996f-a169de13d4fb",
  "timestamp": "2024-12-11T00:16:42.278Z",
  "user_query": "airpods",
  "message_type": null,
  "message": null,
  "event_attributes": {
    "object": {
      "object_id": "B088FVYG44",
      "object_id_field": "product_id"
    },
    "position": {
      "ordinal": 1
    }
  }
}' | jq

curl -s -X PUT http://localhost:9200/ubi_events/_doc/10 -H "Content-Type: application/json" -d'
{
  "application": "esci_ubi_sample",
  "action_name": "click",
  "query_id": "dc6872a3-1f4c-46b2-ad84-7add603b4c73",
  "session_id": "a8f7d668-12b9-4cf3-a56f-22700b9e9b89",
  "client_id": "a654b87b-a8cd-423b-996f-a169de13d4fb",
  "timestamp": "2024-12-11T00:16:42.278Z",
  "user_query": "airpods",
  "message_type": null,
  "message": null,
  "event_attributes": {
    "object": {
      "object_id": "B088FVYG44",
      "object_id_field": "product_id"
    },
    "position": {
      "ordinal": 1
    }
  }
}' | jq

curl -s -X PUT http://localhost:9200/ubi_events/_doc/11 -H "Content-Type: application/json" -d'
{
  "application": "esci_ubi_sample",
  "action_name": "click",
  "query_id": "dc6872a3-1f4c-46b2-ad84-7add603b4c73",
  "session_id": "a8f7d668-12b9-4cf3-a56f-22700b9e9b89",
  "client_id": "a654b87b-a8cd-423b-996f-a169de13d4fb",
  "timestamp": "2024-12-11T00:16:42.278Z",
  "user_query": "airpods",
  "message_type": null,
  "message": null,
  "event_attributes": {
    "object": {
      "object_id": "B088FVYG44",
      "object_id_field": "product_id"
    },
    "position": {
      "ordinal": 1
    }
  }
}' | jq

curl -s -X PUT http://localhost:9200/ubi_events/_doc/12 -H "Content-Type: application/json" -d'
{
  "application": "esci_ubi_sample",
  "action_name": "click",
  "query_id": "dc6872a3-1f4c-46b2-ad84-7add603b4c73",
  "session_id": "a8f7d668-12b9-4cf3-a56f-22700b9e9b89",
  "client_id": "a654b87b-a8cd-423b-996f-a169de13d4fb",
  "timestamp": "2024-12-11T00:16:42.278Z",
  "user_query": "airpods",
  "message_type": null,
  "message": null,
  "event_attributes": {
    "object": {
      "object_id": "B088FVYG44",
      "object_id_field": "product_id"
    },
    "position": {
      "ordinal": 1
    }
  }
}' | jq