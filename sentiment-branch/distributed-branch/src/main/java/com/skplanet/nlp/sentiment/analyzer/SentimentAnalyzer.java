package com.skplanet.nlp.sentiment.analyzer;


import com.skplanet.nlp.sentiment.ds.Attribute;
import com.skplanet.nlp.sentiment.ds.Expression;
import com.skplanet.nlp.sentiment.ds.Pair;
import com.skplanet.nlp.sentiment.ds.Sentiment;
import com.skplanet.nlp.sentiment.knowledge.Collocation;
import com.skplanet.nlp.sentiment.knowledge.SentimentDict;
import com.skplanet.nlp.sentiment.util.ArrayUtils;
import com.skplanet.nlp.sentiment.util.NLPTags;
import com.skplanet.nlp.trie.TokenTrie;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Sentiment Analyzer
 *
 * extract {@link Sentiment} from text
 *
 * @author Donghun Shin / donghun.shin@sk.com
 */
public class SentimentAnalyzer {
    // logger
    private static final Logger LOGGER = Logger.getLogger(SentimentAnalyzer.class.getName());

    private Collocation collocation = null;
    private KeyTerm keyterm = null;

    private TokenTrie attributeDict = null;
    private Map<String, TokenTrie> attExpMap = null;

    /**
     * default constructor
     */
    public SentimentAnalyzer(SentimentDict dict) throws IOException {
        collocation = Collocation.getInstance();
        keyterm = KeyTerm.getInstance();
        attributeDict = dict.getAttributeDict();
        attExpMap = dict.getAttExpMap();
    }

    /**
     * Sentiment Dictionary Search
     *
     * @param text sentence to be analyzed
     * @return list of {@link Sentiment} contains attribute, expression, and polarity
     */
    public List<Sentiment> find(String text) throws IOException {

        // sentiment result container
        List<Sentiment> sentiments = new ArrayList<Sentiment>();

        // ---------------------------------------- //
        // Preprocess
        // ---------------------------------------- //
        Set<Integer> skipPosition = new HashSet<Integer>();
        Set<Integer> boundaryPosition = new HashSet<Integer>();
        Set<Integer> degreePosition = new HashSet<Integer>();

        // collocation
        Pair[] tmpColloc = collocation.process(text, 3, true);
        if (tmpColloc == null) {
            return sentiments;
        }
        String[] sentToks = Pair.getFirsts(tmpColloc);
        String[] sentTags = Pair.getSeconds(tmpColloc);

        // negation check
        Set<Integer> negatedPosition = new HashSet<Integer>();
        for(int i = 0; i < sentToks.length; i++) {
            if(Negation.isNegated(sentToks, sentTags, i)) {
                negatedPosition.add(i);
                LOGGER.debug("negated index: " + i + " (" + sentToks[i] + ")");
            }

            if(this.keyterm.isNegationTag(sentTags[i])) {
                skipPosition.add(i);
                LOGGER.debug("negation term pos : " + i);
            }
        }

        // keyterms process
        Pair[] keytermResult = keyterm.process(sentToks, sentTags);
        for(int i = 0; i < keytermResult.length; i++) {
            // skip tag position
            if(keytermResult[i].getSecond().equals(KeyTerm.SKIP_TAG)) {
                skipPosition.add(i);
            }
            // boundary position
            if(keytermResult[i].getSecond().equals(keyterm.getBoundaryTag())){
                boundaryPosition.add(i);
            }
            // degree position
            if(keytermResult[i].getSecond().equals(NLPTags.MAG)) {
                skipPosition.add(i);
                degreePosition.add(i);
            }
        }

        // Generate Sentence Key
        String[] sentKeys = SentimentDict.createKey(Pair.getFirsts(keytermResult), Pair.getSeconds(keytermResult));

        LOGGER.debug("sentKeys: " + ArrayUtils.array2string(sentKeys));

        // --------------------------------------------- //
        // find attribute first
        // --------------------------------------------- //

        // sentence keys 의 각 토큰을 돌면서 prefix matching 을 시도한다.
        int attOffset;
        for(int attIndex = 0; attIndex < sentKeys.length; attIndex++) {
            // TokenTrie Search
            attOffset = this.attributeDict.prefixRelaxedMatch(sentKeys, attIndex, skipPosition);

            if(attOffset != attIndex) { // prefix matching successed

                // prefix matching successed ==> Attribute Matched !
                // ----------------------------------------------------------------- //
                LOGGER.debug("attribute prefix matched: " + ArrayUtils.array2string(ArrayUtils.subArray(sentKeys, attIndex, attOffset)));
                // Get Attribute Object
                //Attribute attribute = (Attribute) this.attributeDict.RelaxedMatch(ArrayUtils.subArray(sentKeys, attIndex, attOffset), skipPosition);
                Attribute attribute = (Attribute) this.attributeDict.RelaxedMatch(sentKeys, attIndex, skipPosition);

                if(attribute == null) {
                    LOGGER.debug("Attribute prefix-matched but not exact matched!");
                    continue;
                } else {
                    LOGGER.debug("Attribute Matched : " + attribute.getText());
                }

                // set expression search starting index
                int expIndex = attIndex + 1;
                if(expIndex >= sentKeys.length) { //last term, finish finding.
                    break;
                }

                // --------------------------------------------- //
                // find expressions
                // --------------------------------------------- //

                // get expression token trie dictionary
                TokenTrie eTrie = this.attExpMap.get(attribute.getId());
                if(eTrie == null) {
                    LOGGER.error("Loading expression dictionary failed! for " + attribute.getId());
                    continue;
                }
                LOGGER.debug("expression dictionary loaded successfully for " + attribute.getId());

                // get subarray index from attribute matched to nearest boundary tag position
                int endIndex = findBoundaryPosition(attIndex, boundaryPosition);
                String [] expKeys;
                if(endIndex < 0) {
                    expKeys = ArrayUtils.subArray(sentKeys, 0, sentKeys.length);
                } else {
                    expKeys = ArrayUtils.subArray(sentKeys, 0, endIndex);
                }

                int expOffset;
                boolean modalSkip = false;
                // temporary sentiments container
                ArrayList<Sentiment> tmpSentiments = new ArrayList<Sentiment>();
                for(; expIndex < expKeys.length; expIndex++) {

                    // ignore skip tag
                    if (skipPosition.contains(expIndex)) {
                        continue;
                    }

                    // boundary check
                    if (boundaryPosition.contains(expIndex)) {
                        break;
                    }

                    // modality skip check
                    if (keytermResult[expIndex].getSecond().equals(KeyTerm.QUESTION_TAG)) {
                        modalSkip = true;
                        break;
                    }

                    expOffset = eTrie.prefixRelaxedMatch(expKeys, expIndex, skipPosition);

                    if (expOffset == expIndex) { // prefix matched failed
                        continue;
                    } else {
                        // skip points 만 매치된것은 아닌가 체크해야함.
                        int skipPointMatch = 0;
                        for (int i = expIndex; i < expOffset; i++) {
                            if (skipPosition.contains(i)) {
                                skipPointMatch++;
                            }
                        }
                        if ((expOffset - skipPointMatch) == expIndex) {
                            continue;
                        }

                        // Get Expression object
                        Expression expression = (Expression) eTrie.match(ArrayUtils.subArray(sentKeys, expIndex, expOffset, skipPosition));
                        if (expression == null) {
                            continue;
                        } else { // expression found!
                            Expression newExpression = new Expression();

                            // set attribute offset
                            attribute.setStart(attIndex);
                            attribute.setEnd(attOffset);
                            // set expression offset
                            newExpression.setText(expression.getText());
                            newExpression.setMorphs(expression.getMorphs());
                            newExpression.setPostags(expression.getPostags());
                            newExpression.setRepAttribute(expression.getRepAttribute());
                            newExpression.setId(expression.getId());
                            newExpression.setStart(expIndex);
                            newExpression.setEnd(expOffset);

                            // negation check
                            boolean negated = this.isNegated(expIndex, expOffset, negatedPosition);
                            if (negated) {
                                newExpression.setValue(expression.getValue() * -1);
                                LOGGER.debug("Expression is negated: " + newExpression.toString());
                            } else {
                                newExpression.setValue(expression.getValue());
                            }
                            // add sentiment to the result
                            Sentiment singleSentiment = new Sentiment();
                            singleSentiment.setAttribute(attribute);
                            singleSentiment.setExpression(newExpression);
                            singleSentiment.setSentence(text); // text == sentence
                            singleSentiment.setMorphs(sentToks);
                            singleSentiment.setPostags(sentTags);

                            tmpSentiments.add(singleSentiment);
                            LOGGER.debug("sentiment added: " + attribute.getText() + ", " + newExpression.getText() + "," + newExpression.getValue());
                        }
                    }

                    if (!modalSkip && tmpSentiments.size() > 0) {
                        for (Sentiment s : tmpSentiments) {
                            sentiments.add(s);
                        }
                        tmpSentiments.clear();
                    }
                    expIndex = expOffset - 1;
                }
                // ------------- EXPRESSION SEARCH -------------------//

                // update start index.
                attIndex = attOffset - 1;
                // ----------------------------------------------------------------- //
            }
        }

        // find null-attribute expression
        List<Sentiment> nullResult = this.findNullAttributeSentiment(Pair.getFirsts(keytermResult), Pair.getSeconds(keytermResult),
                skipPosition, boundaryPosition, negatedPosition, degreePosition, text);
        boolean dup;
        if (nullResult != null) {

            for (Sentiment nullSentiment : nullResult) {
                dup = false;
                for (Sentiment normSentiment : sentiments) {
                    if (nullSentiment.getExpression().getStart() == normSentiment.getExpression().getStart() &&
                            nullSentiment.getExpression().getEnd() == normSentiment.getExpression().getEnd()) {
                        dup = true;
                        break;
                    }
                }
                if (!dup) {
                    sentiments.add(nullSentiment);
                }
            }
        }

        return sentiments;
    }

    /**
     * Null-Attribute Sentiment Search
     * @return list of sentiments
     */
    public List<Sentiment> findNullAttributeSentiment(String[] sentToks, String[] sentTags,
                                                      Set<Integer> skipPosition,
                                                      Set<Integer> boundaryPosition,
                                                      Set<Integer> negatedPosition,
                                                      Set<Integer> degreePosition,
                                                      String orgText) {
        // ready...
        ArrayList<Sentiment> result = new ArrayList<Sentiment>();
        Attribute nullAttribute = (Attribute) this.attributeDict.match(SentimentDict.createNullKey());
        if (nullAttribute == null) {
            LOGGER.debug("can't find null-attribute");
            return null;
        }
        // get expression dict. for null-attribute
        TokenTrie expDict = attExpMap.get(nullAttribute.getId());

        // find null-attribute sentiment
        String[] sentKey = SentimentDict.createKey(sentToks, sentTags);
        ArrayList<Sentiment> tmpSentiments = new ArrayList<Sentiment>();
        for (int expIndex = 0; expIndex < sentKey.length; expIndex++) {

            // skip tag 는 패스
            if (skipPosition.contains(expIndex)) {
                continue;
            }

            if (sentTags[expIndex].equals(keyterm.getBoundaryTag())) {
                if (tmpSentiments.size() > 0) {
                    for (Sentiment s : tmpSentiments) {
                        result.add(s);
                    }
                    tmpSentiments.clear();
                }
                continue;
            }

            // modality skip check
            if (sentTags[expIndex].equals(KeyTerm.QUESTION_TAG)) {
                // 현재 담은 결과 들을 없애고, 다음 boundary tag나 sentence end 에서 다시 시작한다.
                tmpSentiments.clear();
                while (!(sentTags[expIndex].equals(keyterm.getBoundaryTag()) || expIndex == sentKey.length - 1)) {
                    expIndex++;
                }
                expIndex = expIndex - 1;
                continue;
            }

            int expOffset = expDict.prefixRelaxedMatch(sentKey, expIndex, skipPosition);
            if (expOffset == expIndex) {
                continue;
            } else {
                // skip points 만 매치된것은 아닌가 체크해야함.
                int skipPointMatch = 0;
                for (int i = expIndex; i < expOffset; i++) {
                    if (skipPosition.contains(i)) {
                        skipPointMatch++;
                    }
                }
                if ((expOffset - skipPointMatch) == expIndex) {
                    continue;
                }

                //get expression object
                Expression expression = (Expression) expDict.RelaxedMatch(sentKey, expIndex, skipPosition);
                if (expression == null) {
                    continue;
                } else {
                    Expression newExpression = new Expression();

                    // set expression offset
                    newExpression.setText(expression.getText());
                    newExpression.setMorphs(expression.getMorphs());
                    newExpression.setPostags(expression.getPostags());
                    newExpression.setRepAttribute(expression.getRepAttribute());
                    newExpression.setId(expression.getId());
                    newExpression.setStart(expIndex);
                    newExpression.setEnd(expOffset);

                    LOGGER.debug("Expression is matched: " + expression.toString());
                    // negation check
                    boolean negated = this.isNegated(expIndex, expOffset, negatedPosition);
                    if (negated) {
                        newExpression.setValue(expression.getValue() * -1);
                        LOGGER.debug("Expression is negated: " + newExpression.toString());
                    } else {
                        newExpression.setValue(expression.getValue());
                    }
                    // add sentiment to the result
                    Sentiment singleSentiment = new Sentiment();
                    singleSentiment.setAttribute(nullAttribute);
                    singleSentiment.setExpression(newExpression);
                    singleSentiment.setSentence(orgText); // text == sentence
                    singleSentiment.setMorphs(sentToks);
                    singleSentiment.setPostags(sentTags);

                    tmpSentiments.add(singleSentiment);
                }
            }
            expIndex = expOffset - 1;
        }
        if (tmpSentiments.size() > 0) {
            for (Sentiment s : tmpSentiments) {
                result.add(s);
            }
        }

        return result;
    }

    /**
     * Find Search Boundary for given attribute poisition
     *
     * @param attPos attribute position
     * @param boundaryPosition all boundary tags
     * @return nearest end boundary position, -1 for end of sentence
     */
    private int findBoundaryPosition(int attPos, Set<Integer> boundaryPosition) {
        for(Integer i : boundaryPosition) {
            if(attPos < i) {
                return i;
            }
        }
        return -1;
    }


    /**
     * Check if current expression is negated or not
     *
     * @param sIdx global start index of current expression
     * @param eIdx global end index of current expression
     * @param negPosition all negated positions
     * @return true if current expression is negated
     */
    private boolean isNegated(int sIdx, int eIdx, Set<Integer> negPosition) {
        for(int i = sIdx; i < eIdx; i++) {
            if(negPosition.contains(i)) {
                return true;
            }
        }
        return false;
    }

}
