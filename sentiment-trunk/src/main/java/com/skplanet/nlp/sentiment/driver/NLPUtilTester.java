package com.skplanet.nlp.sentiment.driver;

import com.skplanet.nlp.NLPDoc;
import com.skplanet.nlp.cli.CommandLineInterface;
import com.skplanet.nlp.sentiment.ds.Pair;
import com.skplanet.nlp.sentiment.util.NLPUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Scanner;

/**
 * NLP Tester Program
 * @author Donghun Shin / donghun.shin@sk.com
 * @date 2/4/15
 */
public final class NLPUtilTester {
    private static final Logger LOGGER = Logger.getLogger(NLPUtilTester.class.getName());

    public static void main(String[] args) {

        // set arguments list
        CommandLineInterface cli = new CommandLineInterface();
        cli.addOption("c", "cli", false, "interactive mode", false);
        cli.addOption("f", "file", true, "file mode, -f /path/to/the/file", false);
        cli.addOption("d", "directory", true, "bulk mode, -d /path/to/the/directory", false);
        cli.addOption("o", "output", true, "output file, -o /path/to/the/output/file", false);
        cli.parseOptions(args);

        NLPUtils nlp = NLPUtils.getInstance();
        String outputPath = null;
        if (cli.hasOption("o")) {
            outputPath = cli.getOption("o");
        }
        if (cli.hasOption("c") && !cli.hasOption("f") && !cli.hasOption("d")) { // cli mode
            runCommandLine(nlp);
        } else if (cli.hasOption("f") && !cli.hasOption("c") && !cli.hasOption("d")) {
            runFile(nlp, cli.getOption("f"), outputPath);
        } else if (cli.hasOption("d") && !cli.hasOption("c") && !cli.hasOption("f")) {
            runDirectory(nlp, cli.getOption("d"), outputPath);
        } else { // cli mode by default
            runCommandLine(nlp);
        }

    }

    /**
     * Run Interactive Mode
     */
    static void runCommandLine(NLPUtils nlp) {
        Scanner scan = new Scanner(System.in);
        String line;

        System.out.print("INPUT: ");
        while ((line = scan.nextLine()) != null) {
            if (line.trim().length() == 0) {
                System.out.print("INPUT: ");
                continue;
            }
            line = line.trim();
            String[] sentences = nlp.getSentences(line);
            int sCount = 0;
            for (String sentence : sentences) {
                System.out.println("S" + sCount + ":" + sentence);
                Pair[] nlpPairs = nlp.getNLPResult(sentence);
                System.out.print("NLP:");
                for (Pair nlpPair : nlpPairs) {
                    System.out.print(nlpPair.getFirst() + "/" + nlpPair.getSecond() + " ");
                }
                System.out.println();
            }

            System.out.print("INPUT: ");
        }
    }

    /**
     * Run File Mode 
     */
    static void runFile(NLPUtils nlp, String path, String output) {
        File file = new File(path);
        BufferedReader reader;
        BufferedWriter writer = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            if (output != null) {
                writer = new BufferedWriter(new FileWriter(new File(output)));
            }
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }

                line = line.trim();
                String[] sentences = nlp.getSentences(line);
                int sCount = 0;
                for (String sentence : sentences) {
                    NLPDoc nlpResult = nlp.getNLPDoc(sentence);


                    /*
                    if (output == null) {
                        System.out.println("S" + sCount + ":" + sentence);
                    } else {
                        writer.write("S" + sCount + ":" + sentence);
                        writer.newLine();
                    }
                    Pair[] nlpPairs = nlp.getNLPResult(sentence);
                    if (output == null) {
                        System.out.print("NLP:");
                    } else {
                        writer.write("NLP:");
                    }
                    for (Pair nlpPair : nlpPairs) {
                        if (output == null) {
                            System.out.print(nlpPair.getFirst() + "/" + nlpPair.getSecond() + " ");
                        } else {
                            writer.write(nlpPair.getFirst() + "/" + nlpPair.getSecond() + " ");
                        }
                    }
                    if (output == null) {
                        System.out.println();
                    } else {
                        writer.newLine();
                    }
                    */
                }
            }
            if (writer != null) {
                writer.close();
            }
            reader.close();
        } catch (FileNotFoundException e) {
            LOGGER.error("input file not found : " + path, e);
        } catch (IOException e) {
            LOGGER.error("failed to read input file : " + path, e);
        }

    }

    /**
     * Run Bulk Mode
     */
    static void runDirectory(NLPUtils nlp, String path, String output) {
        File[] files = new File(path).listFiles();
        for (File file : files) {
            runFile(nlp, path + "/" + file.getName(), output + "/" + file.getName());
        }
        LOGGER.info("not supported yet");
    }
}
