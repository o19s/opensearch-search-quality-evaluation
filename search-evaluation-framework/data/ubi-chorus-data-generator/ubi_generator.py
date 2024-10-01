import random
import uuid
from time import time

from opensearchpy import OpenSearch

client = OpenSearch("http://localhost:9200", use_ssl=False)

# The random search terms and weighted actions.
search_terms = ["computer", "laptop", "notebook", "desk", "power plug", "brother", "ink", "hard drive"]
actions_weights = [75, 5, 1, 3, 4, 1]
actions = ["click", "click_through", "add_to_cart", "watch", "view", "purchase"]

weighted_actions = []
for aw, a in zip(actions_weights, actions):
    for x in range(aw):
        weighted_actions.append(a)

print("Actions: " + str(actions))
print("Weighted actions: " + str(weighted_actions))

object_id_field = "primary_ean"
item_description_field = "title"
index_name = "ecommerce"
number_of_user_queries = 1000
page_size = 20
max_number_events_for_each_search = 10

# Generate user queries for random search terms.
for x in range(number_of_user_queries):

    random_search_term_index = random.randint(0, len(search_terms) - 1)
    random_search_term = search_terms[random_search_term_index]
    
    client_id = str(uuid.uuid4())
    query_id = str(uuid.uuid4())
    
    query = {
       "from": 0,
        "size": page_size,
        "query": {
          "match": {
            "short_description": random_search_term
          }
        },
        "ext": {
            "ubi": {
                "client_id": client_id,
                "query_id": query_id,
                "user_query": random_search_term,
                "object_id_field": "primary_ean"
            }
        }
     }
    
    query_response = client.search(
        body = query,
        index = index_name
    )

    random_number_of_events = random.randint(0, max_number_events_for_each_search - 1)

    for y in range(random_number_of_events):
            
        random_search_result_index = random.randint(0, page_size - 1)
        random_action_index = random.randint(0, len(weighted_actions) - 1)
        session_id = str(uuid.uuid4())
        
        #print("random_action_index = " + str(random_action_index))
        #print("random_search_result_index = " + str(random_search_result_index))

        ubi_event = {
            "action_name": weighted_actions[random_action_index],
            "client_id": client_id,
            "query_id": query_id,
            "message_type": None,
            "message": None,
            "timestamp": time(),
            "event_attributes": {
                "object": {
                    "object_id_field": "ean",
                    "object_id": query_response["hits"]["hits"][random_search_result_index]["_source"][object_id_field],
                    "description": query_response["hits"]["hits"][random_search_result_index]["_source"][item_description_field]
                },
                "position": {
                    "index": random_search_result_index
                },
                "session_id": session_id
            }
        }
    
        event_id = str(uuid.uuid4())
        
        response = client.index(
            body = ubi_event,
            index = "ubi_events",
            id = event_id,
            refresh = True
        )

