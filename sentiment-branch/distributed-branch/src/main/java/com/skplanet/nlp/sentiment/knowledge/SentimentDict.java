package com.skplanet.nlp.sentiment.knowledge;

import com.skplanet.nlp.sentiment.Prop;
import com.skplanet.nlp.sentiment.ds.Attribute;
import com.skplanet.nlp.sentiment.ds.Expression;
import com.skplanet.nlp.sentiment.util.HDFSUtil;
import com.skplanet.nlp.trie.TokenTrie;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Sentiment Dictionary handle
 */
public class SentimentDict implements Dictionary {
    private static final Logger LOGGER = Logger.getLogger(SentimentDict.class.getName());


    // ------- Members ------- //

    // env.
    private Properties properties = null;
    private HDFSUtil hdfs = null;

    // dictionary
    private List<String> categoryList = null;
    private String category = null;
    private TokenTrie attributeDict = null;
    private Map<String, Expression> expressionDict = null;
    private Map<String, TokenTrie> attExpMap = null;

    // dictionary info.
    private Set<Attribute> attributeKeySet = null;
    private Set<Expression> expressionKeySet = null;
    private Map<String, List<Expression>> attExpIDMap = null;

    // -------- Methods --------- //



    /**
     * Default Constructor
     */
    public SentimentDict(String category) {
        this.category = category;
        this.categoryList = new ArrayList<String>();
        hdfs = HDFSUtil.getInstance();
        String propPath = hdfs.read(Prop.PROP_PATH);
        InputStream propIS = new ByteArrayInputStream(propPath.getBytes());
        this.properties = new Properties();
        try {
            this.properties.load(propIS);
        } catch (IOException e) {
            LOGGER.error("failed to load properties: " + propPath, e);
        }


        this.attributeKeySet = new HashSet<Attribute>();
        this.expressionKeySet = new HashSet<Expression>();
        this.attExpIDMap = new HashMap<String, List<Expression>>();

        load();
    }

    /**
     * Loading Sentiment Dictionaries
     */
    public void load() {
        // load attributes
        LOGGER.info("attribute dictionary loading (" + category + ") ....");
        this.attributeDict = new TokenTrie();
        this.loadAttributes();
        LOGGER.info("attribute dictionary loading done");

        // load expressions
        LOGGER.info("expression dictionary loading (" + category + ") ....");
        this.expressionDict = new HashMap<String, Expression>();
        this.loadExpressions();
        LOGGER.info("expression dictionary loading done");

        // load att-exp mapping
        LOGGER.info("mapping dictionary loading (" + category + ")....");
        this.attExpMap = new HashMap<String, TokenTrie>();
        this.loadMapping();
        LOGGER.info("mapping dictionary loading done");
    }

    /**
     * Load Attributes
     *
     * attribute dict sample
     * A99	피지	피/vv+지/ec
     * A100	향,향,향기	향/nng,향/nng,향기/nng
     * A101	화학성분	화학/nng+성분/nng
     *
     * @return number of entries
     */
    private int loadAttributes() {
        int entryNum = 0;

        // attribute-${category}.dict
        String attributeDictPath = properties.getProperty(Prop.SENT_DICT_CMP_PATH) + "/" +
                Prop.ATT_DICT_PREFIX +
                this.category +
                Prop.DICT_EXT;
        if (attributeDictPath.contains("\"")) {
            attributeDictPath = attributeDictPath.replace("\"", "");
        }

        String[] attributeEntries;
        attributeEntries = hdfs.read(attributeDictPath).split("\\n");

        for (String att : attributeEntries) {
            if (att.trim().length() == 0 || att.startsWith("#")) {
                continue;
            }

            String[] fields = att.split("\t");

            if (fields.length != 3) {
                LOGGER.error("wrong entry: " + att);
                continue;
            }

            String id = fields[0];
            String[] attTerms = fields[1].split(",");
            String[] nlpTerms = fields[2].split("\\001");


            // add them to the dictionary
            for (int i = 0; i < nlpTerms.length; i++) {
                //att = att.toLowerCase();

                //---------- Collocations ------------//
                //Pair[] tmpColloc = collocation.process(this.nlp.getMorphs(att), this.nlp.getPOSTags(att), 3, false);
                // create attributes
                Attribute attribute = new Attribute();
                attribute.setId(id);
                attribute.setText(attTerms[i]);
                attribute.setMorphs(nlpTerms[i]);
                attribute.setPostags(nlpTerms[i]);
                attribute.setSyns(attTerms);

                String[] key = createKey(attribute.getMorphs(), attribute.getPostags());
                this.attributeDict.put(key, attribute);
                this.attributeKeySet.add(attribute);
                entryNum++;
            }
        }
        return entryNum;
    }

    /**
     * Load Expression
     *
     * expression dict sample
     * E627	느리다	느리/va+./.	-1
     * E628	빠르다	빠르/va+./.	1
     * E629	아쉽다	아쉽/va+./.	-1
     *
     * @return number of entries
     */
    private int loadExpressions() {
        int entryNum = 0;

        // expression-${category}.dict
        String expressionDictPath = properties.getProperty(Prop.SENT_DICT_CMP_PATH) + "/" +
                Prop.EXP_DICT_PREFIX +
                this.category +
                Prop.DICT_EXT;

        if (expressionDictPath.contains("\"")) {
            expressionDictPath = expressionDictPath.replace("\"", "");
        }

        String[] expressionEntries;
        expressionEntries = hdfs.read(expressionDictPath).split("\\n");

        for (String exp : expressionEntries) {
            if(exp.trim().length() == 0 || exp.startsWith("#")) {
                continue;
            }
            String [] fields = exp.split("\t");
            if(fields.length != 4) {
                LOGGER.error("wrong entry: " + exp);
                continue;
            }

            String id = fields[0];
            String text = fields[1].toLowerCase();
            int value = Integer.parseInt(fields[3]);

            //---------- Collocations ------------//

            Expression expression = new Expression();
            expression.setId(id);
            expression.setText(text);
            expression.setMorphs(fields[2]);
            expression.setPostags(fields[2]);
            expression.setValue(value);

            this.expressionDict.put(id, expression);
            this.expressionKeySet.add(expression);
            entryNum++;
        }

        return entryNum;
    }

    /**
     * Load Mapping Dictionary
     */
    private void loadMapping() {
        // att-exp-${category}.dict
        String mapDictPath = properties.getProperty(Prop.SENT_DICT_CMP_PATH) + "/" +
                Prop.MAP_DICT_PREFIX +
                this.category +
                Prop.DICT_EXT;
        if (mapDictPath.contains("\"")) {
            mapDictPath = mapDictPath.replace("\"", "");
        }
        String[] mapEntries;
        mapEntries = hdfs.read(mapDictPath).split("\\n");

        for (String map : mapEntries) {
            if (map.trim().length() == 0 || map.startsWith("#")) {
                continue;
            }
            String[] fields = map.split("\t");
            if (fields.length != 2) {
                LOGGER.error("wrong entry: " + map);
                continue;
            }

            // attribute id 를 키로 함.
            String id = fields[0];
            // expression ids 를 엔트리로 함.
            String[] expList = fields[1].split(",");

            // 현재 attribute id 에 대한 TokenTrie 사전을 생성한다.
            TokenTrie aTrie = new TokenTrie();


            // expression id를 돌면서 각각을 TokenTrie 에 넣어 사전을 만듬.
            for (String eId : expList) {
                String[] efield = eId.split("_");
                if (efield.length != 2) {
                    LOGGER.error("no rep. attribute assigned : " + map);
                    continue;
                }
                //efield[0] ==> eID
                //efield[1] ==> Rep. Attribute

                // expression dict 에서 해당 expression을 가져온다.
                Expression exp = this.expressionDict.get(efield[0]);
                if (exp == null) {
                    continue;
                }
                if (this.attExpIDMap.containsKey(id)) {
                    this.attExpIDMap.get(id).add(exp);
                } else {
                    ArrayList<Expression> eList = new ArrayList<Expression>();
                    eList.add(exp);
                    this.attExpIDMap.put(id, eList);
                }
                // 해당 expression 에 대한 대표 속성을 추가한다.
                exp.setRepAttribute(efield[1]);
                // 해당 expression 에 대한 key를 생성 한다.
                String[] eKey = createKey(exp.getMorphs(), exp.getPostags());

                // 현재 TokenTrie 에 해당 expression을 put 한다.
                aTrie.put(eKey, exp);
            }
            // 모든 expression 이 TokenTrie 사전에 추가 되면 해당 사전을 Mapping 사전에 추가한다.
            this.attExpMap.put(id, aTrie);
        }
    }


    /**
     * Get Current Category ID
     *
     * @return category id
     */
    public String getCategoryID() {
        if (this.category == null) {
            return null;
        }
        return this.category;
    }

    /**
     * Get Attributes in the dictionary
     *
     * @return {@link Attribute} list
     */
    public Set<Attribute> getAttributes() {
        return this.attributeKeySet;
    }

    /**
     * Create a Key for {@link TokenTrie}
     *
     * @param toks token list
     * @param tags pos-tag list
     * @return token + pos-tag pair list
     */
    public static String[] createKey(String [] toks, String [] tags) {
        String [] result = new String[toks.length];
        for(int i = 0; i < toks.length; i++) {
            result[i] = toks[i] + "^" + tags[i];
        }
        return result;
    }

    /**
     * Create a Key for {@link TokenTrie}
     *
     * @return null key
     */
    public static String[] createNullKey() {
        String [] result = new String[1];
        result[0] = "null^eng";
        return result;
    }

    /**
     * Get Attribute Dict
     * @return attribute dict
     */
    public TokenTrie getAttributeDict() {
        return this.attributeDict;
    }

    /**
     * Get Expression Dict
     * @return expression dict
     */
    public Map<String, Expression> getExpressionDict() {
        return this.expressionDict;
    }

    /**
     * Get Attribute-Expression Map
     * @return attribute expression map
     */
    public Map<String, TokenTrie> getAttExpMap() {
        return this.attExpMap;
    }

    /**
     * Get Attribute - Expression ID Mapping
     * @return attribute expression id mapping
     */
    public Map<String, List<Expression>> getAttExpIDMap() {
        return this.attExpIDMap;
    }
    /**
     * Test Dictionary Loading
     * @param args no args
     */
    public static void main(String[] args) {

    }
}
