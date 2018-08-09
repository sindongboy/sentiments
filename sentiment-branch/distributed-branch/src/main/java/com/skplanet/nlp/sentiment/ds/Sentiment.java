package com.skplanet.nlp.sentiment.ds;

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
    private String[] morphs = null;
    private String[] postags = null;

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
        for (int i = 0; i < morphs.length; i++) {
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
        for (int i = 0; i < postags.length; i++) {
            this.postags[i] = postags[i];
        }
    }
}
