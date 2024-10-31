import argparse
from dataclasses import dataclass
from datetime import datetime, timedelta
import json
import uuid

import numpy as np

from opensearchpy import OpenSearch

import pandas as pd

from rich.console import Console

from tqdm import trange, tqdm

console = Console()

parser = argparse.ArgumentParser(description='Description of your program')
parser.add_argument('--esci-dataset-path', help='Path to ESCI dataset', required=True)
parser.add_argument('--num-search-results', help='Maximum number of results per query', default=5, type=int)
parser.add_argument('--num-unique-queries', help='Total number of unique queries', default=200, type=int)
parser.add_argument('--num-query-events', help='Total number of query events', default=1000, type=int)
parser.add_argument('--time-period-days', help='Length of interval in which queries are generated', default=7, type=int)
parser.add_argument('--datetime-start', help='Date and time of first query event', default="2024/06/01", action="store")
parser.add_argument('--seconds-between-clicks', help='Average seconds between clicks', default=1.0, type=float)
parser.add_argument('--generate-csv', help='Generate datasets and save in CSVs', default=False, action="store_true")
parser.add_argument('--generate-open-search', help='Generate datasets and save in Open Search', default=False, action="store_true")
parser.add_argument('--open-search-url', help='Open Search URL', default="http://localhost:9200", action="store")

args = parser.parse_args()


def sampling_weight(df):
    """
    Compute sampling weights in order to make average rating 1.0.

    The sampling weights oversample the zero rating examples in order to bring the average to 1.0.
    This helps making the final empirical ratings to be close to the original ratings under COEC.
    """
    sum_non_z = df[df.rating>0.0].rating.sum()
    num_z = (df.rating==0.0).sum()
    num_non_z = ((df.rating>0.0).sum())
    alpha = (sum_non_z - num_non_z) / num_z
    weights = np.where(df.rating>0.0, 1, alpha)
    return weights


def load_esci(esci_dataset_path):
    console.print("[bold cyan]Loading ESCI dataset[/bold cyan]")
    df_examples = pd.read_parquet(esci_dataset_path + '/shopping_queries_dataset_examples.parquet')
    df_examples = df_examples[df_examples.product_locale=="us"]

    console.print("Number of unique queries:", df_examples["query"].unique().size)
    console.print("Number of unique products:", df_examples["product_id"].unique().size)
    console.print("Number of unique query-product pairs:", df_examples[["query", "product_id"]].drop_duplicates().shape[0])
    # score_mapping = {"E": 3.0, "S": 2.0, "C": 1.0, "I": 0.0}
    score_mapping = {"E": 1.5, "S": 1.0, "C": 0.5, "I": 0.0}
    console.print("Mapping used to obtain numerical rating:", score_mapping)
    df_examples["rating"] = df_examples.esci_label.apply(lambda x: score_mapping[x])
    df_examples["weights"] = sampling_weight(df_examples)

    return df_examples


@dataclass
class GenConfig:
    num_search_results: int
    num_unique_queries: int
    num_query_events: int
    time_period: timedelta
    time_start: datetime
    avg_time_between_clicks: timedelta
    click_rates: list[float]
    open_search_url: str
        
    def get_avg_time_between_queries(self):
        return self.time_period / self.num_query_events


def create_gen_config(args):
    gen_config = GenConfig(
        num_search_results=args.num_search_results,
        num_unique_queries=args.num_unique_queries,
        num_query_events=args.num_query_events,
        time_period=timedelta(days=args.time_period_days),
        time_start=datetime.strptime(args.datetime_start, "%Y/%m/%d"),
        avg_time_between_clicks=timedelta(seconds=args.seconds_between_clicks),
        click_rates=0.1/np.log(np.arange(args.num_search_results)*4+2.7),
        open_search_url=args.open_search_url,
    )
    console.print("[bold cyan]Data Generation Configuration:[/bold cyan]", gen_config)
    return gen_config


def make_top_queries(gen_config, df_examples):
    """
    Select top queries and compute sampling probability.
    
    Columns:
     - query: query string
     - num_judgments: number of judgments in query
     - p: sampling probability
    
    The sampling probability is naive, it's the normalized number of judgments
    """
    df_q_agg = df_examples[["query", "esci_label"]].groupby("query").count().reset_index()
    df_q_agg = df_q_agg.rename(columns={"esci_label": "num_judgments"})
    df_q_agg = df_q_agg.sort_values("num_judgments", ascending=False)
    top_queries = df_q_agg.head(gen_config.num_unique_queries).copy()
    top_queries["p"] = top_queries.num_judgments / top_queries.num_judgments.sum()
    return top_queries


def make_query_sampler(gen_config, top_queries, query, df_g):
    # dfn = df_g.sort_values("ranking", ascending=False)
    # sorting is disabled to avoid increasing ctr at top positions (we want the ratings as close to the originals)
    dfn = df_g
    a = 1000 * dfn.rating * gen_config.click_rates + 0.5
    b = 1000 - a + 49.5
    dfn["p"] = np.random.beta(a, b) # use beta to make 0 rating results have sometimes a click
    # dfn["p"] = dfn.rating * gen_config.click_rates
    dfn["position"] = np.arange(dfn.p.size)
    dfn["p_query"] = top_queries[top_queries["query"]==query].p.values[0]
    return dfn.reset_index(drop=True)


def make_result_sample_per_query(gen_config, top_queries, df_examples):
    """
    Make a datastructure to facilitate sampling.
    
    Returns a dictionary from query strings to dataframes
    
    Columns:
     - query: query string
     - product_id: product id string
     - rating: Amazon ESCI score
     - ranking: noise perturbed rating
     - p: probability of click
     - position: 0-based result position
     - p_query: probability of sampling with query
     - exp_rating: actual expected rating after sampling (after taking into account result ranking)
    """
    judgments = df_examples[df_examples["query"].isin(top_queries["query"].values)]
    judgments = judgments[["query", "product_id", "rating", "weights"]].groupby(["query", "product_id"]).mean().reset_index()
    judgments["ranking"] = judgments.rating + np.random.uniform(-3, 3, judgments.rating.size)
    judgments = judgments.groupby("query").sample(gen_config.num_search_results, weights="weights")
    
    judg_dict = {}
    for q, df_g in judgments.groupby("query"):
        judg_dict[q] = make_query_sampler(gen_config, top_queries, q, df_g)
        
    exp_ctr = compute_exp_ctr_per_pos(gen_config, judg_dict)
        
    # Now update the expected rating based on the achieved expected CTR
    for q in judg_dict.keys():
        q_df = judg_dict[q]
        q_df["exp_rating"] = q_df["p"] / exp_ctr
        judg_dict[q] = q_df
        
    return judg_dict


def compute_exp_ctr_per_pos(gen_config, result_sample_per_query):
    """Compute expected CTR per rank"""
    ref_judg = pd.concat([df for _, df in result_sample_per_query.items()])
    tmp_df = ref_judg
    tmp_df["wp"] = tmp_df.p * tmp_df.p_query
    tmp_df = tmp_df[["position", "wp", "p_query"]].groupby("position").sum()
    exp_ctr = tmp_df.wp / tmp_df.p_query
    return exp_ctr


def prepare_data_generation(gen_config, esci_df):
    console.print("[bold cyan]Preparing Data Generation[/bold cyan]")

    top_queries = make_top_queries(gen_config, esci_df)
    console.print("Top 5 queries and sampling probabilies:")
    for i in range(5):
        console.print("  query:", top_queries.iloc[i]["query"], ", probability:", top_queries.iloc[i]["p"])

    np.random.seed(10)
    judg_dict = make_result_sample_per_query(gen_config, top_queries, esci_df)

    console.print("Expected CTR per rank:", compute_exp_ctr_per_pos(gen_config, judg_dict).values)

    console.print("Expected judgment under COEC for top 5 documents of top 3 queries:")
    for i in range(3):
        console.print(judg_dict[top_queries.iloc[i]["query"]][["query", "product_id", "position", "rating", "exp_rating"]].head(5))

    return top_queries, judg_dict

def make_query_event(gen_config, row):
    query = {
        "from": 0,
        "size": gen_config.num_search_results,
        "query": {
            "match": {
                "short_description": row["user_query"]
            }
        },
        # "ext": {
        #     "ubi": {
        #         "client_id": row["client_id"],
        #         "query_id": row["query_id"],
        #         "user_query": row["user_query"],
        #         "object_id_field": row["object_id_field"],
        #     }
        # }
    }
    response_id = str(uuid.uuid4())
    query_event = {
        "timestamp": int(row["datetime"].timestamp() * 1000),
        "queryId": row["query_id"],
        "userQuery": row["user_query"],
        "query": json.dumps(query),
        "queryResponse": {
            "queryId": row["query_id"],
            "queryResponseId": response_id,
        },
        "queryAttributes": {},
        "clientId": row["client_id"],
    }
    return query_event

def make_ubi_event(gen_config, row):
    ubi_event = {
        "application": "esci_ubi_sample",
        "action_name": row["action_name"],
        "client_id": row["client_id"],
        "query_id": row["query_id"],
        "message_type": None,
        "message": None,
        "timestamp": int(row["datetime"].timestamp() * 1000),
        "event_attributes": {
            "object": {
                "object_id_field": row["object_id_field"],
                "object_id": row["object_id"],
                "description": "",
            },
            "position": {
                "index": row["position"],
            },
            "session_id": row["session_id"],
        }
    }
    return ubi_event

def save_to_csv(gen_config, event_generator):
    console.print("Saving queries and events to [bold]ubi_queries.csv[/bold] and [bold]ubi_events.csv[/bold]")
    first = True
    for queries, events in tqdm(event_generator, total=gen_config.num_query_events/1000, desc="Generating queries in units of 1000"):
        kwargs = {} if first else {"mode": "a", "header": False}
        events.to_csv("ubi_events.csv", index=False, **kwargs)
        queries.to_csv("ubi_queries.csv", index=False, **kwargs)
        first = False

def populate_open_search(gen_config, event_generator):
    console.print("[bold cyan]Indexing data into Open Search[/bold cyan]")
    client = OpenSearch(gen_config.open_search_url, use_ssl=False)

    for queries, events in tqdm(event_generator, total=gen_config.num_query_events/1000, desc="Indexing queries in units of 1000"):
        data = []
        for _, row in queries.iterrows():
            event_id = str(uuid.uuid4())
            data.append({"index": {"_index": "ubi_queries", "_id": event_id}})
            data.append(make_query_event(gen_config, row))

        for _, row in events.iterrows():
            event_id = str(uuid.uuid4())
            data.append({"index": {"_index": "ubi_events", "_id": event_id}})
            data.append(make_ubi_event(gen_config, row))
        
        client.bulk(body=data)


def simulate_events(gen_config, top_queries, result_sample_per_query):

    current_time = gen_config.time_start

    events = []
    queries = []

    for i in range(gen_config.num_query_events):
        new_delta = np.random.exponential(gen_config.get_avg_time_between_queries().seconds)
        current_time = current_time + timedelta(seconds=new_delta)

        # Generation of Query and Impressions
        q = np.random.choice(top_queries["query"], p=top_queries["p"])
        judg_df = result_sample_per_query[q].copy()
        click_event = np.random.binomial(n=1, p=judg_df.p)
        judg_df = judg_df[["product_id", "position"]]
        judg_df = judg_df.rename(columns={"product_id": "object_id"})
        judg_df["object_id_field"] = "product_id"

        client_id = str(uuid.uuid4())
        query_id = str(uuid.uuid4())
        session_id = str(uuid.uuid4())

        queries.append(pd.DataFrame({
            "query_id": [query_id],
            "client_id": [client_id],
            "user_query": [q],
            "size": [judg_df.shape[0]],
            "object_id_field": "product_id",
            "datetime": current_time,
        }))

        judg_df["query_id"] = query_id
        judg_df["client_id"] = client_id
        judg_df["session_id"] = session_id
        judg_df["datetime"] = current_time
        judg_df["datetime"] = judg_df["datetime"].values.astype("datetime64[ns]")
        judg_df["action_name"] = "view"

        events.append(judg_df)

        # Generation of Clicks
        clicks = judg_df[click_event==1].copy()
        clicks["action_name"] = "click"

        time_deltas = np.random.exponential(gen_config.avg_time_between_clicks.seconds, clicks.shape[0])
        time_deltas = np.cumsum(time_deltas)
        time_deltas = pd.to_timedelta(time_deltas, unit='s')
        clicks["datetime"] = clicks.datetime + time_deltas

        events.append(clicks)

        if i % 1000 == 0 and i > 0:
            events = pd.concat(events)
            queries = pd.concat(queries)
            yield queries, events
            events = []
            queries = []

    events = pd.concat(events)
    queries = pd.concat(queries)
    
    yield queries, events

def main(args):
    esci_df = load_esci(args.esci_dataset_path)
    gen_config = create_gen_config(args)
    top_queries, judg_dict = prepare_data_generation(gen_config, esci_df)

    if not args.generate_csv and not args.generate_open_search:
        console.print("[red bold]You have to specify either --generate-csv or --generate-open-search")
        return

    event_generator = simulate_events(gen_config, top_queries, judg_dict)

    if args.generate_csv:
        save_to_csv(gen_config, event_generator)
    elif args.generate_open_search:
        populate_open_search(args, event_generator)

main(args)