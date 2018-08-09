package com.skplanet.nlp.sentiment.knowledge;

import com.skplanet.nlp.sentiment.Prop;
import com.skplanet.nlp.sentiment.analyzer.KeyTerm;
import com.skplanet.nlp.sentiment.ds.Pair;
import com.skplanet.nlp.sentiment.util.HDFSUtil;
import com.skplanet.nlp.trie.TokenTrie;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Collocation class
 *
 * @author Donghun Shin / donghun.shin@sk.com
 *
 */
public final class Collocation {
    private static final Logger LOGGER = Logger.getLogger(Collocation.class.getName());

    private static final String COLLOCATION_DICT_NAME = "collocation.dict";

    // data split tags
    private static final String FIELD_DELIM = "+";
    private static final String VALUE_DELIM = "==>";
    private static final String POS_DELIM = "/";

    // delete operation
    private static final String DELETE = "-";

    // Regular Expression tags
    private static final String SUBSTITUE_TAG = "@";

    // Top priority tags
    private static final String TOP_PRIORITY = "!:";

    //----------------------------------//
    // members
    //----------------------------------//

    private static Collocation instance = null;

    private static HDFSUtil hdfs = null;

    private static TokenTrie dict = null;
    private static TokenTrie topDict = null;

    //----------------------------------//
    // public methods
    //----------------------------------//

    /**
     * Get Collocation Instance
     * @return {@link Collocation} instance
     */
    public static Collocation getInstance() throws IOException {
        if (instance == null) {
            synchronized (Collocation.class) {
                instance = new Collocation();
                LOGGER.debug("[ATTENTION] Collocation instance is created, you must call init() to load collocation dictionary before using it");
            }
        }
        return instance;
    }

    // constructors
    private Collocation() throws IOException {

        // hdfs
        hdfs = HDFSUtil.getInstance();

        // dictionary instance init.
        dict = new TokenTrie();
        topDict = new TokenTrie();

        // get collocation dict info.
        String propStr = hdfs.read(Prop.PROP_PATH);
        InputStream propIS = new ByteArrayInputStream(propStr.getBytes());
        Properties properties = new Properties();
        properties.load(propIS);

        // collocation.dict
        String collocationDictPath = properties.getProperty(Prop.SENT_DICT_SYS_PATH) + "/" + Prop.COL_DICT_PREFIX + Prop.DICT_EXT;
        if (collocationDictPath.contains("\"")) {
            collocationDictPath = collocationDictPath.replace("\"", "");
        }
        String collocationDictStr = hdfs.read(collocationDictPath);
        String[] collocationEntries = collocationDictStr.split("\\n");

        boolean top;

        LOGGER.info("collocation dictionary is loading ....");
        for(String entry : collocationEntries) {
            if (entry.trim().length() == 0 || entry.startsWith("#")) {
                continue;
            }

            // top priority dictionary item?
            if (entry.startsWith(TOP_PRIORITY)) {
                entry = entry.substring(2);
                top = true;
            } else {
                top = false;
            }

            entry = entry.replace(" ", "");

            // field delim : "+"
            // value delim : "==>"
            int offset = entry.indexOf(VALUE_DELIM);
            if (offset < 0) {
                System.err.println("[WARNING] wrong format : " + entry);
                continue;
            }
            String key = entry.substring(0, offset);
            String value = entry.substring(offset + VALUE_DELIM.length());

            String[] keyTokens = tokenizeByDelim(key, FIELD_DELIM);
            String[] valueTokens = tokenizeByDelim(value, FIELD_DELIM);

            if (top) {
                topDict.put(keyTokens, valueTokens);
            } else {
                dict.put(keyTokens, valueTokens);
            }
        }
    }

    /**
     * Collocation Processing (multiple steps)
     *
     * @param input input nlp result
     * @param iter    number of iteration
     * @return collocation processed pair (tokens/tags)
     */
    public Pair[] process(String input, int iter, boolean addEndMark) throws IOException {

        String[] inputToks = input.split(" ");

        //StringBuilder debug = new StringBuilder();

        ArrayList<String> toks = new ArrayList<String>();
        ArrayList<String> tags = new ArrayList<String>();

        for (String token : inputToks) {
            if (token.equals("//sp")) {
                continue;
            }

            String[] fields = token.split("/");
            if (fields.length != 2) {
                LOGGER.warn("nlp result has wrong format: " + token);
                continue;
            }
            toks.add(fields[0]);
            tags.add(fields[1]);
        }

        Pair[] result = null;

        if (tags.size() == 0 || toks.size() == 0) {
            return result;
        }

        // add end mark at the end of terms
        if (addEndMark && !tags.get(tags.size() - 1).equals(KeyTerm.getInstance().getSentenceBoundaryTag())) {
            toks.add(KeyTerm.getInstance().getBoundaryTag());
            tags.add(KeyTerm.getInstance().getBoundaryTag());
        }

        for (int i = 0; i < iter; i++) {
            result = this.process(toks.toArray(new String[toks.size()]), tags.toArray(new String[tags.size()]), true);
            toks.clear();
            tags.clear();
            for (Pair p : result) {
                if (p == null) {
                    break;
                }
                toks.add(p.getFirst());
                tags.add(p.getSecond());
                //debug.append(p.toString()).append(" ");
            }
        }

        for (int i = 0; i < iter; i++) {
            result = this.process(toks.toArray(new String[toks.size()]), tags.toArray(new String[tags.size()]), false);
            toks.clear();
            tags.clear();
            for (Pair p : result) {
                if (p == null) {
                    break;
                }
                toks.add(p.getFirst());
                tags.add(p.getSecond());
            }
        }

        return result;
    }

    /**
     * Collocation processing (single step)
     *
     * @param tokens  tokens
     * @param postags pos tags
     * @return collocation processed pair
     */
    private Pair[] process(String[] tokens, String[] postags, boolean top) {

        String[] query;
        int offset;

        TokenTrie localDict;
        if (top) {
            localDict = topDict;
        } else {
            localDict = dict;
        }

        ArrayList<Pair> result = new ArrayList<Pair>();

        for (int startIndex = 0; startIndex < tokens.length; startIndex++) {

            query = getSubstituteQuery(subArray(tokens, startIndex, tokens.length),
                    subArray(postags, startIndex, postags.length),
                    top);

            offset = localDict.prefixMatch(query);

            if (offset > 0) {
                String[] value = (String[]) localDict.match(subArray(query, 0, offset));

                if (value != null) {
                    String[] sText = getSubstituteTexts(query, startIndex, tokens);
                    Pair[] pair = queryDecompose(value);
                    if (pair == null) {
                        startIndex += offset - 1;
                        continue;
                    }
                    if (sText != null) {
                        pair = getSubstituteTextApply(sText, pair);
                    }
                    Collections.addAll(result, pair);
                } else {
                    Pair p = new Pair(tokens[startIndex], postags[startIndex]);
                    result.add(p);
                    continue;
                }
                startIndex += offset - 1;
            } else {
                Pair p = new Pair(tokens[startIndex], postags[startIndex]);
                if (p.getFirst() != null && p.getSecond() != null) {
                    result.add(p);
                }
            }
        }

        return result.toArray(new Pair[result.size()]);
    }


    //-----------------------------------------------------------
    // private methods
    //-----------------------------------------------------------

    /**
     * Substitute Tag 를 사용하여 매치되는 조합을 모두 탐색하여 원본 tokens 를 대체 한다.
     *
     * @param tokens orginal tokens
     * @param tags   original pos tags
     * @return Substitute Tag 가 적용된 query list (token/tag) 형태
     */
    private String[] getSubstituteQuery(String[] tokens, String[] tags, boolean top) {

        TokenTrie localDict;
        if (top) {
            localDict = topDict;
        } else {
            localDict = dict;
        }
        String[] query;
        int offset;
        int prevOffset = 0;

        for (int sIdx = 0; sIdx < tokens.length; sIdx++) {

            query = mergeTokensAndTags(tokens, tags);
            query = subArray(query, 0, sIdx + 1);
            offset = localDict.prefixMatch(query);
            if (offset > prevOffset) {
                prevOffset = offset;
                continue;
            }

            query = subArray(query, 0, sIdx + 1);
            query = replaceItemInArray(query, SUBSTITUE_TAG, tags[sIdx], sIdx);
            offset = localDict.prefixMatch(query);
            if (offset <= prevOffset) {
                break;
            }
            tokens[sIdx] = SUBSTITUE_TAG;
            prevOffset = offset;
        }

        return mergeTokensAndTags(tokens, tags);
    }

    /**
     * Substitute Tag 로 대체 된 부분을 원래 텍스트 토큰으로 복원 시키는 함수
     *
     * @param sText orginal text
     * @param pair  collocation processed pair ( substitute tag may contained )
     * @return substitute tags are replaced with original text
     */
    private Pair[] getSubstituteTextApply(String[] sText, Pair[] pair) {
        if (pair == null || pair.length == 0) {
            return null;
        }
        int scount = 0;
        int pcount = 0;
        Pair[] result = new Pair[pair.length];
        for (Pair p : pair) {
            if (p.getFirst().startsWith(SUBSTITUE_TAG)) {
                p.setFirst(p.getFirst().replace(SUBSTITUE_TAG, sText[scount++]));
            }
            result[pcount++] = p;
        }

        return result;
    }

    /**
     * Substitute Tag 로 대체 되는 순간에 해당 위치의 텍스트를 리스트로 보관한다. 향후 복원을 위해서 가지고 있는 것임.
     *
     * @param query  사전 탐색을 위해 사용된 query (substitute tag 가 포함 될 수 있다)
     * @param sIdx   query 가 시작되는 global start index ( for tokens[] )
     * @param tokens tokens
     * @return Substitute Tag 로 대체되는 순간에 해당하는 텍스트들의 리스트
     */
    private String[] getSubstituteTexts(String[] query, int sIdx, String[] tokens) {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < query.length; i++) {
            if (query[i].startsWith(SUBSTITUE_TAG)) {
                result.add(tokens[sIdx + i]);
            }
        }

        if (result.size() == 0) {
            return null;
        }
        return result.toArray(new String[result.size()]);
    }

    // ---------------------------------------- //
    // Static Methods
    // ---------------------------------------- //

    /**
     * Simplen Tokenizer
     *
     * @param input string to be tokenized
     * @param delim delimeter
     * @return tokens
     */
    private static String[] tokenizeByDelim(String input, String delim) {
        ArrayList<String> tokens = new ArrayList<String>();
        int offset = input.indexOf(delim);
        if (offset < 0) {
            tokens.add(input);
            return tokens.toArray(new String[tokens.size()]);
        }

        tokens.add(input.substring(0, offset));
        offset += delim.length();

        int sidx = offset;
        while (sidx < input.length()) {
            offset = input.indexOf(delim, sidx);
            if (offset < 0) {
                tokens.add(input.substring(sidx));
                break;
            }
            tokens.add(input.substring(sidx, offset));
            sidx = offset + delim.length();
        }
        return tokens.toArray(new String[tokens.size()]);
    }

    /**
     * Simple Sub Array implementation
     *
     * @param array original array
     * @param sidx  start index
     * @param eidx  end index
     * @return sub array
     */
    private static String[] subArray(String[] array, int sidx, int eidx) {
        List<String> result = new ArrayList<String>();

        for (int i = sidx; i < eidx; i++) {
            if (array[i] != null) {
                result.add(array[i]);
            }
        }

        return result.toArray(new String[result.size()]);
    }

    /**
     * Merge Strings for collocation dictionary format
     *
     * @param a first string
     * @param b second string
     * @return reformatted string for collocation dictionary format
     */
    private static String mergeTokenAndTag(String a, String b) {
        return a + POS_DELIM + b;
    }

    /**
     * Merge String arrays for collocation dictionary format
     *
     * @param tokens  tokens
     * @param postags pos tags
     * @return reformatted for collocation dictionary format
     */
    public static String[] mergeTokensAndTags(String[] tokens, String[] postags) {
        String[] result = new String[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            result[i] = mergeTokenAndTag(tokens[i], postags[i]);
        }
        return result;
    }

    public static String[] mergeTokensAndTags(Pair[] pairs) {
        String[] result = new String[pairs.length];
        int i = 0;
        for (Pair p : pairs) {
            result[i++] = mergeTokenAndTag(p.getFirst(), p.getSecond());
        }
        return result;
    }

    /**
     * Replace Array Item
     *
     * @param array String array
     * @param tok   token to be replaced
     * @param pos   pos to be replaced
     * @param index index
     * @return item replaced array
     */
    private static String[] replaceItemInArray(String[] array, String tok, String pos, int index) {
        try {
            array[index] = tok + POS_DELIM + pos;
        } catch (ArrayIndexOutOfBoundsException e) {
            LOGGER.error("array size: " + array.length + ", index : " + index, e);
        }
        return array;
    }

    /**
     * Query Form Decomposition
     *
     * @param query query to be decomposed
     * @return decomposed pair
     */
    private static Pair[] queryDecompose(String[] query) {
        int addCount = 0;
        Pair[] result = new Pair[query.length];
        for (int i = 0; i < query.length; i++) {
            int offset = query[i].lastIndexOf(POS_DELIM);
            String tok = query[i].substring(0, offset);
            String tag = query[i].substring(offset + 1);
            if (tag.equals(DELETE)) {
                continue;
            }
            addCount++;
            Pair p = new Pair(tok, tag);
            result[i] = p;
        }
        if (addCount == 0) {
            return null;
        }
        return result;
    }
}
