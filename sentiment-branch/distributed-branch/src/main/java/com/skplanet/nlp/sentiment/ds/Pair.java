package com.skplanet.nlp.sentiment.ds;

public class Pair {
	private String first = null;
	private String second = null;

    public Pair(String first, String second){
		this.first = first;
		this.second = second;
	}

	/**
	 * @return the first
	 */
	public String getFirst() {
		return first;
	}

	/**
	 * @param first the first to set
	 */
	public void setFirst(String first) {
		this.first = first;
	}

	/**
	 * @return the second
	 */
	public String getSecond() {
		return second;
	}

	/**
	 * @param second the second to set
	 */
	public void setSecond(String second) {
		this.second = second;
	}

	/**
	 * Get First elements from an array of Pair
	 *
	 * @param pairs array of Pair
	 * @return first elements from an array of Pair
	 */
	public static String [] getFirsts(Pair[] pairs) {
		String[] result = new String[pairs.length];
		for(int i = 0; i < pairs.length; i++) {
			result[i] = pairs[i].getFirst();
		}
		return result;
	}

	/**
	 * Get Second elements from an array of Pair
	 *
	 * @param pairs array of Pair
	 * @return second elements from an array of Pair
	 */
	public static String [] getSeconds(Pair[] pairs) {
		String[] result = new String[pairs.length];
		for(int i = 0; i < pairs.length; i++) {
			result[i] = pairs[i].getSecond();
		}
		return result;
	}

	@Override
	public String toString() {
		return this.first + ":" + this.second;
	}

}
