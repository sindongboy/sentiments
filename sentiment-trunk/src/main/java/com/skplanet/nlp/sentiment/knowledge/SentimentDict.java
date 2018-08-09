package com.skplanet.nlp.sentiment.knowledge;

import com.skplanet.nlp.config.Configuration;
import com.skplanet.nlp.sentiment.analyzer.KeyTerm;
import com.skplanet.nlp.sentiment.ds.Attribute;
import com.skplanet.nlp.sentiment.ds.Expression;
import com.skplanet.nlp.sentiment.ds.Pair;
import com.skplanet.nlp.sentiment.util.NLPUtils;
import com.skplanet.nlp.trie.TokenTrie;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Sentiment Dictionary handle
 */
public class SentimentDict implements Dictionary {
    private static final Logger LOGGER = Logger.getLogger(SentimentDict.class.getName());

    private static final String ATT_DIC_PREFIX = "attribute-";
    private static final String EXP_DIC_PREFIX = "expression-";
    private static final String MAP_DIC_PREFIX = "att-exp-";
    private static final String DIC_SUFFIX = ".dict";
    private static final String ACTOR_LIST_FILE = "actor.list";
    private static final String DIRECTOR_LIST_FILE = "director.list";
    private static final String EXCEPTION_LIST_FILE = "exception.list";

    // ------- Members ------- //
    // sentiment analyzer configuration

    // env.
    private File expressionDictFile = null;
    private File attributeDictFile = null;
    private File attExpMapFile = null;
    private static File actorListFile = null;
    private static File directorListFile = null;
    private static File exceptionListFile = null;
    private NLPUtils nlp = null;
    private Collocation collocation = Collocation.getInstance();
    private KeyTerm keyterm = null;

    // dictionary
    private TokenTrie attributeDict = null;
    private Map<String, Expression> expressionDict = null;
    private Map<String, TokenTrie> attExpMap = null;

    // dictionary info.
    private String categoryID = null;
    private Set<Attribute> attributeKeySet = null;
    private Set<Expression> expressionKeySet = null;
    private Map<String, List<Expression>> attExpIDMap = null;
    private static Set<String> actorSet = new HashSet<String>();
    private static Set<String> directorSet = new HashSet<String>();
    private static List<Pair> exceptionList = new ArrayList<Pair>();

    // -------- Methods --------- //
    /**
     * Default Cosntructor
     */
    public SentimentDict(NLPUtils nlp) {
        this.nlp = nlp;
        this.attributeKeySet = new HashSet<Attribute>();
        this.expressionKeySet = new HashSet<Expression>();
        this.attExpIDMap = new HashMap<String, List<Expression>>();
        this.collocation.init();
        this.keyterm = KeyTerm.getInstance();
    }

    /**
     * Loading Sentiment Dictionaries
     */
    public void load(String category) {
        this.categoryID = category;
        // set dictionary files
        this.setDictionaryFiles(category);
        LOGGER.info("dictionary files are all ready to be read for category: " + category);

        // load attributes
        LOGGER.info("attribute dictionary loading ....");
        this.attributeDict = new TokenTrie();
        this.loadAttributes();
        LOGGER.info("attribute dictionary loading done");

        // load expressions
        LOGGER.info("expression dictionary loading ....");
        this.expressionDict = new HashMap<String, Expression>();
        this.loadExpressions();
        LOGGER.info("expression dictionary loading done");

        // load att-exp mapping
        LOGGER.info("mapping dictionary loading ....");
        this.attExpMap = new HashMap<String, TokenTrie>();
        this.loadMapping();
        LOGGER.info("mapping dictionary loading done");

        // 영화 카테고리에 대해서만 exception, actor 와 director 정보를 가져간다.
        // temporary (preferential package 로 분리 예정)
        if (category.equals("2942")) {
            loadExceptionList();
            loadActorList();
            loadDirectorList();
        }
    }

    /**
     * load Actor list
     * @return total count
     */
    private static int loadActorList() {
        String line;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(actorListFile));
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                actorSet.add(line.trim());
            }
            reader.close();
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
        } catch (IOException e) {
            LOGGER.error(e);
        }
        return actorSet.size();
    }

    /**
     * load sentiment exception
     * @return total count
     */
    private static int loadExceptionList() {
        String line;
        BufferedReader reader;
        exceptionList = new ArrayList<Pair>();
        try {
            reader = new BufferedReader(new FileReader(exceptionListFile));
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                //actorSet.add(line.trim());
                String[] fields = line.split("\\t");
                Pair pair = new Pair(fields[0], fields[1]);
                exceptionList.add(pair);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
        } catch (IOException e) {
            LOGGER.error(e);
        }
        return exceptionList.size();
    }

    /**
     * load director list
     * @return total count
     */
    private static int loadDirectorList() {
        String line;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(directorListFile));
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                directorSet.add(line.trim());
            }
            reader.close();
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
        } catch (IOException e) {
            LOGGER.error(e);
        }
        return directorSet.size();
    }

    public static boolean isActor(String actor) {
        return actorSet.contains(actor);
    }

    public static boolean isDirector(String director) {
        return directorSet.contains(director);
    }

    public static boolean isException(String att, String exp) {
        for (Pair p : exceptionList) {
            String attribute = p.getFirst().replace(" ", "").trim();
            String expression = p.getSecond().replace(" ", "").trim();

            if (attribute.equals(att) && expression.equals(exp)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Load Attributes
     *
     * @return number of entries
     */
    private int loadAttributes() {
        int entryNum = 0;

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(this.attributeDictFile));
            String line;
            while((line = reader.readLine())!=null) {
                if(line.trim().length() == 0 || line.startsWith("#")) {
                    continue;
                }
                String [] fields = line.split("\t");
                if(fields.length != 2) {
                    LOGGER.error("wrong entry: " + line);
                    continue;
                }
                String[] attTerms = fields[1].split(",");
                String id = fields[0];


                // add them to the dictionary
                for(String att : attTerms) {
                    att = att.toLowerCase();

                    //---------- Collocations ------------//
                    Pair[] tmpColloc = collocation.process(this.nlp.getMorphs(att), this.nlp.getPOSTags(att), 3, false);
                    // create attributes
                    Attribute attribute = new Attribute();
                    attribute.setId(id);
                    attribute.setText(att);
                    attribute.setMorphs(Pair.getFirsts(tmpColloc));
                    attribute.setPostags(Pair.getSeconds(tmpColloc));
                    attribute.setSyns(attTerms);

                    String[] key = createKey(attribute.getMorphs(), attribute.getPostags());
                    this.attributeDict.put(key, attribute);
                    this.attributeKeySet.add(attribute);
                    entryNum++;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            LOGGER.error("Attribute Dictionary doesn't exist: " + this.categoryID, e);
        } catch (IOException e) {
            LOGGER.error("Failed to read Attribute dictionary: " + this.attributeDictFile.getName(), e);
        }
        return entryNum;
    }

    /**
     * Load Expression
     *
     * @return number of entries
     */
    private int loadExpressions() {
        int entryNum = 0;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(this.expressionDictFile));
            String line;
            while((line = reader.readLine())!=null) {
                if(line.trim().length() == 0 || line.startsWith("#")) {
                    continue;
                }
                String [] fields = line.split("\t");
                if(fields.length != 3) {
                    LOGGER.error("wrong entry: " + line);
                    continue;
                }

                String id = fields[0];
                String text = fields[1].toLowerCase();
                int value = Integer.parseInt(fields[2]);

                //---------- Collocations ------------//
                Pair[] tmpColloc = collocation.process(this.nlp.getMorphs(text), this.nlp.getPOSTags(text), 3, false);

                tmpColloc = keyterm.process(Pair.getFirsts(tmpColloc), Pair.getSeconds(tmpColloc));
                ArrayList<Pair> finalColloc = new ArrayList<Pair>();
                for (Pair p : tmpColloc) {
                    if (!p.getSecond().equals(KeyTerm.SKIP_TAG)) {
                        finalColloc.add(p);
                    }
                }

                Expression expression = new Expression();
                expression.setId(id);
                expression.setText(text);
                expression.setMorphs(Pair.getFirsts(finalColloc.toArray(new Pair[finalColloc.size()])));
                expression.setPostags(Pair.getSeconds(finalColloc.toArray(new Pair[finalColloc.size()])));
                expression.setValue(value);

                this.expressionDict.put(id, expression);
                this.expressionKeySet.add(expression);
                entryNum++;
            }
            reader.close();
        } catch (FileNotFoundException e) {
            LOGGER.error("Expression Dictionary doesn't exist: " + this.categoryID, e);
        } catch (IOException e) {
            LOGGER.error("Failed to read Expression dictionary: " + this.expressionDictFile.getName(), e);
        }

        return entryNum;
    }

    private void loadMapping() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(this.attExpMapFile));
            String line;
            while((line = reader.readLine())!=null) {
                if(line.trim().length() == 0 || line.startsWith("#")) {
                    continue;
                }
                String [] fields = line.split("\t");
                if(fields.length != 2) {
                    LOGGER.error("wrong entry: " + line);
                    continue;
                }

                // attribute id 를 키로 함.
                String id = fields[0];
                // expression ids 를 엔트리로 함.
                String [] expList = fields[1].split(",");

                // 현재 attribute id 에 대한 TokenTrie 사전을 생성한다.
                TokenTrie aTrie = new TokenTrie();


                // expression id를 돌면서 각각을 TokenTrie 에 넣어 사전을 만듬.
                for(String eId : expList) {
                    String [] efield = eId.split("_");
                    if(efield.length != 2) {
                        LOGGER.error("no rep. attribute assigned : " + line);
                        continue;
                    }
                    //efield[0] ==> eID
                    //efield[1] ==> Rep. Attribute

                    // expression dict 에서 해당 expression을 가져온다.
                    Expression exp = this.expressionDict.get(efield[0]);
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
            reader.close();
        } catch (FileNotFoundException e) {
            LOGGER.error("Mapping Dictionary doesn't exist: " + this.categoryID, e);
        } catch (IOException e) {
            LOGGER.error("Failed to read Mapping dictionary: " + this.attExpMapFile.getName(), e);
        }

    }


    /**
     * Get Current Category ID
     *
     * @return category id
     */
    public String getCategoryID() {
        if (this.categoryID == null) {
            return null;
        }
        return this.categoryID;
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
     * Get Expression in the dictionary
     *
     * @return {@link Expression} list
     */
    public Set<Expression> getExpressions() {
        return this.expressionKeySet;
    }

    public List<Expression> getExpressions(String att) {
        // ready...
        String[] nToks = nlp.getMorphs(att);
        String[] nTags = nlp.getPOSTags(att);
        Pair[] tmpColloc = collocation.process(nToks, nTags, 3, false);
        Attribute nullAttribute = (Attribute) this.attributeDict.match(createKey(Pair.getFirsts(tmpColloc), Pair.getSeconds(tmpColloc)));
        if (nullAttribute == null) {
            LOGGER.debug("can't find attribute: " + att);
            return null;
        }
        // get expression dict. for null-attribute
        return attExpIDMap.get(nullAttribute.getId());
    }

    /**
     * Set the dictioinary files
     *
     * @param category category ID for the dictionaries
     */
    private void setDictionaryFiles(String category) {
        Configuration config = Configuration.getInstance();
        this.attributeDictFile = new File(config.getResource(ATT_DIC_PREFIX + category + DIC_SUFFIX, Configuration.CLASSPATH_LOAD).getFile());
        this.expressionDictFile = new File(config.getResource(EXP_DIC_PREFIX + category + DIC_SUFFIX, Configuration.CLASSPATH_LOAD).getFile());
        this.attExpMapFile = new File(config.getResource(MAP_DIC_PREFIX + category + DIC_SUFFIX, Configuration.CLASSPATH_LOAD).getFile());
        if (category.equals("2942")) {
            this.actorListFile = new File(config.getResource(ACTOR_LIST_FILE, Configuration.CLASSPATH_LOAD).getFile());
            this.directorListFile = new File(config.getResource(DIRECTOR_LIST_FILE, Configuration.CLASSPATH_LOAD).getFile());
            this.exceptionListFile = new File(config.getResource(EXCEPTION_LIST_FILE, Configuration.CLASSPATH_LOAD).getFile());
        }

        if (!(this.attributeDictFile.exists() && this.expressionDictFile.exists() && this.attExpMapFile.exists())) {
            LOGGER.error("one of the dictionary files doesn't exist: " + category);
            LOGGER.info("Sentiment Analyzer terminated : failed to load dictionary");
            System.exit(1);
        }
    }


    /**
     * Get remove of negation words from keyterms
     *
     * @param orgToks original tokens
     * @param orgTags original postags
     * @return token array negation word removed
     */
    /*
    private String [] getNoNegTokens(String [] orgToks, String[] orgTags) {
        ArrayList<String> result = new ArrayList<String>();
        for(int i = 0; i < orgToks.length; i++) {
            //if(!orgTags[i].equals(KeyTerm.NEGATION_TAG)) {
            if(!keyterm.isNegationTag(orgTags[i])) {
                result.add(orgToks[i]);
            }
        }
        return result.toArray(new String[result.size()]);
    }
    */

    /**
     * Get remove of negation words of keyterms
     *
     * @param orgToks original tokens
     * @param orgTags original postags
     * @return postag array negation word removed
     */
    /*
    private String [] getNoNegTags(String [] orgToks, String[] orgTags) {
        ArrayList<String> result = new ArrayList<String>();
        for(int i = 0; i < orgToks.length; i++) {
            if(!keyterm.isNegationTag(orgTags[i])) {
                result.add(orgTags[i]);
            }
        }
        return result.toArray(new String[result.size()]);
    }
    */

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
}
