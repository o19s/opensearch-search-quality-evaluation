# All credit to Daniel Wrigley for notebook code:
# https://colab.research.google.com/drive/1eIZou9UP6-rUjrHZLqvrEsocsp75LEEq

import urllib

import pandas as pd
import numpy as np
import os


# Method of moments estimation. Requires mean * (1 - mean) > var (which is normally given)
def alpha_beta(mean, var):

    a_b = (mean * (1 - mean) / var) - 1
    return mean * a_b,  (1 - mean) * a_b


# alternative: fall back to fitting a beta distribution (we don't use this)
# def alpha_beta_fit(ctx, mean, var):
#
#     a,b = alpha_beta(mean, var)
#     if (a >= 0) and (b>=0):
#
#         return a,b
#
#     a,b,_,_ = beta.fit(df_data[df_data['ctx'] == ctx ].cp)
#     return a,b

# bootstrapping the pooled prior
def mu_and_sigma2(cps, weights = None, percentile=99, sample_size=None, iterations=1000):

    means = []
    vars = []

    if sample_size is None:
        sample_size = len(cps)

    if weights is None:
        for _ in range(iterations):
            sample = np.random.choice(a=cps, size=sample_size, replace=True)
            means.append(sample.mean())
            vars.append(sample.var())
    else:
        draw_probs = weights / weights.sum()

        for _ in range(iterations):
            sample = np.random.choice(a=cps, size=sample_size, p=draw_probs, replace=True)
            means.append(sample.mean())
            vars.append(sample.var())

    return np.mean(means), np.percentile(vars, percentile)


def make():

    names = ['query_id', 'position', 'num_results', 'clicked', 'doc_id']

    # TODO: The events would come from OpenSearch UBI data.
    if not os.path.isfile("./raw_events.zip"):
        print("Downloading events file...")
        urllib.request.urlretrieve("https://o19s-search-result-quality.s3.amazonaws.com/raw_events.zip", "./raw_events.zip")

    print("Loading raw events...")
    df_raw_events = pd.read_csv("./raw_events.zip", names=names, header=0)

    df_raw_events_grouped = df_raw_events.groupby(by=['query_id','doc_id','position','num_results'], as_index=False).agg({
        'clicked': ['sum','count']
    })

    df_raw_events = df_raw_events_grouped.copy()

    # We've loaded the raw data and rename some columns to fit the notebook assumptions
    df_raw_events.columns = ['query', 'product', 'position', 'num_results', 'clicks', 'views']    

    # Context ID ends up in ctx column
    df_raw_events['ctx'] = df_raw_events.position.apply(str) + '_' + df_raw_events.num_results.apply(lambda x: '401' if x > 400 else str(x))

    # aggregation per query-doc pair and context (might already be given in raw data)
    df_aggr_events = df_raw_events.groupby(by=['query','product','ctx'], as_index=False).agg({
        'views': 'sum',
        'clicks': 'sum',
        'position': 'first'
    })

    # ctr (= cp = click probability per query-doc pair)
    df_aggr_events['cp'] = df_aggr_events.clicks/df_aggr_events.views

    # Grouping. We add the count per context for better analysis and clean-up
    df_grouped_events = df_aggr_events.groupby(by='ctx', as_index=False).agg({
        'cp': ['count','mean', 'var']
    })
    df_grouped_events.columns = ['ctx', 'cp_count', 'cp_mean', 'cp_var']

    # Clean-up. It's not nice but also not a big problem if we have to drop some of the contexts here.
    # They would normally have only very few members - so it will be fair to just use the pooled
    # prior later on

    # We cannot deal with count < 2 (the sample variance would be undefined), also cp_mean == 0 (no click in a context)
    # and cp_var == 0 (all queries in the context have the same click-through) have a smell.
    df_ctx = df_grouped_events.query('(cp_count > 1) & (cp_mean > 0) & (cp_var > 0)')
    print('Dropped contexts: {}'.format(df_grouped_events.shape[0] - df_ctx.shape[0]))

    # Add the context stats to the query-document pairs
    df_merged_events = df_aggr_events.copy()
    df_merged_events = pd.merge(df_merged_events, df_ctx , how='left', on='ctx')

    # This df holds clicks and views per query-document pair, aggregated across contexts.
    # We use _qd to mark variables per query-doc
    df_query_product = df_aggr_events.groupby(by=['query', 'product'], as_index=False).agg({
        'views': 'sum',
        'clicks': 'sum',

    })
    df_query_product = df_query_product.rename(columns={
        "views": "views_qd",
        'clicks': 'clicks_qd',
    })

    # add the aggregated query-doc stats and the context stats to the main df
    df_merged_events = pd.merge(df_aggr_events, df_query_product, on=['query', 'product'])
    df_merged_events = pd.merge(df_merged_events, df_ctx, how='left', on='ctx')

    # The sample variance can be NaN (if there were too few observations. We've dropped these case in df_ctx but not
    # in the query-document pairs). We set the variance to 0 in these cases and leave it 0 when it actually is 0.
    # Remember that 'views' contains the views of the query-document pair per context and views_qd contains the
    # context independent views of the query-document pair.
    df_merged_events['weight'] = df_merged_events.cp_var.apply(lambda x: 0 if x == 0 or np.isnan(x) else 1/x) * df_merged_events.views / df_merged_events.views_qd
    df_merged_events.head(5)

    # apply the weight. We first apply it per query,doc,ctx....
    df_merged_events['w_cp_mean'] = df_merged_events.cp_mean * df_merged_events.weight

    # ... and then take the sum when we start our judgment df, which is grouped by query-doc
    df_judgments = df_merged_events.groupby(by=['query', 'product'], as_index=False).agg({
        'w_cp_mean': 'sum',
        #'w_cp_var': 'sum',
        'views': 'sum',
        'clicks': 'sum',
        'position': 'mean',
        'cp': 'mean',
        'weight': 'sum'
    })
    #df_judgments.head(5)

    #mu,sigma2 =  mu_and_sigma2(df_ctx.cp_mean, df_ctx.cp_count,sample_size=100000)
    mu ,sigma2 = mu_and_sigma2(df_ctx.cp_mean, percentile=99, sample_size=100000)
    print(mu, sigma2)

    # finally, the params of our beta distribution. We calculate prior params per query-doc pair
    df_judgments['theta'] = (df_judgments['w_cp_mean'] + mu/sigma2) / (df_judgments['weight'] + 1/sigma2)
    df_judgments['tau2'] = df_judgments['weight'].apply(lambda x: sigma2 if x == 0 else 1 / (x + 1/sigma2))

    # re-parameterise Beta(theta,tau2) to Beta(alpha,beta)
    df_judgments['alpha'],df_judgments['beta'] = zip(*df_judgments.apply(lambda row: alpha_beta(row.theta,row.tau2), axis=1))

    df_judgments['expected'] = (df_judgments.alpha / (df_judgments.alpha + df_judgments.beta))
    df_judgments['posterior'] = (df_judgments.alpha + df_judgments.clicks)/(df_judgments.alpha + df_judgments.beta + df_judgments.views)

    # judgment (>0)
    df_judgments['judgment'] = df_judgments.posterior / df_judgments.expected
    
    # log judgment (-inf < log judgment < inf, where 1 means 'neutral')
    df_judgments['log_judgment'] = np.log(df_judgments.judgment)

    print(df_judgments.head(5))

    # TODO: Do something useful with the judgments.
    # save to file. You might just want columns ['query','product','log_judgment','judgment']
    df_judgments.to_csv("./judgments.csv", index=False)