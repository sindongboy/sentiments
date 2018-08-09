package com.skplanet.nlp.sentiment.driver;

import com.skplanet.nlp.sentiment.Prop;
import com.skplanet.nlp.sentiment.ds.Expression;
import com.skplanet.nlp.sentiment.knowledge.SentimentDict;
import com.skplanet.nlp.sentiment.util.HDFSUtil;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Donghun Shin / donghun.shin@sk.com
 * @since  5/10/16
 */
public class RepAttributeMappingDriver {
    private static final Logger LOGGER = Logger.getLogger(RepAttributeMappingDriver.class.getName());

    static HDFSUtil hdfs = HDFSUtil.getInstance();

    public static void main(String[] args) {
        String partition = args[0];

        Properties properties;

        hdfs = HDFSUtil.getInstance();
        String propPath = hdfs.read(Prop.PROP_PATH);
        //LOGGER.info("properties path: " + propPath);
        InputStream propIS = new ByteArrayInputStream(propPath.getBytes());
        properties = new Properties();
        try {
            properties.load(propIS);
        } catch (IOException e) {
            LOGGER.error("failed to load properties: " + propPath, e);
        }


        String dictListPath = properties.getProperty(Prop.SENT_DICT_LIST);
        if (dictListPath.contains("\"")) {
            dictListPath = dictListPath.replace("\"", "");
        }
        String dictListStr = hdfs.read(dictListPath);
        Scanner scanner = new Scanner(dictListStr);
        StringBuffer sb = new StringBuffer();
        while (scanner.hasNextLine()) {
            String category = scanner.nextLine();
            if (category.trim().length() == 0 || category.trim().startsWith("#")) {
                continue;
            }

            LOGGER.info("dictionary processing: " + category.trim());
            SentimentDict dict = new SentimentDict(category.trim());

            Map<String, List<Expression>> idMapping = dict.getAttExpIDMap();
            Set<String> keySet = idMapping.keySet();
            for (String attId : keySet) {
                List<Expression> expressionList = idMapping.get(attId);

                for (int i = 0; i < expressionList.size(); i++) {
                    // category
                    sb.append(category).append('\001');
                    // attribute id
                    sb.append(attId).append('\001');
                    // expression id
                    sb.append(expressionList.get(i).getId()).append('\001');
                    // rep. attribute
                    sb.append(expressionList.get(i).getRepAttribute()).append('\001');
                    // u_dt
                    sb.append(partition).append('\n');
                }
            }

        }
        scanner.close();

        hdfs.delete(properties.getProperty(Prop.REP_DICT_TMP) + partition);
        hdfs.write(properties.getProperty(Prop.REP_DICT_TMP) + partition, sb.toString().trim());
    }
}
