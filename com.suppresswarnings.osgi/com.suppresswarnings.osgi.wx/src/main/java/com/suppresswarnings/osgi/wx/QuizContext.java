package com.suppresswarnings.osgi.wx;

import com.suppresswarnings.osgi.alone.Context;

public class QuizContext extends Context<String> {
	static String quiz = "请回答与电影相关的语句。";
	public QuizContext(){
		super(quiz, new QuizS().S1);
	}
	
}
