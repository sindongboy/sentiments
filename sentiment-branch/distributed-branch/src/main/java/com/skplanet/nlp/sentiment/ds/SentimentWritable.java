package com.skplanet.nlp.sentiment.ds;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Hadoop DataType implements {@link WritableComparable} for {@link Sentiment}
 *
 * it holds following information
 * - content id
 * - sentence id
 * - attribute id
 * - attribute text
 * - expression id
 * - expression text
 * - polarity
 *
 * @author Donghun Shin / donghun.shin@sk.com
 * @since  3/31/16
 */
public class SentimentWritable implements WritableComparable<SentimentWritable> {

    // for MapReduce framework
    private Text contentIdText;
    private Text cat1Text;
    private Text cat2Text;
    private Text cat3Text;
    private Text cat4Text;
    private Text sentenceIdText;
    private Text attributeText;
    private Text attributeIdText;
    private Text expressionText;
    private Text expressionIdText;
    private Text repAttributeText;
    private IntWritable polarityIntWritable;

    /**
     * default constructor
     */
    public SentimentWritable() {
        contentIdText = new Text();
        cat1Text = new Text();
        cat2Text = new Text();
        cat3Text = new Text();
        cat4Text = new Text();
        sentenceIdText = new Text();
        attributeText = new Text();
        attributeIdText = new Text();
        expressionText = new Text();
        expressionIdText = new Text();
        repAttributeText = new Text();
        polarityIntWritable = new IntWritable();
    }

    /**
     * Custom constructor
     *
     * @param contentIdText content id
     * @param cat1Text  category 1
     * @param cat2Text  category 2
     * @param cat3Text  category 3
     * @param cat4Text  category 4
     * @param sentenceIdText  sentiment id
     * @param attributeText        attribute {@link Text}
     * @param attributeIdText      attribute id {@link Text}
     * @param expressionText        expression {@link Text}
     * @param expressionIdText      expression id {@link Text}
     * @param repAttributeText      rep. attribute keyword {@link Text}
     * @param polarityIntWritable polarity {@link IntWritable}
     */
    public SentimentWritable(
            Text contentIdText,
            Text cat1Text,
            Text cat2Text,
            Text cat3Text,
            Text cat4Text,
            Text sentenceIdText,
            Text attributeText,
            Text attributeIdText,
            Text expressionText,
            Text expressionIdText,
            Text repAttributeText,
            IntWritable polarityIntWritable) {

        this.contentIdText = contentIdText;
        this.cat1Text = cat1Text;
        this.cat2Text = cat2Text;
        this.cat3Text = cat3Text;
        this.cat4Text = cat4Text;
        this.sentenceIdText = sentenceIdText;
        this.attributeText = attributeText;
        this.attributeIdText = attributeIdText;
        this.expressionText = expressionText;
        this.expressionIdText = expressionIdText;
        this.repAttributeText = repAttributeText;
        this.polarityIntWritable = polarityIntWritable;
    }

    public void set(
            Text contIdText,
            Text cat1Text,
            Text cat2Text,
            Text cat3Text,
            Text cat4Text,
            Text sentIdText,
            Text attText,
            Text attIdText,
            Text expText,
            Text expIdText,
            Text repAttributeText,
            IntWritable polIntWritable) {

        this.contentIdText = contIdText;
        this.cat1Text = cat1Text;
        this.cat2Text = cat2Text;
        this.cat3Text = cat3Text;
        this.cat4Text = cat4Text;
        this.sentenceIdText = sentIdText;
        this.attributeText = attText;
        this.attributeIdText = attIdText;
        this.expressionText = expText;
        this.expressionIdText = expIdText;
        this.repAttributeText = repAttributeText;
        this.polarityIntWritable = polIntWritable;
    }

    public void write(DataOutput dataOutput) throws IOException {
        this.contentIdText.write(dataOutput);
        this.cat1Text.write(dataOutput);
        this.cat2Text.write(dataOutput);
        this.cat3Text.write(dataOutput);
        this.cat4Text.write(dataOutput);
        this.sentenceIdText.write(dataOutput);
        this.attributeText.write(dataOutput);
        this.attributeIdText.write(dataOutput);
        this.expressionText.write(dataOutput);
        this.expressionIdText.write(dataOutput);
        this.repAttributeText.write(dataOutput);
        this.polarityIntWritable.write(dataOutput);
    }

    public void readFields(DataInput dataInput) throws IOException {
        this.contentIdText.readFields(dataInput);
        this.cat1Text.readFields(dataInput);
        this.cat2Text.readFields(dataInput);
        this.cat3Text.readFields(dataInput);
        this.cat4Text.readFields(dataInput);
        this.sentenceIdText.readFields(dataInput);
        this.attributeText.readFields(dataInput);
        this.attributeIdText.readFields(dataInput);
        this.expressionText.readFields(dataInput);
        this.expressionIdText.readFields(dataInput);
        this.repAttributeText.readFields(dataInput);
        this.polarityIntWritable.readFields(dataInput);
    }

    public int compareTo(SentimentWritable o) {
        return this.polarityIntWritable.compareTo(o.polarityIntWritable);
    }

    public Text getContentIdText() {
        return contentIdText;
    }

    public void setContentIdText(Text contentIdText) {
        this.contentIdText = contentIdText;
    }

    public Text getCat1Text() {
        return cat1Text;
    }

    public void setCat1Text(Text cat1Text) {
        this.cat1Text = cat1Text;
    }

    public Text getCat2Text() {
        return cat2Text;
    }

    public void setCat2Text(Text cat2Text) {
        this.cat2Text = cat2Text;
    }

    public Text getCat3Text() {
        return cat3Text;
    }

    public void setCat3Text(Text cat3Text) {
        this.cat3Text = cat3Text;
    }

    public Text getCat4Text() {
        return cat4Text;
    }

    public void setCat4Text(Text cat4Text) {
        this.cat4Text = cat4Text;
    }

    public Text getSentenceIdText() {
        return sentenceIdText;
    }

    public void setSentenceIdText(Text sentenceIdText) {
        this.sentenceIdText = sentenceIdText;
    }

    public Text getAttributeText() {
        return attributeText;
    }

    public void setAttributeText(Text attributeText) {
        this.attributeText = attributeText;
    }

    public Text getAttributeIdText() {
        return attributeIdText;
    }

    public void setAttributeIdText(Text attributeIdText) {
        this.attributeIdText = attributeIdText;
    }

    public Text getExpressionText() {
        return expressionText;
    }

    public void setExpressionText(Text expressionText) {
        this.expressionText = expressionText;
    }

    public Text getExpressionIdText() {
        return expressionIdText;
    }

    public void setExpressionIdText(Text expressionIdText) {
        this.expressionIdText = expressionIdText;
    }

    public Text getRepAttributeText() {
        return repAttributeText;
    }

    public void setRepAttributeText(Text repAttributeText) {
        this.repAttributeText = repAttributeText;
    }

    public IntWritable getPolarityIntWritable() {
        return polarityIntWritable;
    }

    public void setPolarityIntWritable(IntWritable polarityIntWritable) {
        this.polarityIntWritable = polarityIntWritable;
    }
}
