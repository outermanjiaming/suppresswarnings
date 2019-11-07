package com.suppresswarnings.corpus.service.shop;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import com.suppresswarnings.osgi.leveldb.LevelDB;

public class Required {
	String key;
	String question;
	String answer;
	Predicate<Required> predictor;
	BiConsumer<Required, LevelDB> acceptor;
	public String getQuestion() {
		return question;
	}
	public String key() {
		return key;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	
	public static Required of(String key, String question, Predicate<Required> predictor, BiConsumer<Required, LevelDB> acceptor) {
		Required required = new Required();
		required.key = key;
		required.question = question;
		required.predictor = predictor;
		required.acceptor = acceptor;
		return required;
	}
}
