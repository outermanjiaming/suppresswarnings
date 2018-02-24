package com.suppresswarnings.osgi.corpus;

import java.util.ArrayList;
import java.util.List;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;

public class QuizContext extends WXContext {
	List<Stage> stages = new ArrayList<Stage>();
	int index;
	State<Context<WXService>> start, next;
	public Stage stage(){
		if(index >= 0 && index < stages.size()) {
			return stages.get(index);
		}
		return null;
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
					tried = 3;
					index ++;
					stage = stage();
					if(stage != null) {
						u.output("信息已经记录，下一条：\n" + stage.getTitle());
					} else {
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
		init(start);
	}
	
	public QuizContext next(Stage stage){
		stages.add(stage);
		return this;
	}
	
	
}
