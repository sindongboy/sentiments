package com.skplanet.nlp.sentiment.driver;

import com.skplanet.nlp.cli.CommandLineInterface;
import com.skplanet.nlp.sentiment.analyzer.SentimentAnalyzer;
import com.skplanet.nlp.sentiment.ds.Pair;
import com.skplanet.nlp.sentiment.ds.Sentiment;
import com.skplanet.nlp.sentiment.knowledge.Collocation;
import com.skplanet.nlp.sentiment.knowledge.SentimentDict;
import com.skplanet.nlp.sentiment.util.NLPUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Scanner;

/**
 * Command Line Interface Mode Sentiment Analyzer Test Program
 * @author Donghun Shin / donghun.shin@sk.com
 * @date 10/26/15
 */
public class SentimentCLI {
    private static final Logger LOGGER = Logger.getLogger(SentimentCLI.class.getName());
    public static void main(String[] args) {
        // command interface
        CommandLineInterface cli = new CommandLineInterface();
        cli.addOption("c", "category", true, "category number", true);
        cli.parseOptions(args);

        // User Interface(System.in and out)
        Scanner scan = new Scanner(System.in);

        // NLP init.
        NLPUtils nlp = NLPUtils.getInstance();

        // Sentiment Dict Init.
        SentimentDict sentimentDict = new SentimentDict(nlp);
        sentimentDict.load(cli.getOption("c"));

        // Analyzer Init.
        SentimentAnalyzer analyzer = new SentimentAnalyzer(sentimentDict);

        Collocation collocation = Collocation.getInstance();
        collocation.init();

        String line;
        System.out.print("INPUT: ");
        while ((line = scan.nextLine()) != null) {
            if (line.trim().length() == 0) {
                System.out.print("INPUT: ");
                continue;
            }

            String[] sents = nlp.getSentences(line.trim());

            int sentenceCount = 0;
            for (String sent : sents) {
                List<Sentiment> sentiments = analyzer.find(sent);

                // sentence output
                System.out.println("SENTENCE" + sentenceCount + ": " + sent);

                // nlp output
                System.out.print("NLP" + sentenceCount + ": ");
                StringBuffer nlpSb = new StringBuffer();
                Pair[] nlpResults = nlp.getNLPResult(sent);
                for (Pair nlpResult : nlpResults) {
                    nlpSb.append(nlpResult.getFirst() + "/" + nlpResult.getSecond() + " ");
                }
                System.out.println(nlpSb.toString().trim());

                // keyterm output
                System.out.print("COLLOCATION: ");
                Pair[] collocPair = collocation.process(nlp.getMorphs(sent), nlp.getPOSTags(sent), 3, true);
                for (Pair colloc : collocPair) {
                    System.out.print(colloc.getFirst() + "/" + colloc.getSecond() + " ");
                }
                System.out.println();

                int sentimentCount = 0;
                for (Sentiment sentiment : sentiments) {
                    System.out.println("SENTIMENT" + sentimentCount + ": " + sentiment.getAttribute().getText() + " || " + sentiment.getExpression().getText() + " || " + sentiment.getExpression().getValue());
                    sentimentCount++;
                }

            }

            System.out.print("INPUT: ");
        }

    }
}
