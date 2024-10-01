package org.opensearch.qef.clickmodel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.qef.model.Judgment;

import java.io.IOException;
import java.util.Collection;

public abstract class ClickModel<T extends ClickModelParameters> {

    public abstract Collection<Judgment> calculateJudgments() throws IOException;

    private static final Logger LOGGER = LogManager.getLogger(ClickModel.class.getName());

    public void showJudgments(final Collection<Judgment> judgments) {

        for(final Judgment judgment : judgments) {

            LOGGER.info(judgment.toString());

        }

    }

}
