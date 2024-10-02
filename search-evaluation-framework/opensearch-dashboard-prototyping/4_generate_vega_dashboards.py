import json

import pandas as pd


dfm = pd.read_csv("metrics.csv")

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
        name = "metrics_input",
        values = rows,
    )

jmetrics = metrics_to_vega_data(dfm.head(10))

with open("vega_templates/scatter_plot_vega.json") as f:
    dash = json.load(f)

with open("vega_templates/scatter_plot_data.json") as f:
    data_transform = json.load(f)


dash["data"] = [jmetrics] + data_transform["data"] + dash["data"]

print(json.dumps(dash, indent=2))
