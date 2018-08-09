package com.skplanet.nlp.sentiment.driver;

import com.skplanet.nlp.cli.CommandLineInterface;
import com.skplanet.nlp.sentiment.analyzer.SentimentAnalyzer;
import com.skplanet.nlp.sentiment.ds.Sentiment;
import com.skplanet.nlp.sentiment.knowledge.SentimentDict;
import com.skplanet.nlp.sentiment.util.NLPUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.List;

/**
 * Distributed version of Sentiment Analyzer
 *
 * @author Donghun Shin / donghun.shin@sk.com
 * @date 3/7/16
 */
public class DistributedSentimentAnalyzer {
    /** logger */
    private static final Logger LOGGER = Logger.getLogger(DistributedSentimentAnalyzer.class.getName());

    public static void main(String [] args) {
        // interface
        CommandLineInterface cli = new CommandLineInterface();
        cli.addOption("c", "category", true, "category", true);
        cli.addOption("i", "input", true, "input path", true);
        cli.addOption("o", "output", true, "output path", true);
        cli.parseOptions(args);

        // nlp
        NLPUtils nlp = NLPUtils.getInstance();
        SentimentDict dict = new SentimentDict(nlp);
        dict.load(cli.getOption("c"));

        // analyzer
        SentimentAnalyzer analyzer = new SentimentAnalyzer(dict);

        //input file
        File inputDirectory = new File(cli.getOption("i"));
        if (!inputDirectory.isDirectory()) {
            LOGGER.error("Input Must be a directory", new FileNotFoundException());
        }

        // output base
        String outputBase = cli.getOption("o");

        BufferedReader reader;
        BufferedWriter writer;
        String line;
        int count = 1;
        int total = inputDirectory.listFiles().length;
        for (File f : inputDirectory.listFiles()) {
            if (!(f == null)) {
                if (f.getName().startsWith(".")) {
                    continue;
                }
                LOGGER.info("Processing : " + f.getName() + " (" + count + "/" + total + ")");
                try {
                    reader = new BufferedReader(new FileReader(f));
                    writer = new BufferedWriter(new FileWriter(new File(outputBase + "/" + f.getName())));
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().length() == 0) {
                            continue;
                        }

                        String[] sentences = nlp.getSentences(line);
                        for (String sent : sentences) {
                            List<Sentiment> result = analyzer.find(sent);
                            if (result.size() > 0) {
                                for (Sentiment s : result) {
                                    if (s.toString() == null) {
                                        continue;
                                    }
                                    writer.write(s.toString());
                                    writer.newLine();
                                }
                            }
                        }
                    }
                    writer.close();
                    reader.close();
                    count++;
                } catch (FileNotFoundException e) {
                    LOGGER.error("file not found", e);
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
        }
    }

}
