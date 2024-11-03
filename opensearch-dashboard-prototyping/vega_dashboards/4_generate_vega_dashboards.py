import argparse
import json

import pandas as pd

plots = ["scatter_plot", "violin_plot"]

parser = argparse.ArgumentParser(description='Vega Dashboard generator')
parser.add_argument("--test", action="store_true")
parser.add_argument("--plot", required=True, help=f"plot to generate, one of: {plots}")
args = parser.parse_args()

dfm = pd.read_csv("../data_notebooks/metrics.csv")

def metrics_to_vega_data(df):
    rows = []
    for _, row in df.iterrows():
        rows.append(dict(
            search_config=row["search_config"],
            query=row["query"],
            metric=row["metric"],
            value=row["value"],
        ))
    return dict(
        values = rows,
    )

assert args.plot in plots

with open(f"templates/{args.plot}_vega.json") as f:
    dash = json.load(f)

if args.test:
    jmetrics = metrics_to_vega_data(dfm.head(10))
    dash["data"] = jmetrics
else:
    with open(f"templates/{args.plot}_data.json") as f:
        jmetrics = json.load(f)
        dash["data"] = jmetrics["data"]
        dash["transform"] = jmetrics["transform"] + dash["transform"]

print(json.dumps(dash, indent=2))
