package com.suppresswarnings.osgi.corpus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;
import com.suppresswarnings.osgi.alone.Version;
import com.suppresswarnings.osgi.data.Const;

public class QuizContext extends WXContext {
	List<Stage> stages = new ArrayList<Stage>();
	int index;
	State<Context<WXService>> start, next;
	BiConsumer<Stage, WXContext> eachConsumer = new BiConsumer<Stage, WXContext>(){

		@Override
		public void accept(Stage t, WXContext u) {
			String key = String.join(Const.delimiter, Version.V1, Const.TextDataType.reply, t.getKey(), u.time(), u.openid());
			u.content().saveToData(key, t.getValue());
			u.update();
			u.log("[eachConsumer] save to data: " + key);
		}
	};
	BiConsumer<List<Stage>, WXContext> listConsumer =  new BiConsumer<List<Stage>, WXContext>(){

		@Override
		public void accept(List<Stage> t, WXContext u) {
			u.log("[listConsumer] stages size: " + t.size());
		}
	};
	public Stage stage(){
		if(index >= 0 && index < stages.size()) {
			return stages.get(index);
		}
		return null;
	}
	public QuizContext(BiConsumer<Stage, WXContext> eachConsumer, BiConsumer<List<Stage>, WXContext> listConsumer, String openid, WXService ctx) {
		this(openid, ctx);
		if(eachConsumer != null) this.eachConsumer = eachConsumer;
		if(listConsumer != null) this.listConsumer = listConsumer;
	}
	public QuizContext(String openid, WXService ctx) {
		super(openid, ctx);
		start = new State<Context<WXService>>(){

			/**
			 * 
			 */
			private static final long serialVersionUID = -6246433644555776385L;

			@Override
			public void accept(String t, Context<WXService> u) {
				Stage first = stage();
				if(first == null) {
					u.output("不存在的");
					return;
				}
				
				u.output(first.getTitle());
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				if(stages.isEmpty()) return init;
				return next;
			}

			@Override
			public String name() {
				return "start";
			}

			@Override
			public boolean finish() {
				return false;
			}};
		
		next = new State<Context<WXService>>(){
			int tried = 3;
			/**
			 * 
			 */
			private static final long serialVersionUID = 8066861158938018887L;

			@Override
			public void accept(String t, Context<WXService> u) {
				Stage stage = stage();
				if(stage == null) {
					tried = 3;
					u.output("不存在的");
					return;
				}
				stage.setValue(t);
				if(stage.agree()) {
					eachConsumer.accept(stage, QuizContext.this);
					tried = 3;
					index ++;
					stage = stage();
					if(stage != null) {
						u.output("信息已经记录，下一条：\n" + stage.getTitle());
					} else {
						listConsumer.accept(stages, QuizContext.this);
						u.output("信息记录完成：" + stages);
					}
				} else {
					if(tried < 1) {
						fail.accept(t, u);
					} else { 
						output(stage.error() +"，请重试("+tried+")：\n" + stage.getTitle());
					}
					
				}
			}
			

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				if(tried < 1) {
					tried = 3;
					return fail.apply(t, u);
				}
				if(index >= stages.size()) {
					return init;
				}
				tried --;
				return this;
			}

			@Override
			public String name() {
				return "next";
			}

			@Override
			public boolean finish() {
				return false;
			}
		};
		
		
		String name = openid + "-question-start";
		String begin = ctx.value(name);
		begin = ctx.pageOfQuestion(10, begin, new BiConsumer<String,String>(){

			@Override
			public void accept(String key, String title) {
				Stage stage = new Stage(key, title);
				next(stage);
			}});
		if(begin != null) {
			ctx.valuex(name, begin, TimeUnit.MINUTES.toMillis(3));
		}
		init(start);
	}
	
	public QuizContext next(Stage stage){
		this.stages.add(stage);
		return this;
	}
	
	public void stages(List<Stage> stages){
		this.stages.addAll(stages);
	}
}
