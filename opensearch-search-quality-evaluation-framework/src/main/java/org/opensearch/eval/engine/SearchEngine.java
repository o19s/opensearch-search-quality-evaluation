package org.opensearch.eval.engine;

import org.opensearch.eval.model.ClickthroughRate;
import org.opensearch.eval.model.data.Judgment;
import org.opensearch.eval.model.ubi.query.UbiQuery;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public abstract class SearchEngine {

    public abstract boolean doesIndexExist(String index) throws IOException;
    public abstract boolean createIndex(String index, Map<String, Object> mapping) throws IOException;
    public abstract boolean deleteIndex(String index) throws IOException;

    public abstract String getUserQuery(final String queryId) throws Exception;
    public abstract UbiQuery getQueryFromQueryId(final String queryId) throws Exception;
    public abstract long getCountOfQueriesForUserQueryHavingResultInRankR(final String userQuery, final String objectId, final int rank) throws Exception;
    public abstract void indexRankAggregatedClickthrough(final Map<Integer, Double> rankAggregatedClickThrough) throws Exception;
    public abstract void indexClickthroughRates(final Map<String, Set<ClickthroughRate>> clickthroughRates) throws Exception;
    public abstract String indexJudgments(final Collection<Judgment> judgments) throws Exception;

    public abstract boolean bulkIndex(String index, Map<String, Object> documents) throws IOException;

    public abstract Collection<Judgment> getJudgments(final String index) throws IOException;

}
