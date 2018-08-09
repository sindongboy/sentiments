package com.skplanet.nlp.sentiment.ds;

public class Attribute {

	private String id = null;
	private String text = null;
	private String[] morphs = null;
	private String[] postags = null;
	private String[] syns = null;
    private int start = 0;
    private int end = 0;

	/**
	 * Default Constructor
	 */
	public Attribute() {
	}

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

	/**
	 * @return the syns
	 */
	public String[] getSyns() {
		return syns;
	}

	/**
	 * @param syns the syns to set
	 */
	public void setSyns(String[] syns) {
		this.syns = new String[syns.length];
		for(int i = 0; i < syns.length; i++) {
			this.syns[i] = syns[i];
		}
	}

	/**
	 * Set Attribute at once
	 *
	 * @param id id
	 * @param text text
	 * @param morphs morphs
	 * @param postags pos-tags
	 * @param syns attribute synonyms
	 */
	public void setAttribute(String id, String text, String[] morphs, String[] postags, String[] syns){
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
		this.syns = new String[syns.length];
		for(int i = 0; i < syns.length; i++) {
			this.syns[i] = syns[i];
		}
	}

}
