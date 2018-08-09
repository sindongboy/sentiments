package com.skplanet.nlp.sentiment.knowledge;

import java.io.IOException;

/**
 * @author Donghun Shin / donghun.shin@sk.com
 * @date 10/22/15
 */
public class TSV2SHEET {
    public static void main(String[] args) throws IOException {
        /*
        CommandLineInterface cli = new CommandLineInterface();
        cli.addOption("t", "tsv", true, "tsv file to be converted to sheet", true);
        cli.addOption("c", "category", true, "category number", true);
        cli.addOption("o", "output", true, "output path", true);
        cli.parseOptions(args);

        File tsvFile = new File(cli.getOption("t"));
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(tsvFile));
        String line;
        MultiMap<String, String> map = new MultiHashMap<String, String>();

        while ((line = reader.readLine()) != null) {
            if (line.trim().length() == 0) {
                continue;
            }

            String[] fields = line.split("\\t");
            if (fields.length != 4) {
                System.err.println("wrong fields: " + line);
                continue;
            }

            String rep = fields[0];
            String att = fields[1];
            String exp = fields[2];
            String pol = fields[3];

            map.put(att, exp + "\t" + pol + "\t" + rep);
        }
        reader.close();

        BufferedWriter writer;
        File sheetFile = new File(cli.getOption("o"));
        writer = new BufferedWriter(new FileWriter(sheetFile));



        Set<String> keySet = map.keySet();
        for (String key : keySet) {
            writer.write("FEA:" + key);
            writer.newLine();

            List<String> expressions = (List<String>) map.get(key);
            for (String exp : expressions) {
                //System.out.println("exp: " + exp);
                writer.write("EXP:" + exp);
                writer.newLine();
            }
            writer.newLine();
        }
        writer.close();
        */
    }

}
