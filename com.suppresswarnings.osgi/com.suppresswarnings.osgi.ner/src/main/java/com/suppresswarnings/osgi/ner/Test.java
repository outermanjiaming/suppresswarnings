package com.suppresswarnings.osgi.ner;

public class Test {

	public static void main(String[] args) {
//		//1.tagged -> tsv
//		Train.tsv("D:/files/todo/company/ner/quiz.tag");
//		//2.tsv -> train
//		Train.train("D:/files/todo/company/ner/quiz.tag_tsv", "D:/files/todo/company/ner/quiz.ner");
		//3.model -> API
		API api = new API("D:/files/todo/company/ner/quiz.ner");
		String text = "你也不想答题吧";
		Item[] items = api.ner(text);
		for(Item it : items) {
			System.out.println(it.id() + ":" + it.toString());
		}
		String tagged = api.tag(text);
		System.out.println(tagged);
	}
}
