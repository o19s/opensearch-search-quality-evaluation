fields = [
    'click',
    'impression', 
    'display_url', 
    'ad_id',
    'advertiser_id',
    'depth',
    'position',
    'query_id',
    'keyword_id',
    'title_id',
    'description_id',
    'user_id',
]

def parse_line(line):
    values = line.strip().split('\t')
    return dict(zip(fields, values))

def make_event(instance, action_name):
    return {
        "action_name": action_name,
        "client_id": instance['user_id'],
        "query_id": instance['query_id'],
        "event_attributes": {
            "object_id_field": "ean",
            "object_id": instance["ad_id"],
            'description': instance["description_id"],
            "position": {
                "index": instance["position"],
            },
            "session_id": instance["user_id"],
        }
    }

def consider_display_url(display_url, sample_rate=1.0):
    bucket = (hash(display_url)&1023)/1024 # warning, hash is not deterministic across python runs
    return bucket < sample_rate

with open('./track2/training.txt.1k', 'r') as f:
    for line in f:
        instance = parse_line(line)
        if consider_display_url(instance['display_url']):
            event = make_event(instance, 'view')
            # note, the impressions field contains how many impressions of this ad were shown
            # although one is printed, there is potentially more than one event
            print(event)

            if instance['click'] == '1':
                event = make_event(instance, 'click')
                print(event)