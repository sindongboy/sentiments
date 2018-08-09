package com.skplanet.nlp.sentiment.ds;

public class Expression {

    // ---------- Methods ----------- //
    private String id = null;
    private String text = null;
    private String [] morphs = null;
    private String [] postags = null;
    private String repAttribute = null;
    private int value = 0;
    private int start = 0;
    private int end = 0;

    // ----------- Members ----------- //

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * Default Constructor
     */
    public Expression() {
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the morphs
     */
    public String[] getMorphs() {
        return morphs;
    }

    /**
     * Set Morphs
     * @param morphs the morphs to set
     */
    public void setMorphs(String morphs) {
        String[] morphList = morphs.split(" ");
        this.morphs = new String[morphList.length];
        for (int i = 0; i < morphList.length; i++) {
            this.morphs[i] = morphList[i].split("/")[0];
        }
    }

    /**
     * Set Morphs
     *
     * @param morphs the morphs to set
     */
    public void setMorphs(String[] morphs) {
        this.morphs = new String[morphs.length];
        for (int i = 0; i < morphs.length; i++) {
            this.morphs[i] = morphs[i];
        }
    }

    /**
     * Get POS tags
     * @return the postags
     */
    public String[] getPostags() {
        return postags;
    }

    /**
     * Set POS tags
     * @param postags the postags to set
     */
    public void setPostags(String postags) {
        String[] postagList = postags.split(" ");
        this.postags = new String[postagList.length];
        for (int i = 0; i < postagList.length; i++) {
            this.postags[i] = postagList[i].split("/")[1];
        }
    }

    /**
     * Set POS tags
     *
     * @param postags the postags to set
     */
    public void setPostags(String[] postags) {
        this.postags = new String[postags.length];
        for (int i = 0; i < postags.length; i++) {
            this.postags[i] = postags[i];
        }
    }

    /**
     * Get Polarity value
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * Set Polarity value
     * @param value the value to set
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Get Rep. Attribute
     *
     * @return Rep. Attribute
     */
    public String getRepAttribute() {
        return this.repAttribute;
    }

    /**
     * Set Rep. Attribute
     *
     * @param rep Rep. Attribute to be set
     */
    public void setRepAttribute(String rep) {
        this.repAttribute = rep;
    }

    /**
     * Set Expression at once
     *
     * @param id id
     * @param text text
     * @param morphs morphs
     * @param postags pos-tags
     * @param value polarity
     */
    public void setExpression(String id, String text, String[] morphs, String[] postags, int value) {
        this.id = id;
        this.text = text;
        this.morphs = new String[morphs.length];
        for(int i = 0; i < morphs.length; i++) {
            this.morphs[i] = morphs[i];
        }
        this.postags = new String[postags.length];
        for(int i = 0; i < postags.length; i++) {
            this.postags[i] = postags[i];
        }
        this.value = value;
    }

    @Override
    public String toString() {
        return this.id + "," + this.text + "," + this.value;
    }
}
