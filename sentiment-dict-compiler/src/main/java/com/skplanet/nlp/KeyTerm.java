package com.skplanet.nlp;

import com.skplanet.nlp.config.Configuration;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Key Term Generator
 *
 * @author Donghun Shin / donghun.shin@sk.com
 */
public final class KeyTerm {
    private static final Logger LOGGER = Logger.getLogger(KeyTerm.class.getName());

    // instance
    private static KeyTerm instance = null;

    /**
     * Create Instance
     * @return instance of {@link KeyTerm}
     */
    public static KeyTerm getInstance() {
        if(instance == null) {
            synchronized (KeyTerm.class){
                instance = new KeyTerm();
            }
        }
        return instance;
    }

    private static final String KEYTERM_CONFIG_NAME = "keyterms.properties";

    // Keyterm Tags Declaration
    private static final String BOUNDARY_TAG = "BOUNDARY_TAG";
    private static final String SENTENCE_END_TAG = "SENTENCE_END_TAG";
    private static final String NEGATION_TAG = "NEGATION_TAG";
    private static final String FRONT_NEGATION_LEXS = "FRONT_NEGATION_LEXS";
    private static final String KEYTERM_POSTAG = "KEYTERM_POSTAG";

    public static final String SKIP_TAG = ">";
    public static final String QUESTION_TAG = "?";

    //----------------------------------/
    // members
    //----------------------------------//
    private String boundaryTag = null;
    private String sentenceBoundaryTag = null;
    private String negationTag = null;
    private List<String> frontNegLexs = null;
    private Set<String> keytermPostags = null;

    private boolean moreEtm = false;

    // constructor
    private KeyTerm() {
        Configuration config = Configuration.getInstance();
        try {
            config.loadProperties(KEYTERM_CONFIG_NAME, Configuration.CLASSPATH_LOAD);
        } catch (NullPointerException e) {
            LOGGER.error("can't find keyterm properties", e);
        } catch (IOException e) {
            LOGGER.error("fail to read keyterm properties", e);
        }

        // set boundary tag
        boundaryTag = config.readProperty(KEYTERM_CONFIG_NAME, BOUNDARY_TAG);
        LOGGER.debug("boundary tag: " + boundaryTag);
        // set sentence boundary tag
        sentenceBoundaryTag = config.readProperty(KEYTERM_CONFIG_NAME, SENTENCE_END_TAG);
        LOGGER.debug("sentence boundary tag: " + sentenceBoundaryTag);
        // set negation tag
        negationTag = config.readProperty(KEYTERM_CONFIG_NAME, NEGATION_TAG);
        LOGGER.debug("negation tag: " + negationTag);

        // set front negation terms
        String [] frontEnds = config.readProperty(KEYTERM_CONFIG_NAME, FRONT_NEGATION_LEXS).split(",");
        frontNegLexs = new ArrayList<String>();
        for(String f : frontEnds) {
            LOGGER.debug("front negation tag: " + f);
            frontNegLexs.add(f);
        }

        // set keyterm postags
        String [] kTags = config.readProperty(KEYTERM_CONFIG_NAME, KEYTERM_POSTAG).split(",");
        keytermPostags = new HashSet<String>();
        for(String tag : kTags) {
            LOGGER.debug("keyterm postags: " + tag);
            keytermPostags.add(tag);
            // add special tags
            keytermPostags.add(boundaryTag);
            keytermPostags.add(sentenceBoundaryTag);
            keytermPostags.add(negationTag);
            keytermPostags.add(SKIP_TAG);
        }
    }

    //----------------------------------//
    // getter & setter
    //----------------------------------//

    /**
     * Get Boundary Tag
     *
     * @return boundary tag
     */
    public String getBoundaryTag() {
        return boundaryTag;
    }

    /**
     * Get Sentence boundary tag
     *
     * @return sentence boundary tag
     */
    public String getSentenceBoundaryTag() {
        return sentenceBoundaryTag;
    }

    /**
     * Get negation tag
     *
     * @return negation tag
     */
    public String getNegationTag() {
        return negationTag;
    }

    /**
     * Check if given term is "front negation" term
     *
     * @param key term to be checked
     * @return true if given term is front negation term, otherwise false
     */
    public boolean isFrontNegationTerm(String key) {
        return frontNegLexs.contains(key);
    }

    /**
     * get keyterm postags
     *
     * @return keyterm postags
     */
    public Set<String> getKeytermPostags() {
        return keytermPostags;
    }

    /**
     * Check if given tag is among keyterm postags
     *
     * @param postag postag to be checked
     * @return true if given tag is among keyterm postags, otherwise false
     */
    public boolean isKeytermPostags(String postag) {
        return keytermPostags.contains(postag);
    }

    /**
     * Check if given tag is boundary tag
     *
     * @param tag tag to be checked
     * @return true if given tag is boundary tag
     */
    public boolean isBoundaryTag(String tag) {
        return boundaryTag.equals(tag);
    }

    /**
     * Check if given tag is sentnece boundary tag
     *
     * @param tag tag to be checked
     * @return true if given tag is sentence boundary tag
     */
    public boolean isSentenceBoundaryTag(String tag) {
        return sentenceBoundaryTag.equals(tag);
    }

    /**
     * Check if given tag is neagtion tag
     *
     * @param tag tag to be checked
     * @return true if given tag is negation tag
     */
    public boolean isNegationTag(String tag) {
        return negationTag.equals(tag);
    }

    //----------------------------------//
    // methods
    //----------------------------------//

    /**
     * Keyterm Extraction
     *
     * 1. apply keyterm postags
     * 2. adjective phrase handle
     * 3. idioms handle
     *
     * caution: please, use right before dictionary search
     *
     * @param toks original tokens
     * @param tags original postags
     * @return keyterm processed {@link Pair} object
     */
    public Pair[] process(String [] toks, String [] tags) {
        List<Pair> result = new ArrayList<Pair>();

        // adjective phrase ==> normalized phrase
        Pair[] normalizedTerms;
        do {
            normalizedTerms = adjPhrase2NormalPhrase(toks, tags);
            toks = Pair.getFirsts(normalizedTerms);
            tags = Pair.getSeconds(normalizedTerms);
        } while (this.moreEtm);

        for (Pair p : normalizedTerms) {
            // keyterm postag 가 아니면, SKIP TAG 로 대채한다.
            if (!isKeytermPostags(p.getSecond())) {
                LOGGER.debug("not a keyterm candidate: " + p.toString());
                p.setFirst(SKIP_TAG);
                p.setSecond(SKIP_TAG);
            } else {
                LOGGER.debug("is a keyterm candidate: " + p.toString());
            }
            result.add(p);
        }

        return result.toArray(new Pair[result.size()]);
    }


    /**
     * look up any adjective phrase and transform them to normal phrase
     *
     * @param toks tokens
     * @param tags postags
     * @return normalized phrase
     */
    private Pair[] adjPhrase2NormalPhrase(String [] toks, String [] tags) {

        List<Pair> result = new ArrayList<Pair>();
        Pair[] terms = arraysToPairs(toks, tags);
        String debugStr = "";
        for(Pair p : terms) {
            debugStr += p.toString() + " ";
        }
        LOGGER.debug("keyterm adj-phrased input: " + debugStr);

        int eIndex = -1;
        int end = -1;
        int start = -1;

        // find etm
        for(int i = 0; i < terms.length; i++) {
            if(terms[i].getSecond().equals(NLPTags.ETM) && terms[i].getFirst().equals("은")) {
                eIndex = i;
                break;
            }
        }
        // no etm found, return as it was
        if(eIndex < 0) {
            this.moreEtm = false;
            return terms;
        }


        // find end index
        for(int i = eIndex + 1; i < terms.length; i++) {
            if(terms[i].getSecond().startsWith(NLPTags.NN) ||
                    terms[i].getSecond().equals(NLPTags.ENG) ||
                    terms[i].getSecond().equals(NLPTags.MM) ||
                    terms[i].getSecond().equals(NLPTags.MAG) ||
                    terms[i].getSecond().equals(NLPTags.NR)) {
                end = i;
                continue;
            }
            break;
        }
        // no after-etm found, return as it was
        if(end < 0) {
            this.moreEtm = false;
            return terms;
        }

        // find start index
        for(int i = eIndex - 1; i >= 0; i--) {
            // pos filter
            if(terms[i].getSecond().startsWith(NLPTags.VA) ||
                    terms[i].getSecond().startsWith(NLPTags.VV) ||
                    terms[i].getSecond().startsWith(NLPTags.MAG)) {
                start = i;
                continue;
            }
            break;
        }
        // no preceeding term before etm, return as it was
        if(start < 0) {
            this.moreEtm = false;
            return terms;
        }

        // re-ordering

        // 1. 0 ~ (start - 1)
        result.addAll(Arrays.asList(terms).subList(0, start));

        // 1.5 add Boundary start tag
		/*
		// TODO: 이부분에 대한 로직은 좀 더 고민이 필요하다
        Pair sBoundMark = new Pair(boundaryTag, boundaryTag);
        result.add(sBoundMark);
        */

        // 2. (eIndex + 1) ~ end
        result.addAll(Arrays.asList(terms).subList(eIndex + 1, end + 1));
        // 3. start ~ (eIndex - 1)
        result.addAll(Arrays.asList(terms).subList(start, eIndex));

        // 3.5 add Boundary end tag and Phrase end tag
        // - 단,
        // 1. 이어지는 term 이 "도/jks" 이 아니라면, boundary tag는 쓰지 않는다.
        Pair endMakr = new Pair(sentenceBoundaryTag, sentenceBoundaryTag);
        Pair eBoundMark = new Pair(boundaryTag, boundaryTag);

        result.add(endMakr);
        if(!(terms[end+1].getFirst().equals("도") && terms[end+1].getSecond().equals(NLPTags.JKS)
        )) {
            result.add(eBoundMark);
        }
        // 4. (end + 1) ~ length
        result.addAll(Arrays.asList(terms).subList(end + 1, terms.length));


        // check more etm
        this.moreEtm = false;
        for(int i = eIndex; i < result.size(); i++) {
            if(result.get(i).getSecond().equals(NLPTags.ETM)) {
                this.moreEtm = true;
                break;
            }
        }
        return result.toArray(new Pair[result.size()]);
    }

    private Pair[] arraysToPairs(String [] toks, String [] tags) {
        Pair[] result = new Pair[toks.length];
        for(int i = 0; i < toks.length; i++) {
            result[i] = new Pair(toks[i], tags[i]);
        }
        return result;
    }

}
