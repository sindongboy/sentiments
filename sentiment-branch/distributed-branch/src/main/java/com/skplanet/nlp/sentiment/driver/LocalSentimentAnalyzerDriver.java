package com.skplanet.nlp.sentiment.driver;

import com.skplanet.nlp.sentiment.analyzer.SentimentAnalyzer;
import com.skplanet.nlp.sentiment.ds.Sentiment;
import com.skplanet.nlp.sentiment.knowledge.DictionaryAll;
import com.skplanet.nlp.sentiment.knowledge.SentimentDict;
import com.skplanet.nlp.sentiment.util.HDFSUtil;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * LOCAL TEST ONLY
 *
 * @author Donghun Shin / donghun.shin@sk.com
 * @date 4/1/16
 */
public class LocalSentimentAnalyzerDriver {
    private static final Logger LOGGER = Logger.getLogger(LocalSentimentAnalyzerDriver.class.getName());


    public static void main(String[] args) throws IOException {
        HDFSUtil hdfs = HDFSUtil.getInstance();

        File reviews = new File(args[0]);

        DictionaryAll dicts = DictionaryAll.getInstance();
        SentimentDict dict = dicts.getDictionary("347");

        SentimentAnalyzer analyzer = new SentimentAnalyzer(dict);

        BufferedReader reader = new BufferedReader(new FileReader(reviews));
        String line;
        while ((line = reader.readLine()) != null) {

            if (line.trim().length() == 0) {
                continue;
            }

            String[] fields = line.trim().split("\\t");
            String sentence = fields[3];

            List<Sentiment> result = analyzer.find(sentence);
            if (result.size() > 0) {
                for (Sentiment s : result) {
                    if (s.toString() == null) {
                        continue;
                    }
                    System.out.println(fields[1] + "\t" +
                            s.getAttribute().getText() + "\t" +
                                    s.getAttribute().getId() + "\t" +
                                    s.getExpression().getText() + "\t" +
                                    s.getExpression().getId() + "\t" +
                                    s.getExpression().getValue()
                    );
                }
            }
        }

        reader.close();
    }
}
