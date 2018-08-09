package com.skplanet.nlp.sentiment.driver;

import com.skplanet.nlp.sentiment.analyzer.Negation;
import com.skplanet.nlp.sentiment.ds.Pair;
import com.skplanet.nlp.sentiment.knowledge.Collocation;
import com.skplanet.nlp.sentiment.util.NLPUtils;

import java.util.Scanner;

/**
 * Sample Negation Tester
 *
 * @author Donghun Shin / donghun.shin@sk.com
 *
 */
final class NegationTester {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        NLPUtils nlp = NLPUtils.getInstance();
        Pair[] pair;

        Collocation colloc = Collocation.getInstance();
		colloc.init();

        String line;

        System.out.print("INPUT: ");
        while ((line = scan.nextLine()) != null) {
            if (line.trim().length() == 0) {
                continue;
            }
            if (line.toLowerCase().equals("exit")) {
                break;
            }
            if (line.toLowerCase().equals("reload")) {
                colloc.dictionaryReLoading();
                System.out.println("collocation dictionary reloaded");
                continue;
            }

            //nlpRes = nlp.getNLPResultBySentence(line);
			String [] toks = nlp.getMorphs(line);
			String [] tags = nlp.getPOSTags(line);
            pair = colloc.process(toks, tags, 2, true);


            for (int i = 0; i < pair.length; i++) {
                if (pair[i].getSecond().equals("vv") || pair[i].getSecond().equals("va")) {
                    if (Negation.isNegated(pair, i)) {
                        System.out.println(pair[i].getFirst() + "/" + pair[i].getSecond() + " is negated");
                    } else {
                        System.out.println(pair[i].getFirst() + "/" + pair[i].getSecond() + " is not negated");
                    }
                }
            }

            System.out.print("INPUT: ");
        }
		scan.close();
    }

    private NegationTester() {

    }
}
