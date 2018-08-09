package com.skplanet.nlp;

import com.skplanet.nlp.cli.CommandLineInterface;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;

/**
 * Dictionary Preprocessing Utility
 *
 * @author Donghun Shin / donghun.shin@sk.com
 * @since  3/13/16
 */
public class DictPreprocessor {

    private static final Logger LOGGER = Logger.getLogger(DictPreprocessor.class.getName());

    private static String rawPath = null;
    private static String nlpPath = null;

    //static HDFSUtil hdfs = HDFSUtil.getInstance();

    static NLPUtils nlp = NLPUtils.getInstance();
    static Collocation collocation = Collocation.getInstance();
    static KeyTerm keyterm = KeyTerm.getInstance();

    public static void main(String[] args) throws IOException {
        CommandLineInterface cli = new CommandLineInterface();
        cli.addOption("c", "category", true, "category", true);
        cli.addOption("i", "input", true, "input dict path", true);
        cli.addOption("o", "output", true, "output dict path", true);
        cli.parseOptions(args);

        //String category = cli.getOption("c");
        String category = cli.getOption("c");
        rawPath = cli.getOption("i");
        nlpPath = cli.getOption("o");

        processAttribute(category);
        processExpression(category);
        processMapping(category);

    }

    /**
     * NLP to attribute dictionary
     * @param category category number
     * @throws IOException
     */
    static void processAttribute(String category) throws IOException {
        File inputFile = new File(rawPath + "/" + Prop.ATT_DICT_PREFIX + category + Prop.DICT_EXT);

        int count = 0;
        StringBuffer resultSB = new StringBuffer();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        while ((line = reader.readLine()) != null) {
            if (line.trim().length() == 0 || line.startsWith("#")) {
                continue;
            }

            if (count % 100 == 0) {
                LOGGER.info("attribute processing: " + count);
            }
            count++;

            String[] fields = line.trim().split("\\t");
            if (fields.length != 2) {
                LOGGER.error("attribute dictionary has wrong entry: " + line);
            }

            resultSB.append(fields[0] + "\t" + fields[1] + "\t");
            String[] acronyms = fields[1].split(",");

            StringBuffer sb = new StringBuffer();
            //for (String att : acronyms) {
            for (int i = 0; i < acronyms.length; i++) {
                Pair[] nlpResults = nlp.getNLPResult(acronyms[i].toLowerCase());

                //for (Pair nlpResult : nlpResults) {
                for (int j = 0; j < nlpResults.length; j++) {
                    if (j == nlpResults.length - 1) {
                        sb.append(nlpResults[j].getFirst() + "/" + nlpResults[j].getSecond());
                    } else {
                        sb.append(nlpResults[j].getFirst() + "/" + nlpResults[j].getSecond()).append(" ");
                    }
                }
                if (i < acronyms.length - 1) {
                    sb.append('\001');
                }
            }
            resultSB.append(sb.toString()).append("\n");
        }
        reader.close();

        File outputFile = new File(nlpPath + "/" + Prop.ATT_DICT_PREFIX + category + Prop.DICT_EXT);
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write(resultSB.toString());
        writer.close();
    }

    static void processExpression(String category) throws IOException {
        File inputFile = new File(rawPath + "/" + Prop.EXP_DICT_PREFIX + category + Prop.DICT_EXT);

        int count = 0;
        StringBuffer resultSB = new StringBuffer();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));

        while ((line = reader.readLine()) != null) {

            if (line.trim().length() == 0 || line.startsWith("#")) {
                continue;
            }

            if (count % 100 == 0) {
                LOGGER.info("expression processing: " + count);
            }
            count++;

            String[] fields = line.trim().split("\\t");
            // 0: TAG
            // 1: Expression
            // 2: Polarity
            if (fields.length != 3) {
                LOGGER.error("expression dictionary has wrong entry: " + line);
                continue;
            }

            resultSB.append(fields[0] + "\t" + fields[1] + "\t");

            StringBuffer sb = new StringBuffer();
            //Pair[] nlpResults = nlp.getNLPResult(fields[1]);

            Pair[] tmpColloc = collocation.process(nlp.getMorphs(fields[1]), nlp.getPOSTags(fields[1]), 3, false);

            tmpColloc = keyterm.process(Pair.getFirsts(tmpColloc), Pair.getSeconds(tmpColloc));
            ArrayList<Pair> finalColloc = new ArrayList<Pair>();
            for (Pair p : tmpColloc) {
                if (!p.getSecond().equals(KeyTerm.SKIP_TAG)) {
                    finalColloc.add(p);
                }
            }


            for (int i = 0; i < finalColloc.size(); i++) {
                if (i == finalColloc.size() - 1) {
                    sb.append(finalColloc.get(i).getFirst() + "/" + finalColloc.get(i).getSecond());
                } else {
                    sb.append(finalColloc.get(i).getFirst() + "/" + finalColloc.get(i).getSecond()).append(" ");
                }
            }

            sb.append("\t" + fields[2]);

            resultSB.append(sb.toString().trim().replaceAll("\\+\\t", "\t")).append("\n");

        }


        File outputFile = new File(nlpPath + "/" + Prop.EXP_DICT_PREFIX + category + Prop.DICT_EXT);
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write(resultSB.toString());
        writer.close();
    }

    static void processMapping(String category) throws IOException {
        File inputFile = new File(rawPath + "/" + Prop.MAP_DICT_PREFIX + category + Prop.DICT_EXT);

        int count = 0;
        StringBuffer resultSB = new StringBuffer();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));

        while ((line = reader.readLine()) != null) {
            if (line.trim().length() == 0 || line.startsWith("#")) {
                continue;
            }

            if (count % 100 == 0) {
                LOGGER.info("mapping processing: " + count);
            }
            count++;

            String[] fields = line.trim().split("\\t");
            // 0: Att Key
            // 1: Expressions _ Rep.Attribute ,
            if (fields.length != 2) {
                LOGGER.error("mapping dictionary has wrong entry: " + line);
            }

            resultSB.append(fields[0] + "\t");

            String[] efields = fields[1].split(",");
            StringBuffer sb = new StringBuffer();
            for (String efield : efields) {
                String[] eItem = efield.split("_");
                sb.append(eItem[0]).append("_").append(eItem[1]);

                /*
                Pair[] nlpResults = nlp.getNLPResult(eItem[1]);

                for (Pair nlpResult : nlpResults) {
                    sb.append(nlpResult.getFirst() + "/" + nlpResult.getSecond()).append("+");
                }
                */
                sb.append(",");
            }

            resultSB.append(sb.toString().trim().replaceAll("\\+,", ",").replaceAll(",$", "")).append("\n");

        }

        File outputFile = new File(nlpPath + "/" + Prop.MAP_DICT_PREFIX + category + Prop.DICT_EXT);
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write(resultSB.toString());
        writer.close();
    }
}
