package com.suppresswarnings.corpus.service.moneytree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.WXnews;

public class MoneyTree extends WXContext {
	public static final String CMD = "我的摇钱树";
	State<Context<CorpusService>> enter = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6508369618393668443L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			tree.accept(t, u);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(bind.name().equals(t)) return bind;
			if(register.name().equals(t)) return register;
			if(card.name().equals(t)) return card;
			if(buy.name().equals(t)) return buy;
			if(alliance.name().equals(t)) return alliance;
			return tree;
		}

		@Override
		public String name() {
			return "摇钱树入口";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<CorpusService>> bind = new State<Context<CorpusService>>() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 8353058721615305103L;
		String beacon = null;
		State<Context<CorpusService>> setname = new State<Context<CorpusService>>() {
			boolean set = false;
			/**
			 * 
			 */
			private static final long serialVersionUID = -8776193067563331461L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(setname.name().equals(t)) {
					u.output("请给你的摇钱树取个名字：");
				} else {
					String key = String.join(Const.delimiter, Const.Version.V1, "Info", "iBeacon", "Name", beacon);
					u.content().account().put(key, t);
					u.output("完成！你的摇钱树：" + t);
					set = true;
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if(set) {
					set = false;
					return init;
				}
				return setname;
			}

			@Override
			public String name() {
				return "修改名称";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		State<Context<CorpusService>> process = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 6989584353811257613L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				String ibeacon = t;
				u.output("正在绑定..." + ibeacon);
				String check = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "iBeacon", ibeacon));
				if(u.content().isNull(check)) {
					u.content().tellAdmins(openid(), "摇钱树编号错误/不存在/未登记：" + ibeacon);
					u.output("请联系素朴网联管理员，摇钱树编号错误/不存在/未登记：" + ibeacon);
				} else {
					String key = String.join(Const.delimiter, Const.Version.V1, "Info", "iBeacon", "Bind", ibeacon);
					String exist = u.content().account().get(key);
					if(u.content().isNull(exist)) {
						u.content().account().put(key, openid());
						beacon = ibeacon;
						String moneytree = String.join(Const.delimiter, Const.Version.V1, openid(), "Info", "iBeacon", "Bind", ibeacon);
						u.content().account().put(moneytree, ibeacon);
						u.output("恭喜恭喜，绑定成功：" + ibeacon);
						u.output("附近的用户打开素朴网联小程序，即可发现你的摇钱树，他们在你的摇钱树下聊天，你就可以获得奖金！注意：手机需要打开蓝牙和定位。现在你可以设置摇钱树的名称，请输入：");
						u.output(setname.name());
					} else {
						u.content().tellAdmins(openid(), "绑定摇钱树失败：" + ibeacon);
						u.output("摇钱树已经被别人绑定了，请联系管理员。");
					}
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if(setname.name().equals(t)) {
					return setname;
				}
				return init;
			}

			@Override
			public String name() {
				return "绑定";
			}

			@Override
			public boolean finish() {
				return true;
			}
		};

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("请输入摇钱树的编号：");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return process;
		}

		@Override
		public String name() {
			return "绑定摇钱树";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<CorpusService>> register = new State<Context<CorpusService>>() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 8353058721615305103L;
		State<Context<CorpusService>> process = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 6989584353811257613L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				//TODO bugly
				if(u.content().authrized(openid(), "Moneytree")) {
					String ibeacon = t;
					u.output("正在注册..." + ibeacon);
					String key = String.join(Const.delimiter, Const.Version.V1, "Info", "iBeacon", ibeacon);
					String check = u.content().account().get(key);
					if(u.content().isNull(check)) {
						u.content().account().put(key, "Register");
						u.output("摇钱树注册成功：" + ibeacon);
					} else {
						u.output("摇钱树"+ibeacon+"已注册：" + check);
					}
				} else {
					u.output("您暂时没有注册摇钱树的权限，请联系管理员。");
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return init;
			}

			@Override
			public String name() {
				return "绑定";
			}

			@Override
			public boolean finish() {
				return true;
			}
		};

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("请输入摇钱树的编号：");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return process;
		}

		@Override
		public String name() {
			return "注册摇钱树";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<CorpusService>> card = new State<Context<CorpusService>>() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 3180219263115384542L;
		State<Context<CorpusService>> process = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 6776035460586124439L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				String ibeacon = t;
				u.output("正在绑定..." + ibeacon);
				String check = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Card", ibeacon));
				if(u.content().isNull(check)) {
					u.output("卡券编号错误/不存在/未登记，请联系管理员");
				} else {
					String key = String.join(Const.delimiter, Const.Version.V1, "Info", "Card", "Bind", ibeacon);
					String exist = u.content().account().get(key);
					if(u.content().isNull(exist)) {
						u.content().account().put(key, openid());
						String moneytree = String.join(Const.delimiter, Const.Version.V1, openid(), "Info", "Card", "Bind", ibeacon);
						u.content().account().put(moneytree, ibeacon);
						u.output("恭喜恭喜，绑定成功：" + ibeacon);
						u.output("现在可以邀请用户领取卡券，指导用户点击广告才能有现金奖励。");
					} else {
						u.content().tellAdmins(openid(), "绑定卡券失败：" + ibeacon);
						u.output("卡券已经被别人绑定了，请联系管理员。");
					}
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return init;
			}

			@Override
			public String name() {
				return "绑定";
			}

			@Override
			public boolean finish() {
				return true;
			}
		};

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("请输入卡券的编号：");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return process;
		}

		@Override
		public String name() {
			return "绑定卡券";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<CorpusService>> buy = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5301495101957849715L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			WXnews news = new WXnews();
			Gson gson = new Gson();
			news.setTitle("立即抢购1棵摇钱树");
			news.setDescription("拥有1棵摇钱树，附近的人可以在你的摇钱树下聊天，你因此可以获得奖金。");
			news.setUrl("http://SuppressWarnings.com/payment.html?state=Moneytree");
			news.setPicUrl("http://SuppressWarnings.com/like.png");
			String json = gson.toJson(news);
			u.output("news://" + json);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return tree;
		}

		@Override
		public String name() {
			return "摇钱树";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	State<Context<CorpusService>> alliance = new State<Context<CorpusService>>() {
		String groupid = null;
		Required require = null;
		Required[] requests = {Required.of("请输入名称", r ->{
			return r.getAnswer() != null;
		},(r,db) ->{
			db.put(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Name"), r.getAnswer());
			logger.info(groupid + ":" + r.getQuestion() + " = " + r.getAnswer());
		}), Required.of("请上传图片", r ->{
			return r.getAnswer() != null && r.getAnswer().startsWith("IMAGE_");
		},(r,db)  ->{
			String icon = r.getAnswer().substring("IMAGE_".length());
			db.put(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Icon"), icon);
			logger.info(groupid + ":" + r.getQuestion() + " = " + r.getAnswer());
		}),Required.of("请发送位置", r ->{
			return r.getAnswer() != null && r.getAnswer().startsWith("LOC_");
		},(r,db)  ->{
			String location = r.getAnswer();
			String[] args = location.substring("LOC_".length()).split(";");
			db.put(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Latitude"), args[0]);
			db.put(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Longtitude"), args[1]);
			db.put(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Location"), args[2]);
			logger.info(groupid + ":" + r.getQuestion() + " = " + r.getAnswer());
		}),Required.of("请输入电话", r ->{
			return r.getAnswer() != null;
		},(r,db) ->{
			db.put(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Contact"), r.getAnswer());
			logger.info(groupid + ":" + r.getQuestion() + " = " + r.getAnswer());
		})};
		
		
		State<Context<CorpusService>> complete = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 7501666771663057899L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				for(Required r : requests) {
					if(r.predictor.test(r)) {
						r.acceptor.accept(r, u.content().account());
					}
				}
				u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Bossid"), openid());
				u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Order"), "true");
				u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Type"), "bar");
				u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Clients", "List", groupid), groupid);
				u.output(groupid + "配置完成");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return init;
			}

			@Override
			public String name() {
				return "确认";
			}

			@Override
			public boolean finish() {
				return true;
			}
			
		};
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -5301495101957849715L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String temp = t;
			u.output();
			String check = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "iBeacon", temp));
			if(!u.content().isNull(check)) {
				String clientKey = String.join(Const.delimiter, Const.Version.V1, "Info", "Clients", "List", temp);
				String exist = u.content().account().get(clientKey);
				if(!u.content().isNull(exist)) {
					String   bossid = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", temp, "Bossid"));
					if(openid().equals(bossid)) {
						u.output("你正在修改配置");
						groupid = temp;
					} else {
						u.output("已经被绑定，请联系管理员。");
					}
				} else {
					u.output("你正在进行配置");
					groupid = temp;
				}
			}
			if(groupid == null) {
				u.output("请输入摇钱树编号：");
			} else {
				if(require != null) {
					require.setAnswer(temp);
				}
				require = null;
				for(Required r : requests) {
					if(r.predictor.test(r)) {
						continue;
					} else {
						require = r;
						u.output(require.getQuestion());
						break;
					}
				}
				if(require == null) {
					for(Required r : requests) {
						u.output(r.getQuestion());
						u.output(r.getAnswer());
					}
					u.output("你已经配置完成，如果不正确请输入退出，没问题的话请输入：确认");
				}
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(complete.name().equals(t) && groupid != null) return complete;
			return alliance;
		}

		@Override
		public String name() {
			return "我要加盟";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	State<Context<CorpusService>> tree = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5080316144777548469L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("欢迎来到摇钱树");
			
			String moneytree = String.join(Const.delimiter, Const.Version.V1, openid(), "Info", "iBeacon", "Bind");
			List<String> ibeacons = new ArrayList<>();
			u.content().account().page(moneytree, moneytree, null, 100, new BiConsumer<String, String>() {
				
				@Override
				public void accept(String k, String v) {
					ibeacons.add(v);
				}
			});
			if(ibeacons.isEmpty()) {
				u.output("你还没有绑定摇钱树，你可以输入："+bind.name());
			} else {
				u.output("以下是你的摇钱树：");
				for(String ibeacon : ibeacons) {
					String customer = String.join(Const.delimiter, Const.Version.V1, openid(), "iBeacon", "Customer", ibeacon);
					Set<String> openids = new HashSet<>();
					u.content().account().page(customer, customer, null, Integer.MAX_VALUE, new BiConsumer<String, String>() {

						@Override
						public void accept(String k, String v) {
							openids.add(v);
						}
					});
					u.output("摇钱树:" + ibeacon + "的用户量:" + openids.size());
				}
			}
			
			String play = String.join(Const.delimiter, Const.Version.V1, openid(), "iBeacon", "Shake");
			List<String> trees = new ArrayList<>();
			u.content().account().page(play, play, null, 1000, new BiConsumer<String, String>() {
				
				@Override
				public void accept(String k, String v) {
					trees.add(v);
				}
			});
			if(trees.isEmpty()) {
				u.output("你还没有玩过摇钱树");
			} else {
				u.output("你玩过" + trees.size() + "棵摇钱树");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(bind.name().equals(t)) return bind;
			if(register.name().equals(t)) return register;
			if(alliance.name().equals(t)) return alliance;
			return init;
		}

		@Override
		public String name() {
			return "我的摇钱树";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	public MoneyTree(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state(enter);
	}

}
