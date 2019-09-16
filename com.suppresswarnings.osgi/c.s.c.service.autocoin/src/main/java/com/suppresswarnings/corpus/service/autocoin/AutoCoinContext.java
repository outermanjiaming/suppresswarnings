package com.suppresswarnings.corpus.service.autocoin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.WXnews;

public class AutoCoinContext extends WXContext {
	public static final String CMD = "我要app";
	String commands = "";

	State<Context<CorpusService>> auto = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6826786535908216922L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			WXnews news = new WXnews();
			Gson gson = new Gson();
			news.setTitle("素朴网联 APP");
			news.setDescription("点击后选择在浏览器打开，然后下载，安装，激活，绑定。");
			news.setUrl("http://suppresswarnings.com/third.html?state=" + openid());
			news.setPicUrl("http://SuppressWarnings.com/like.png");
			String json = gson.toJson(news);
			u.output("news://" + json);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(command.name().equals(t)) return command;
			return init;
		}

		@Override
		public String name() {
			return "我要自动赚钱";
		}

		@Override
		public boolean finish() {
			return true;
		}
	};
	
	State<Context<CorpusService>> command = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2615975567766084449L;
		Map<String, String> cmds = new HashMap<>();
		Map<String, String> index = new HashMap<>();
		State<Context<CorpusService>> create = new State<Context<CorpusService>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5032258763358789716L;

			String now = null;

			State<Context<CorpusService>> save = new State<Context<CorpusService>>() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 8480407601558899896L;

				@Override
				public void accept(String t, Context<CorpusService> u) {
					String myCmdKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Autocoin", "CMD", time());
					u.content().account().put(myCmdKey, now);
					u.output("命令已经记录：" + now);
					index.clear();
					cmds.clear();
				}

				@Override
				public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
					return command;
				}

				@Override
				public String name() {
					return "保存";
				}

				@Override
				public boolean finish() {
					return true;
				}
			};
			
			@Override
			public void accept(String t, Context<CorpusService> u) {
				now = t;
				u.output("请直接输入命令，当前命令：" + now);
				u.output("确认命令请输入：" + create.name());
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if(create.name().equals(t)) return save;
				return create;
			}

			@Override
			public String name() {
				return "新增命令";
			}

			@Override
			public boolean finish() {
				return true;
			}
		};
		State<Context<CorpusService>> delete = new State<Context<CorpusService>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -4866277732248487816L;

			String key = null;
			
			State<Context<CorpusService>> ok = new State<Context<CorpusService>>() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 6826786535908216922L;

				@Override
				public void accept(String t, Context<CorpusService> u) {
					u.content().account().put(key, "None");
					u.output("删除成功");
					index.clear();
					cmds.clear();
				}

				@Override
				public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
					return command;
				}

				@Override
				public String name() {
					return "完成";
				}

				@Override
				public boolean finish() {
					return true;
				}
			};
			
			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(index.containsKey(t)) {
					key = index.get(t);
					String todo = cmds.get(key);
					u.output("你即将删除命令：" + todo);
					u.output("请输入：" + delete.name());
				} else {
					u.output("请选择你要删除的命令，输入1个数字：");
					index.forEach((i, k) ->{
						u.output(i + ". " + cmds.get(k));
					});
					u.output("你还可以输入：返回");
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if(delete.name().equals(t)) {
					return ok;
				}
				if("返回".equals(t)) return command;
				return delete;
			}

			@Override
			public String name() {
				return "删除命令";
			}

			@Override
			public boolean finish() {
				return true;
			}
		};
		
		
		@Override
		public void accept(String t, Context<CorpusService> u) {
			String myCmdKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Autocoin", "CMD");
			AtomicInteger integer = new AtomicInteger(1);
			if(cmds.isEmpty()) {
				u.content().account().page(myCmdKey, myCmdKey, null, 30, (k,v) ->{
					index.put("" + integer.get(), k);
					cmds.put(k, v);
					integer.incrementAndGet();
				});
			}
			u.output("你现在有"+integer.decrementAndGet()+"个命令：");
			u.output("你可以输入：");
			u.output(create.name());
			u.output(delete.name());
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(create.name().equals(t)) return create;
			if(delete.name().equals(t)) return delete;
			return command;
		}

		@Override
		public String name() {
			return "修改命令";
		}

		@Override
		public boolean finish() {
			return true;
		}
	};
	
	State<Context<CorpusService>> enter = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2790894786889317205L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			auto.accept(t, u);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return auto;
		}

		@Override
		public String name() {
			return "我要app";
		}

		@Override
		public boolean finish() {
			return true;
		}
	};
	
	public AutoCoinContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state(enter);
	}

}
