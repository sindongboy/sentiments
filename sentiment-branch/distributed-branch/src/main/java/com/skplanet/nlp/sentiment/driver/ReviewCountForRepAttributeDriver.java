package com.skplanet.nlp.sentiment.driver;


import com.skplanet.nlp.sentiment.Prop;
import com.skplanet.nlp.sentiment.ds.SentimentWritable;
import com.skplanet.nlp.sentiment.mapper.ReviewCountForRepAttributeMapper;
import com.skplanet.nlp.sentiment.reducer.ReviewCountForRepAttributeReducer;
import com.skplanet.nlp.sentiment.util.HDFSUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Review Counter for each Rep. Attribute keyword ( MapReduce Implementation )
 *
 * @author Donghun Shin / donghun.shin@sk.com
 * @since 3/11/16
 *
 */
public class ReviewCountForRepAttributeDriver extends Configured implements Tool {

    private static final Logger LOGGER = Logger.getLogger(ReviewCountForRepAttributeDriver.class.getName());

    public int run(String[] args) throws Exception {

        Configuration conf = getConf();
        //conf.addResource(new Path(Prop.HADOOP_CONF_PATH + "/core-site.xml"));
        //conf.addResource(new Path(Prop.HADOOP_CONF_PATH + "/hdfs-site.xml"));
        //conf.addResource(new Path(Prop.HADOOP_CONF_PATH + "/mapred-site.xml"));
        //conf.addResource(new Path(Prop.HADOOP_CONF_PATH + "/yarn-site.xml"));

        // require args[0] to be the partition number
        // ex) 20140425
        args = new GenericOptionsParser(conf, args).getRemainingArgs();

        HDFSUtil hdfs = HDFSUtil.getInstance();
        String propStr = hdfs.read(Prop.PROP_PATH);
        InputStream propIS = new ByteArrayInputStream(propStr.getBytes());
        Properties prop = new Properties();
        prop.load(propIS);

        Job job = Job.getInstance(conf, "Sentiment Analyzer for Product Reviews: " + args[0]);

        // set queuename
        job.getConfiguration().set("mapreduce.job.queuename", "TAS");
        // set task timeout
        //job.getConfiguration().set("mapreduce.task.timeout", "600000");
        // set retry number
        job.getConfiguration().set("mapreduce.map.max.attempts", "2");
        // set min. size of split
        //job.getConfiguration().set("mapreduce.input.fileinputformat.split.minsize", "4194304");
        // set max. size of split
        //job.getConfiguration().set("mapreduce.input.fileinputformat.split.maxsize", "4194304");
        // set mapper numbers
        //job.getConfiguration().set("mapreduce.job.maps", "1000000");

        hdfs.delete(prop.getProperty(Prop.OUTPUT_BASE) + args[0]);
        FileInputFormat.addInputPath(job, new Path(prop.getProperty(Prop.INPUT_BASE) + args[0]));
        FileOutputFormat.setOutputPath(job, new Path(prop.getProperty(Prop.OUTPUT_BASE) + args[0]));

        job.setJarByClass(ReviewCountForRepAttributeDriver.class);
        job.setMapperClass(ReviewCountForRepAttributeMapper.class);
        job.setReducerClass(ReviewCountForRepAttributeReducer.class);
        //job.setNumReduceTasks(1);

        // Mapper emits a string as key and an SentimentWritable as value
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(SentimentWritable.class);

        // Reducer emits a result text
        job.setOutputKeyClass(WritableComparable.class);
        job.setOutputValueClass(Text.class);

        // wait for completion
        return (job.waitForCompletion(true) ? 0 : 1);
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new ReviewCountForRepAttributeDriver(), args);
        System.exit(exitCode);
    }
}
