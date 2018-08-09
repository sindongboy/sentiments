package com.skplanet.nlp;

/**
 * Static Properties
 *
 * @author Donghun Shin / donghun.shin@sk.com
 * @date 3/7/16
 */
public class Prop {

    public static final String HADOOP_CONF_PATH = "/app/hadoop/conf";
    //public static final String HIVE_CONF_PATH = "/app/hive/conf";
    public static final String PROP_PATH = "/user/tas/product/config/sentiment/sentiment-analyzer.properties";

    // ------------------------------------ //
    // Path and Files
    // ------------------------------------ //
    /** NLPAPI Properties */
    public static final String NLP_PROP = "NLP_PROP";
    public static final String SENT_DICT_LIST = "SENT_DICT_LIST";

    // ------------------------------------ //
    // HIVE
    // ------------------------------------ //
    //public static final String HIVE_DB_NM = "svc_tas";
    //public static final String HIVE_TB_REVIEW_NLP = "tb_review_nlp";
    //public static final String HIVE_TB_REVIEW_KEYWORDS = "tb_review_keywords";

    // ------------------------------------ //
    // Path and Files
    // ------------------------------------ //

    /** Input Path */
    //private static final String INPUT_BASE = "/user/hive/warehouse/svc_tas.db/tb_review_nlp/p_yyyymmdd=";
    public static final String INPUT_BASE = "INPUT_BASE";
    /** Output Path */
    //private static final String OUTPUT_BASE = "/user/tas/product/tmp/sentiment";
    public static final String OUTPUT_BASE = "OUTPUT_BASE";

    /** Sentiment Dict */
    public static final String SENT_DICT_CMP_PATH = "SENT_DICT_CMP_PATH";
    /** System Dict */
    public static final String SENT_DICT_SYS_PATH = "SENT_DICT_SYS_PATH";

    /** Dictionary Name */
    public static final String ATT_DICT_PREFIX = "attribute-";
    public static final String EXP_DICT_PREFIX = "expression-";
    public static final String MAP_DICT_PREFIX = "att-exp-";
    public static final String COL_DICT_PREFIX = "collocation";
    public static final String DICT_EXT = ".dict";

    // ------------------------------------ //
    // Keyterms related static properties   //
    // ------------------------------------ //
    /** Boundary Tag */
    public static final String BOUNDARY_TAG = "BOUNDARY_TAG";
    /** Sentence End Tag */
    public static final String SENTENCE_END_TAG = "SENTENCE_END_TAG";
    public static final String NEGATION_TAG = "NEGATION_TAG";
    public static final String FRONT_NEGATION_LEXS = "FRONT_NEGATION_LEXS";
    public static final String KEYTERM_POSTAG = "KEYTERM_POSTAG";

    // ------------------------------------ //
    // dictionary related static properties   //
    // ------------------------------------ //



}
