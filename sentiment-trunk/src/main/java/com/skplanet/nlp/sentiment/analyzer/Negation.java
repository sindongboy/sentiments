package com.skplanet.nlp.sentiment.analyzer;

import com.skplanet.nlp.sentiment.ds.Pair;
import com.skplanet.nlp.sentiment.util.NLPTags;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Negation Checker implementation
 *
 * @author Donghun Shin / donghun.shin@sk.com
 */
public final class Negation {
    private static final Logger LOGGER = Logger.getLogger(Negation.class.getName());

    private Negation() {

    }

    //----------------------------------//
    // members
    //----------------------------------//
    private static KeyTerm keyterm = KeyTerm.getInstance();

    //----------------------------------//
    // methods
    //----------------------------------//

    /**
     * negation checker
     * @param terms nlp result
     * @param tIdx target vv, or va
     * @return true if it is negated, otherwise false
     */
    public static boolean isNegated(Pair[] terms, int tIdx) {
        List<String> tokens = new ArrayList<String>();
        List<String> tags = new ArrayList<String>();
        for (Pair p : terms) {
            if (p.getFirst() != null && p.getSecond() != null) {
                tokens.add(p.getFirst());
                tags.add(p.getSecond());
            }
        }

        return check(tokens.toArray(new String[tokens.size()]), tags.toArray(new String[tags.size()]), tIdx);
    }


    /**
     * Negation Checker
     * @param tokens tokens
     * @param postags pos tags
     * @param tIdx target index
     * @return true if it is negated, otherwise return false
     */
    public static boolean isNegated(String[] tokens, String[] postags, int tIdx) {
        return check(tokens, postags, tIdx);
    }

    private static boolean check(String[] tokens, String[] tags, int tIdx) {
        if(!(tags[tIdx].equals(NLPTags.VA) || tags[tIdx].equals(NLPTags.VV))) {
            LOGGER.debug("negation check isn't applicable to \"" + tokens[tIdx] + "/" + tags[tIdx] + "\"");
            return false;
        }
        LOGGER.debug("following term is checked if negated: " + tokens[tIdx] + "/" + tags[tIdx]);

        int result = 1;
        ArrayList<Integer> negMatrix = new ArrayList<Integer>();
        ArrayList<Integer> conjuntMatrix = new ArrayList<Integer>();

        // context checking.
        int index = 0;
        for (int i = 0; i < tokens.length; i++) {

            if (tags[i].endsWith(NLPTags.VXN)) {
                negMatrix.add(index);
            }

            if (tags[i].endsWith(NLPTags.EC) && tokens[i].equals("고")) {
                conjuntMatrix.add(index);
            }

            index++;
        }


        for (int i = tIdx + 1; i < tags.length; i++) {

            if(keyterm.isBoundaryTag(tags[i]) || keyterm.isSentenceBoundaryTag(tags[i])) {
                break;
            }

            if (keyterm.isNegationTag(tags[i])) {
                result = result * -1;
            }
        }

        if (tIdx > 0) {
            if(keyterm.isFrontNegationTerm(tokens[tIdx - 1])) {
                result = result * -1;
            }
        }

        return result <= 0;
    }

}

