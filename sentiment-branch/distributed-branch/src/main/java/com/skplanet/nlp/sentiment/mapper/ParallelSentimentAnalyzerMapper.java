package com.skplanet.nlp.sentiment.mapper;

import com.skplanet.nlp.sentiment.analyzer.SentimentAnalyzer;
import com.skplanet.nlp.sentiment.ds.Sentiment;
import com.skplanet.nlp.sentiment.ds.SentimentWritable;
import com.skplanet.nlp.sentiment.knowledge.DictionaryAll;
import com.skplanet.nlp.sentiment.knowledge.SentimentDict;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * @author Donghun Shin / donghun.shin@sk.com
 * @date 4/5/16
 */
public class ParallelSentimentAnalyzerMapper extends Mapper<WritableComparable, Text, Text, SentimentWritable> {
    private static final Logger LOGGER = Logger.getLogger(ParallelSentimentAnalyzerMapper.class.getName());

    /** key information */
    String productId;
    String contentId;

    private static final int FIELD_NUM = 11;

    /** value information */
    private SentimentWritable sentiment = new SentimentWritable();
    private Text contentIdText = new Text();
    private Text cat1Text = new Text();
    private Text cat2Text = new Text();
    private Text cat3Text = new Text();
    private Text cat4Text = new Text();
    private Text sentenceIdText = new Text();
    private Text attributeText = new Text();
    private Text attributeIdText = new Text();
    private Text expressionText = new Text();
    private Text expressionIdText = new Text();
    private Text repAttributeText = new Text();
    private IntWritable polarityIntWritable = new IntWritable();

    /** sentiment dictionaries */
    private DictionaryAll sentimentDicts = DictionaryAll.getInstance();

    @Override
    protected void map(WritableComparable key, Text value, Context context) throws IOException, InterruptedException {

        // index 8 --> category path added ( 대카^중카^소카^세카 )
        String[] fields = value.toString().split("\\001", FIELD_NUM);

        SentimentWritable dummyResult = new SentimentWritable();

        // ------------------------ //
        // make a dummy result
        // content id
        dummyResult.setContentIdText(new Text("N/A"));
        // category
        dummyResult.setCat1Text(new Text("N/A"));
        dummyResult.setCat2Text(new Text("N/A"));
        dummyResult.setCat3Text(new Text("N/A"));
        dummyResult.setCat4Text(new Text("N/A"));
        // sentence id
        dummyResult.setSentenceIdText(new Text("N/A")); // attributes
        dummyResult.setAttributeIdText(new Text("N/A"));
        dummyResult.setAttributeText(new Text("N/A"));
        // expression
        dummyResult.setExpressionIdText(new Text("N/A"));
        dummyResult.setExpressionText(new Text("N/A"));
        // rep. attribute
        dummyResult.setRepAttributeText(new Text("N/A"));

        // value
        dummyResult.setPolarityIntWritable(new IntWritable(0));
        // ------------------------ //

        if (fields.length == FIELD_NUM) {

            // ---------------- //
            // key information
            // ---------------- //
            productId = fields[0];
            contentId = fields[1];
            String sentenceId = fields[2];

            // ---------------- //
            // category info.
            // ---------------- //
            // 대카테고리
            String category1 = fields[4];
            this.cat1Text.set(category1);
            // 중카테고리
            String category2 = fields[5];
            this.cat2Text.set(category2);
            // 소카테고리
            String category3 = fields[6];
            this.cat3Text.set(category3);
            // 세카테고리
            String category4 = fields[7];
            this.cat4Text.set(category4);

            // get omp category
            String ompCategory = sentimentDicts.getOMPCategory(category1, category2, category3, category4);
            // 카테고리 매핑 정보가 없다면, 일반 사전으로 매핑 한다 (1_sheets)
            if (ompCategory == null) {
                ompCategory = "1";
            }

            // dictionary mapping
            SentimentDict sentimentDict = sentimentDicts.getDictionary(ompCategory);
            if (sentimentDict == null) {
                LOGGER.error("can't get the sentiment dictionary for category " + ompCategory);
            }
            SentimentAnalyzer analyzer = new SentimentAnalyzer(sentimentDict);

            // ------------------- //
            // sentiment analysis
            // ------------------- //
            String sentence = fields[8];
            List<Sentiment> result = analyzer.find(sentence);

            if (result.size() > 0) {
                for (Sentiment s : result) {

                    this.contentIdText.set(contentId);
                    this.sentenceIdText.set(sentenceId);
                    this.attributeText.set(s.getAttribute().getText());
                    this.attributeIdText.set(s.getAttribute().getId());
                    this.expressionText.set(s.getExpression().getText());
                    this.expressionIdText.set(s.getExpression().getId());
                    this.polarityIntWritable.set(s.getExpression().getValue());
                    this.repAttributeText.set(s.getExpression().getRepAttribute());

                    // write output
                    this.sentiment.set(
                            this.contentIdText,
                            this.cat1Text,
                            this.cat2Text,
                            this.cat3Text,
                            this.cat4Text,
                            this.sentenceIdText,
                            this.attributeText,
                            this.attributeIdText,
                            this.expressionText,
                            this.expressionIdText,
                            this.repAttributeText,
                            this.polarityIntWritable
                    );

                    context.write(new Text(productId), this.sentiment);
                }
            }
        }
    }
}
