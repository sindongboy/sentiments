package com.skplanet.nlp.sentiment.knowledge;

import com.skplanet.nlp.sentiment.Prop;
import com.skplanet.nlp.sentiment.util.HDFSUtil;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

/**
 * Sentiment Dictionary Loader
 *
 * load all dictionaries
 *
 * @author Donghun Shin / donghun.shin@sk.com
 * @since  3/31/16
 */
public class DictionaryAll {
    private static final Logger LOGGER = Logger.getLogger(DictionaryAll.class.getName());

    private static DictionaryAll instance = null;
    private static Map<String, SentimentDict> sentimentDictMap = null;
    private static Map<String, String> categoryMap = null;

    private HDFSUtil hdfs = null;
    private Properties properties = null;

    /**
     * Get Instance
     *
     * @return instance of Sentiment Dictionary Map
     */
    public static DictionaryAll getInstance() {
        if (instance == null) {
            // method synchronized
            synchronized (DictionaryAll.class) {
                instance = new DictionaryAll();
            }
        }
        return instance;
    }

    // private constructor
    private DictionaryAll() {
        LOGGER.info("Sentiment Dictionary loading ....");

        sentimentDictMap = new HashMap<String, SentimentDict>();

        // --------------------------------------- //
        // get properties
        // --------------------------------------- //
        hdfs = HDFSUtil.getInstance();
        String propPath = hdfs.read(Prop.PROP_PATH);
        InputStream propIS = new ByteArrayInputStream(propPath.getBytes());
        this.properties = new Properties();
        try {
            this.properties.load(propIS);
        } catch (IOException e) {
            LOGGER.error("failed to load properties: " + propPath, e);
        }
        // --------------------------------------- //
        // load category mapping
        // --------------------------------------- //
        categoryMap = new HashMap<String, String>();
        String mapFilePath = properties.getProperty(Prop.CAT_MAP);
        String mapStr = hdfs.read(mapFilePath);
        Scanner scanner = new Scanner(mapStr);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.trim().length() == 0 || line.trim().startsWith("#")) {
                continue;
            }

            String[] fields = line.split("\\t");
            if (fields.length != 5 || fields[0].equals("0")) {
                continue;
            }
            String key = fields[1] + "_" + fields[2] + "_" + fields[3] + "_" + fields[4];
            String val = fields[0];
            categoryMap.put(key, val);

        }
        scanner.close();


        // --------------------------------------- //
        // load sentiment dict
        // --------------------------------------- //
        String dictListPath = this.properties.getProperty(Prop.SENT_DICT_LIST);
        if (dictListPath.contains("\"")) {
            dictListPath = dictListPath.replace("\"", "");
        }
        String dictListStr = hdfs.read(dictListPath);
        scanner = new Scanner(dictListStr);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.trim().length() == 0 || line.trim().startsWith("#")) {
                continue;
            }
            SentimentDict dict = new SentimentDict(line.trim());
            sentimentDictMap.put(line.trim(), dict);
        }
        scanner.close();

        LOGGER.info("Sentiment Dictionary loading done");
    }

    /**
     * Get Sentiment Dictionary
     * @param category category
     * @return {@link SentimentDict}
     */
    public SentimentDict getDictionary(String category) {
        return sentimentDictMap.get(category);
    }

    /**
     * Get OMP category
     * @param cat1 category 1
     * @param cat2 category 2
     * @param cat3 category 3
     * @param cat4 category 4
     * @return omp category if exists otherwise null
     */
    public String getOMPCategory(String cat1, String cat2, String cat3, String cat4) {
        return categoryMap.get(cat1 + "_" + cat2 + "_" + cat3 + "_" + cat4);
    }
    public static void main(String[] args) {
        DictionaryAll dictionaries = DictionaryAll.getInstance();
    }
}
