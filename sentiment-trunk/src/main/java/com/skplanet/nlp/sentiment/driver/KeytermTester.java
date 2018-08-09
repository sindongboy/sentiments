package com.skplanet.nlp.sentiment.driver;

import com.skplanet.nlp.sentiment.analyzer.KeyTerm;
import com.skplanet.nlp.sentiment.ds.Pair;
import com.skplanet.nlp.sentiment.knowledge.Collocation;
import com.skplanet.nlp.sentiment.util.NLPUtils;
import org.apache.log4j.Logger;

import java.util.Scanner;

/**
 * Sample Key Term Tester
 *
 * @author Donghun Shin / donghun.shin@sk.com
 */
final class KeytermTester {
	private static final Logger LOGGER = Logger.getLogger(KeytermTester.class.getName());

	public static void main(String [] args) {

		LOGGER.info("NLP initializing ....");
		NLPUtils nlp = NLPUtils.getInstance();
		LOGGER.info("NLP initializing done");
		
		LOGGER.info("keyterm instance creating");
		KeyTerm keyterm = KeyTerm.getInstance();
		LOGGER.info("keyterm instance created");

		LOGGER.info("Collocation initializing ....");
		Collocation colloc = Collocation.getInstance();
		colloc.init();
		LOGGER.info("Collocation initializing done");

		Scanner scan = new Scanner(System.in);

		String line; 
		System.out.print("INPUT: ");
		while((line = scan.nextLine()) != null) {
			if(line.trim().length()==0) {
				System.out.print("INPUT: ");
				continue;
			}

			String[] toks = nlp.getMorphs(line);
			String[] tags = nlp.getPOSTags(line);
			System.out.println("Before: " + displayNLPResult(toks, tags));
			Pair[] collocResult = colloc.process(toks, tags, 3, true);
			toks = Pair.getFirsts(collocResult);
			tags = Pair.getSeconds(collocResult);
			Pair[] keytermResult = keyterm.process(toks, tags);
			toks = Pair.getFirsts(keytermResult);
			tags = Pair.getSeconds(keytermResult);
			System.out.println("After: " + displayNLPResult(toks, tags));
			System.out.print("INPUT: ");
		}

		scan.close();
	}

	static String displayNLPResult(String[] toks, String[] tags) {
		String result = "";
		for(int i = 0; i < toks.length; i++) {
			result += toks[i] + "/" + tags[i] + " ";
		}
		return result.trim();
	}

    private KeytermTester() {

    }
}
