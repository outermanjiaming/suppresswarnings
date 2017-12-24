package org.com.suppresswarnings.osgi.ner;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

public class Eval {

	public static void evaluate(String model, String originText) {
		
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length < 2) {
			System.out.println("java -cp this.jar com.meizu.ner.Eval path/to/jojo_12.0.model path/to/originText");
			return;
		}
		AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(args[0]);
		List<String> texts = Files.readAllLines(Paths.get(args[1]), Charset.forName("UTF-8"));
		for(String text : texts) {
			Item[] items = API.ner(classifier, text);
			for(Item item : items) {
				System.out.println(item);
			}
			System.out.println(API.tag0(items, text));
		}
	}
}
