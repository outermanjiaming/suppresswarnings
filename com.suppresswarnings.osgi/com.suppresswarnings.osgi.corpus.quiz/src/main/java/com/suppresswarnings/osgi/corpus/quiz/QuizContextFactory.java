package com.suppresswarnings.osgi.corpus.quiz;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.corpus.ContextFactory;
import com.suppresswarnings.osgi.corpus.QuizContext;
import com.suppresswarnings.osgi.corpus.RequireChain;
import com.suppresswarnings.osgi.corpus.RequireLength;
import com.suppresswarnings.osgi.corpus.Stage;
import com.suppresswarnings.osgi.corpus.WXService;

public class QuizContextFactory implements ContextFactory {
	public static final long ttl = TimeUnit.HOURS.toMillis(2);
	RequireChain length20 = new RequireLength(1, 20);
	RequireChain length140 = new RequireLength(1, 140);
	@Override
	public Context<WXService> getInstance(String openid, WXService content) {
		QuizContext context = new QuizContext(openid, content);
		Stage one = new Stage("ancestor", "请问人类的祖先是什么？");
		one.andRequire(length20);
		
		Stage two = new Stage("color", "人类的肤色有哪几种？");
		two.andRequire(length140);
		
		Stage three = new Stage("language", "你会哪几种语言？");
		three.andRequire(length140);
		
		context.next(one).next(two).next(three);
		return context;
	}

	@Override
	public String command() {
		return "我要答题";
	}

	@Override
	public String description() {
		return "从题库中随机抽取题目进行答题";
	}

	@Override
	public long ttl() {
		return ttl;
	}

}
