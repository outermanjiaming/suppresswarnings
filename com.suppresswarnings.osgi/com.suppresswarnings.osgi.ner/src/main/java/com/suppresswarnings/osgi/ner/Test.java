package com.suppresswarnings.osgi.ner;

public class Test {

	public static void main(String[] args) {
		//1.tagged -> tsv
//		Train.tsv("D:/files/todo/company/ner/datetimelocation.tag");
		//2.tsv -> train
//		Train.train("D:/files/todo/company/ner/datetimelocation.tag_tsv", "D:/files/todo/company/ner/datetimelocation.ner");
		//3.model -> API
		API api = new API("D:/files/todo/company/ner/datetimelocation.ner");
		String tagged = api.tag("你们今天中午11点去下村吃饭吧");
		System.out.println(tagged);
	}
}
