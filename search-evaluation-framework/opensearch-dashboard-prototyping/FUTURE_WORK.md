Some future work ideas that can be put in issues when the right home for this project is found.
 * The Scores pane (Bar chart of metrics) in Search Configuration Comparison has repeated metrics in the legend.
   This is necessary to enforce different color for metrics. A new solution would have to be found otherwise that
   doesn't use a sub-aggregation.
 * Scale problems, graphs that show metrics in different scales use the maximum range (e.g. that of DCG) which makes
   the smaller scale metrics harder to visualize. This shows up in the Performance Over Time pane in Search Configuration
   Comparison dashboard. This is at the moment not supported: https://github.com/opensearch-project/OpenSearch-Dashboards/issues/706
 * The pane referred to above still has problems with colors. Two different metrics can be displayed in the same color, though
   choosing a third metric shows that the problem is the color palette (so there is an attempt in selecting a different color).

Bigger issues:
 * The tabular metric views, like for example Score Summary in Search Configuration Comparison, are separated into
   different tables when multiple metrics are used. If this table were a pivot table, metrics could be shown as
   additional columns. Pivot tables are not supported out of the box however:
   https://github.com/opensearch-project/OpenSearch-Dashboards/issues/705
 * Drill down capabilities, in the Single Search Configuration Deep Dive, it would be great if when clicking on a
   query, not only a filter is applied to the page, but actually a navigation to a new view happens. This view could
   be a new page that shows more detailed statistics about the query. For example: query results, the query frequency,
   the IDFs of the terms, etc. Similarly it would be interesting to get to this new view from clicking a query in the
   scatterplot view.
 * New comparison view of score distributions between two configurations. In this view the relevance engineer can for
   example query outliers in one configuration and see the scores in the other configuration. Views that support this
   use case could be for example a qq-plot or a scatter plot where the Y axis corresponds to scores in one configuration
   and the X axis corresponds to scores in the other configuration.