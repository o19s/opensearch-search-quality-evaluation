package org.opensearch.eval.judgments.clickmodel;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class JudgmentParameters {

    @SerializedName("judgment_set_type")
    private String judgmentSetType;

    @SerializedName("judgment_set_generator")
    private String judgmentSetGenerator;

    @SerializedName("judgment_set_name")
    private String judgmentSetName;

    @SerializedName("judgment_set_description")
    private String judgmentSetDescription;

    @SerializedName("judgment_set_parameters")
    private Map<String, Object> judgmentSetParameters;

    public JudgmentParameters() {

    }

    public JudgmentParameters(final String judgmentSetType, final String judgmentSetGenerator, final String judgmentSetName,
                                final String judgmentSetDescription, final Map<String, Object> judgmentSetParameters) {

        this.judgmentSetType = judgmentSetType;
        this.judgmentSetGenerator = judgmentSetGenerator;
        this.judgmentSetName = judgmentSetName;
        this.judgmentSetDescription = judgmentSetDescription;
        this.judgmentSetParameters = judgmentSetParameters;

    }

    public String getJudgmentSetType() {
        return judgmentSetType;
    }

    public String getJudgmentSetGenerator() {
        return judgmentSetGenerator;
    }

    public String getJudgmentSetName() {
        return judgmentSetName;
    }

    public String getJudgmentSetDescription() {
        return judgmentSetDescription;
    }

    public Map<String, Object> getJudgmentSetParameters() {
        return judgmentSetParameters;
    }

}
