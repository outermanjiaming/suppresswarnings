package com.suppresswarnings.osgi.ner;

import java.util.List;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

public class API {
	AbstractSequenceClassifier<CoreLabel> classifier;
	String model = "model.ser";
	private API(){
		classifier = CRFClassifier.getClassifierNoExceptions(model);
	}
	public API(String model){
		this.model = model;
		classifier = CRFClassifier.getClassifierNoExceptions(this.model);
	}
	public void reload(){
		String model = System.getProperty("model", this.model);
		this.model = model;
		classifier = CRFClassifier.getClassifierNoExceptions(this.model);
	}
	public static API getInstance(){
		return Holder.INSTANCE;
	}
	private static final class Holder{
		static final API INSTANCE = new API();
	}
	public Item[] ner(String originText){
		return ner(this.classifier, originText);
	}
	public String tag(String originText) {
		return tag(this.classifier, originText);
	}
	public static Item[] ner(AbstractSequenceClassifier<CoreLabel> classifier, String originText) {
		String textWithSpaces = Util.format(originText, false);
		String uniformText    = Util.format(originText, true);
		List<Triple<String, Integer, Integer>> list = classifier.classifyToCharacterOffsets(uniformText);
		Item[] items = new Item[list.size()];
		for (int i=0;i<list.size();i++) {
			Triple<String, Integer, Integer> item = list.get(i);
			String key = item.first();
			String value = Util.keep1Space(textWithSpaces.substring(item.second(), item.third()));
			items[i] = new Item(i).key(key).value(value);
		}
		return items;
	}
	
	public static String tag(AbstractSequenceClassifier<CoreLabel> classifier, String originText) {
		String textWithSpaces = Util.format(originText, false);
		String uniformText    = Util.format(originText, true);
		List<Triple<String, Integer, Integer>> list = classifier.classifyToCharacterOffsets(uniformText);
		return tag0(list, textWithSpaces);
	}
	
	public static String tag(String model, String text) {
		AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(model);
		String tagged = tag(classifier, text);
		System.err.println(tagged);
		return tagged;
	}
	
	public static String tag0(Item[] items, String originText) {
		StringBuffer sb = new StringBuffer();
		int index = 0;
		for (Item item : items) {
			String value = item.value;
			int i = originText.indexOf(value, index);
			sb.append(originText.substring(index, i));
			sb.append("{") .append(value) .append("|") .append(item.key) .append("}");
			index = i + value.length();
		}
		sb.append(originText.substring(index, originText.length()));
		String tagged = sb.toString();
		return tagged;
	}
	
	public static String tag0(List<Triple<String, Integer, Integer>> list, String textWithSpaces) {
		StringBuffer sb = new StringBuffer();
		int index = 0;
		for (Triple<String, Integer, Integer> item : list) {
			String value = Util.keep1Space(textWithSpaces.substring(item.second(), item.third()));
			sb.append(Util.keep1Space(textWithSpaces.substring(index, item.second())));
			sb.append("{") .append(value) .append("|") .append(item.first()) .append("}");
			index = item.third();
		}
		sb.append(Util.removeLastLeftBraces(Util.keep1Space(textWithSpaces.substring(index, textWithSpaces.length()))));
		String tagged = sb.toString();
		return tagged;
	}
}
