package org.opensearch.qef;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.qef.clickmodel.coec.CoecClickModel;
import org.opensearch.qef.clickmodel.coec.CoecClickModelParameters;
import org.opensearch.qef.model.Judgment;

import java.util.Collection;

/**
 * Entry point for the OpenSearch Evaluation Framework.
 */
public class App {

    private static final Logger LOGGER = LogManager.getLogger(App.class.getName());

    public static void main(String[] args) throws Exception {

        final CoecClickModelParameters coecClickModelParameters = new CoecClickModelParameters(false, 20);
        final CoecClickModel coecClickModel = new CoecClickModel(coecClickModelParameters);

        final Collection<Judgment> judgments = coecClickModel.calculateJudgments();
        Judgment.showJudgments(judgments);

    }

}
