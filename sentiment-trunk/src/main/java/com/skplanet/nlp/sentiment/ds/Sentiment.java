package com.skplanet.nlp.sentiment.ds;

import com.skplanet.nlp.sentiment.knowledge.SentimentDict;

public class Sentiment {

    private static final String SENTIMENT_PREFIX = "S";
    private static final String REP_ATT_PREFIX = "R";
    private static final String ATT_PREFIX = "A";
    private static final String EXP_PREFIX = "E";
    private static final String POS_PREFIX = "P";
    private static final String NEG_PREFIX = "N";

	// --------- Members --------- //
	private Attribute attribute = null;
	private Expression expression = null;
	private String sentence = null;
	private String [] morphs = null;
	private String [] postags = null;

	/**
	 * default constructor
	 */
	public Sentiment() {
	}

	/**
	 * @return the attribute
	 */
	public Attribute getAttribute() {
		return attribute;
	}

	/**
	 * @param attribute the attribute to set
	 */
	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	/**
	 * @return the expression
	 */
	public Expression getExpression() {
		return expression;
	}

	/**
	 * @param expression the expression to set
	 */
	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	/**
	 * @return the sentence
	 */
	public String getSentence() {
		return sentence;
	}

	/**
	 * @param sentence the sentence to set
	 */
	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	/**
	 * @return the morphs
	 */
	public String[] getMorphs() {
		return morphs;
	}

	/**
	 * @param morphs the morphs to set
	 */
	public void setMorphs(String[] morphs) {
		this.morphs = new String[morphs.length];
		for(int i = 0; i < morphs.length; i++) {
			this.morphs[i] = morphs[i];
		}
	}

	/**
	 * @return the postags
	 */
	public String[] getPostags() {
		return postags;
	}

	/**
	 * @param postags the postags to set
	 */
	public void setPostags(String[] postags) {
		this.postags = new String[postags.length];
		for(int i = 0; i < postags.length; i++) {
			this.postags[i] = postags[i];
		}
	}

    @Override
    public String toString() {

        String suffix;
        if (this.expression.getValue() < 0) {
            suffix = NEG_PREFIX;
        } else {
            suffix = POS_PREFIX;
        }

        String attributeLocal = this.attribute.getText().replace(" ", "").trim();
        String expressionLocal = this.expression.getText().replace(" ", "").trim();

        if (SentimentDict.isException(attributeLocal, expressionLocal)) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        // REP
        sb.append(SENTIMENT_PREFIX + REP_ATT_PREFIX).append(suffix).append("_").append(this.expression.getRepAttribute().replace(" ", "")).append("\t");

        // ATT
        String attributeRes;
        if (this.attribute.getText().equals("null")) {
            attributeRes = "null";
            sb.append(this.attribute.getText()).append("\t");
        } else {
            if (SentimentDict.isActor(this.attribute.getText().replace(" ", ""))) {
                //attributeRes = this.attribute.getText().replace(" ", "") + "_A"; // actor
                attributeRes = this.attribute.getText().replace(" ", "") + "_P"; // actor --> P 로 통일
            } else if (SentimentDict.isDirector(this.attribute.getText().replace(" ", ""))) {
                //attributeRes = this.attribute.getText().replace(" ", "") + "_D"; // director
                attributeRes = this.attribute.getText().replace(" ", "") + "_P"; // director --> P 로 통일
            } else {
                attributeRes = this.attribute.getText().replace(" ", "");
            }
            sb.append(SENTIMENT_PREFIX + ATT_PREFIX + suffix + "_" + attributeRes).append("\t");
        }

        if (attributeRes.endsWith("_P")) {
            sb.append(SENTIMENT_PREFIX + EXP_PREFIX + suffix + "_" + this.attribute.getText().replace(" ", "") + "_" + this.expression.getText().replace(" ", "")).append("_P");
            /* _A _D --> P 로 통일
            sb.append(SENTIMENT_PREFIX + EXP_PREFIX + suffix + "_" + this.attribute.getText().replace(" ", "") + "_" + this.expression.getText().replace(" ", "")).append("_A");
        } else if (attributeRes.endsWith("_D")) {
            sb.append(SENTIMENT_PREFIX + EXP_PREFIX + suffix + "_" + this.attribute.getText().replace(" ", "") + "_" + this.expression.getText().replace(" ", "")).append("_D");
            */
        } else {
            sb.append(SENTIMENT_PREFIX + EXP_PREFIX + suffix + "_" + this.attribute.getText().replace(" ", "") + "_" + this.expression.getText().replace(" ", ""));
        }

        return sb.toString();
    }
}
