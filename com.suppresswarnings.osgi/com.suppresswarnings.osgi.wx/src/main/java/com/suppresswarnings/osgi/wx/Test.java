package com.suppresswarnings.osgi.wx;

import java.util.stream.Stream;

import com.suppresswarnings.osgi.ner.API;
import com.suppresswarnings.osgi.ner.Item;

public class Test {

	public static void main(String[] args) {
//		QuizContext ctx = new QuizContext(QuizS.S1);
//		Shell shell = new Shell();
//		boolean x = Stream.of("QUIZ", "$$").anyMatch(ctx);
//		System.out.println(x);
//		System.out.println(ctx);
//		System.out.println(ctx.output());
		
		API api = new API("D:/files/todo/company/ner/quiz.ner");
		Item[] items = api.ner("这是什么时候");
		QuizContext context = new QuizContext();
		for(Item it : items) {
			System.out.print(it);
			context.test(it.key());
		}
		System.out.println(context.output());
	}
}
