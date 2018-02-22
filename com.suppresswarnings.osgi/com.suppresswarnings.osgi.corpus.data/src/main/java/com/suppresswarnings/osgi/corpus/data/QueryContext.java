package com.suppresswarnings.osgi.corpus.data;

import java.util.ArrayList;
import java.util.List;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;
import com.suppresswarnings.osgi.corpus.WXContext;
import com.suppresswarnings.osgi.corpus.WXService;

public class QueryContext extends WXContext {
	List<Stage> stages = new ArrayList<Stage>();
	int index = 0;
	public Stage stage(){
		if(index >= 0 && index < stages.size()) {
			return stages.get(index);
		}
		return null;
	}
	public QueryContext(String openid, WXService ctx) {
		super(openid, ctx);
		Stage one = new Stage("age", "你几岁了？");
		RequireMinMax ageR = new RequireMinMax(1, 140);
		one.addRequire(ageR);
		
		Stage two = new Stage("email", "请输入你的邮箱：");
		RequireLength lengthR = new RequireLength(3, 140);
		RequireEmail emailR = new RequireEmail();
		two.addRequire(lengthR);
		two.addRequire(emailR);
		stages.add(one);
		stages.add(two);
	}
	
	State<Context<WXService>> start = new State<Context<WXService>>(){

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
			return chain;
		}

		@Override
		public String name() {
			return "start";
		}

		@Override
		public boolean finish() {
			return false;
		}};
	
	State<Context<WXService>> end = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 6779162430810222883L;

		@Override
		public void accept(String t, Context<WXService> u) {
			for(Stage stage : stages) {
				log(stage.toString());
			}
			u.output("任务完成，进入待机状态");
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			return init;
		}

		@Override
		public String name() {
			return "end";
		}

		@Override
		public boolean finish() {
			return false;
		}};
	
	State<Context<WXService>> chain = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 8066861158938018887L;

		@Override
		public void accept(String t, Context<WXService> u) {
			Stage stage = stage();
			if(stage == null) {
				u.output("不存在的");
				return;
			}
			stage.setValue(t);
			if(stage.agree()) {
				index ++;
				stage = stage();
				if(stage != null) {
					u.output("信息已经记录，下一条：\n" + stage.getTitle());
				} else {
					u.output("恭喜你所有任务已经完成");
				}
			} else {
				u.output("数据不正确，请重试：\n" + stage.getTitle());
			}
		}
		

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			if(index >= stages.size()) {
				return init;
			}
			return this;
		}

		@Override
		public String name() {
			return "数据收集";
		}

		@Override
		public boolean finish() {
			return false;
		}};
}
