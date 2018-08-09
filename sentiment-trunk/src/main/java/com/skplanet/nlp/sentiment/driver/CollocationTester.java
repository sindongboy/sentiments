package com.skplanet.nlp.sentiment.driver;

import com.skplanet.nlp.sentiment.ds.Pair;
import com.skplanet.nlp.sentiment.knowledge.Collocation;
import com.skplanet.nlp.sentiment.util.NLPUtils;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Sample Collocation Tester
 *
 * @author Donghun Shin / donghun.shin@sk.com
 */
final class CollocationTester {

    public static void main(String [] args) {
        int iter = 4;

        NLPUtils nlp = NLPUtils.getInstance();
        Collocation colloc = Collocation.getInstance();
        colloc.init();

        Scanner scan = new Scanner(System.in);
        String line;
        System.out.print("\nINPUT: ");
        while ((line = scan.nextLine()) != null) {
            if (line.trim().length() == 0) {
                continue;
            }

            if (line.toLowerCase().equals("reload")) {
                colloc.dictionaryReLoading();
                System.out.print("INPUT: ");
                continue;
            }

            if (line.toLowerCase().equals("exit")) {
                System.exit(1);
            }

            String[] tokens = nlp.getMorphs(line);
            String[] postags = nlp.getPOSTags(line);
            ArrayList<Pair> pairs = new ArrayList<Pair>();
            for (int i = 0; i < tokens.length; i++) {
                Pair p = new Pair(tokens[i], postags[i]);
                pairs.add(p);
            }


            System.out.println("\nSentence: " + line);
            System.out.print("Before: ");
            for (Pair p : pairs) {
                System.out.print(p.getFirst() + "/" + p.getSecond() + " ");
            }
            System.out.println();

            // ------ PROCESS -------- //
            Pair[] after = colloc.process(tokens, postags, iter, true);
            // ------ PROCESS -------- //

            System.out.print("After: ");
            for (Pair p : after) {
                System.out.print(p.getFirst() + "/" + p.getSecond() + " ");
            }
            System.out.print("\nINPUT: ");
        }
        scan.close();
    }

    private CollocationTester() {
    }
}
