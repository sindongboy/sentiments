package com.skplanet.nlp.sentiment.util;

import com.skplanet.nlp.NLPAPI;
import com.skplanet.nlp.NLPDoc;
import com.skplanet.nlp.config.Configuration;
import com.skplanet.nlp.morph.Morphs;
import com.skplanet.nlp.sentiment.ds.Pair;
import org.apache.log4j.Logger;

/**
 * NLP-Indexterm based NLP Utilities (Singleton)
 */
public final class NLPUtils {
	private static final Logger LOGGER = Logger.getLogger(NLPUtils.class.getName());
	private static final String CONFIG_NAME = "nlp_api.properties";

	private static NLPUtils instance = null;

	private static NLPAPI nlpApi = null;

	/**
	 * Get Instance
	 * @return instance of NLP Class
	 */
	public static NLPUtils getInstance() {
		if (instance == null) {
			// method synchronized
			synchronized (NLPUtils.class) {
				instance = new NLPUtils();
			}
		}
		return instance;
	}

	// private constructor
	private NLPUtils() {
		LOGGER.info("NLP initializing ....");

        nlpApi = new NLPAPI(CONFIG_NAME, Configuration.CLASSPATH_LOAD);
		LOGGER.info("NLP initializing done");
	}

	// ----------- Static Utilities ------------ //

	/**
	 * Return NLP API itself
	 * @return nlp api
	 */
	public NLPAPI getNLPAPI() {
		return nlpApi;
	}

	/**
	 * Get Sentences from given text, possibly composed of multiple sentences
	 *
	 * @param text Text to be splited into single sentences
	 * @return array of sentences
	 */
	public String [] getSentences(String text) {
		return nlpApi.doSegmenting(text);
	}

	/**
	 * Get Morph List from given text
	 *
	 * @param text Text to be nlp-analyzed
	 * @return array of Morphs 
	 */
	public String [] getMorphs(String text) {
		NLPDoc nlpRes = nlpApi.doNLP(text);
		Morphs morphs = nlpRes.getMorphs();
		String [] result = new String[morphs.getCount()];
		for(int i = 0; i < morphs.getCount(); i++) {
			result[i] = morphs.getMorph(i).getTextStr();
		}
		return result;
	}

	/**
	 * Get POS Tag List from given text
	 *
	 * @param text Text to be nlp-analyzed
	 * @return array of POS-Tags
	 */
	public String [] getPOSTags(String text) {
		NLPDoc nlpRes = nlpApi.doNLP(text);
		Morphs morphs = nlpRes.getMorphs();
		String [] result = new String[morphs.getCount()];
		for(int i = 0; i < morphs.getCount(); i++) {
			result[i] = morphs.getMorph(i).getPosStr();
		}
		return result;
	}

    /**
     * Get NLP Result Pair
     * @param text text to be nlp-analyzed
     * @return pair of nlp result
     */
    public Pair[] getNLPResult(String text) {
        String[] postag = getPOSTags(text);
        String[] tokens = getMorphs(text);
        Pair[] result = new Pair[postag.length];
        for (int i = 0; i < postag.length; i++) {
            result[i] = new Pair(tokens[i], postag[i]);
            /*
            result[i].setFirst(tokens[i]);
            result[i].setSecond(postag[i]);
            */
        }
        return result;
    }

}
