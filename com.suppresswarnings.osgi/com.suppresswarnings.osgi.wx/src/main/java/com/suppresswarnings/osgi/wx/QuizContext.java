package com.suppresswarnings.osgi.wx;

import java.util.ArrayList;
import java.util.List;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;

public class QuizContext extends Context<List<String>> {
	String output = "请决定要不要答题：";
	public QuizContext(){
		super(QuizS.S1, new ArrayList<String>());
	}
	public QuizContext(State init) {
		super(init, new ArrayList<String>());
	}
	@Override
	public void accept(String t) {
		content.add(t);
		output = t;
	}
	@Override
	public String toString() {
		return "QuizContext [content=" + content + ", state=" + state.name() + "]";
	}
	public String output(){
		return output;
	}
}
