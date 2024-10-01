
# How to load dashboards into your Open Search installation

Follow these steps:
 * Run the `3_open_search_ingestion.ipynb` notebook. This notebook creates an index `sqe_metrics` and populates it
   with pre-computed search metrics.
 * Upload the dashboards in file through `search_dashboard.ndjson`. In the side menu, click:
   **(Management) > Stack Management > Saved objects**. Click import and select the file.
   Overwriting existing objects should work for you.

# Prototype Dashboards in Notebook

In order to see the visualizations without loading the notebook follow this link:

  https://nbviewer.org/urls/gist.githubusercontent.com/alexeyrodriguez/2d906604518991cd27ebbdcaf32b3b71/raw/15a3b75c796fdcd7e030e7430625f99179b79047/2_visualizations.ipynb

This is a gist containing the 2nd notebook visualized through a public viewer.
Note, the gist is only accessible if one has the URL, plus the visualizastion notebook contains no confidencial information.


