package com.skplanet.nlp.sentiment.reducer;

import com.skplanet.nlp.sentiment.ds.SentimentWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Donghun Shin / donghun.shin@sk.com
 * @date 4/5/16
 */
public class ParallelSentimentAnalyzerReducer extends Reducer<Text, SentimentWritable, WritableComparable, Text> {
    private static final Logger LOGGER = Logger.getLogger(ParallelSentimentAnalyzerReducer.class.getName());

        @Override
        protected void reduce(Text key, Iterable<SentimentWritable> values, Context context) throws IOException, InterruptedException {

            // iterate over nlp result
            Iterator<SentimentWritable> iter = values.iterator();
            while (iter.hasNext()) {
                StringBuffer result = new StringBuffer();
                SentimentWritable value = iter.next();

                String productId = key.toString();
                Text contentId = value.getContentIdText();
                Text cat1 = value.getCat1Text();
                Text cat2 = value.getCat2Text();
                Text cat3 = value.getCat3Text();
                Text cat4 = value.getCat4Text();
                Text sentenceId = value.getSentenceIdText();
                Text attributeId = value.getAttributeIdText();
                Text attributeText = value.getAttributeText();
                Text expressionId = value.getExpressionIdText();
                Text expressionText = value.getExpressionText();
                Text repAttributeText = value.getRepAttributeText();
                IntWritable polarity = value.getPolarityIntWritable();

                result.append(productId).append('\001')
                        .append(contentId).append('\001')
                        .append(cat1).append('\001')
                        .append(cat2).append('\001')
                        .append(cat3).append('\001')
                        .append(cat4).append('\001')
                        .append(sentenceId).append('\001')
                        .append(attributeId).append('\001')
                        .append(attributeText).append('\001')
                        .append(expressionId).append('\001')
                        .append(expressionText).append('\001')
                        .append(repAttributeText).append('\001')
                        .append(polarity.get());

                context.write(null, new Text(result.toString()));
            }
        }
}
