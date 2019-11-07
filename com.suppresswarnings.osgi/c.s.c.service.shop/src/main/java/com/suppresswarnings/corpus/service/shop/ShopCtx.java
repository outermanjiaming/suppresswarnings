package com.suppresswarnings.corpus.service.shop;

import java.io.File;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.http.CallableGet;
import com.suppresswarnings.corpus.service.http.CallablePost;
import com.suppresswarnings.corpus.service.http.PostDownload;
import com.suppresswarnings.corpus.service.wx.WXnews;

public class ShopCtx extends WXContext {
	public ShopCtx(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state(enter);
		this.token = Long.toHexString(new Random().nextLong());
		CallablePost post = new CallablePost("http://localhost:8880/spring-cloud-account/account/register/" + openid() + "?token=" + token, "");
		try {
			String ret = post.call();
			logger.info("register ok: " + ret);
		} catch (Exception e) {
			logger.error("fail to register", e);
		}
	}
	public static final String[] AUTH = {"Shop"};
	public static final String CMD = "我的商铺";
	public String token = null;
	public Gson gson = new Gson();
	State<Context<CorpusService>> joinnearby = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1967534736276669723L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String[] args = t.split("\\$");
			//String qrScene = "T_Joinnearby_$" + offerid + "$" + referid + "$" + dealid;
			String offerid = args[1];
			String referid = args[2];
			String dealid = args[3];
			String url = "http://localhost:8880/spring-cloud-stream/offer/accept/" + openid() + "?token=" + token + "&offerid=" + offerid + "&referid=" + referid + "&dealid=" + dealid;
			CallableGet get = new CallableGet(url);
			try {
				String ret = get.call();
				Reply reply = gson.fromJson(ret, Reply.class);
				if(reply.getState() == 200) {
					u.output("核销成功" );
				} else {
					u.output("核销失败:" + reply.getMsg());
				}
			} catch (Exception e) {
				u.output("核销异常:" + e.getMessage());
			}
			u.output("感谢您为顾客核销了优惠！");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "核销";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	State<Context<CorpusService>> shutdown = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8600240224640966258L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			CallableGet get = new CallableGet("http://localhost:8880/spring-cloud-stream/offer/shutdown/" + openid() + "?token=" + token);
			try {
				String ret = get.call();
				Reply reply = gson.fromJson(ret, Reply.class);
				if(reply.getState() == 200) {
					u.output("关闭成功" );
				} else {
					u.output("关闭失败:" + reply.getMsg());
				}
			} catch (Exception e) {
				u.output("关闭异常:" + e.getMessage());
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "关闭";
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
		private static final long serialVersionUID = 2948356478756866456L;
		
		
		@Override
		public void accept(String t, Context<CorpusService> u) {
			shop.accept(t, u);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(t.startsWith("SCAN_")) {
				String scene = t.substring("SCAN_".length());
				if(scene.startsWith("T_Joinnearby_")) {
					return joinnearby;
				} else if(scene.startsWith("P_Shop_Offer")) {
					return publish;
				} else if(scene.startsWith("P_Shop_Refer")) {
					return recommend;
				}
			}
			return shop;
		}

		@Override
		public String name() {
			return CMD;
		}

		@Override
		public boolean finish() {
			return false;
		}};
	State<Context<CorpusService>> shop = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8600240224640966258L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("欢迎加盟素朴网联！顾客消费之后，你还可以免费送给用户很多优惠，让顾客更喜欢你们；你也可以发布优惠，顾客就会找上门来消费。你可以输入以下命令：");
			u.output(alliance.name());
			u.output(publish.name());
			u.output(recommend.name());
			u.output(shutdown.name());
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(alliance.name().equals(t)) return alliance;
			if(publish.name().equals(t)) return publish;
			if(recommend.name().equals(t)) return recommend;
			if(shutdown.name().equals(t)) return shutdown;
			if(t.startsWith("SCAN_")) {
				String scene = t.substring("SCAN_".length());
				if(scene.startsWith("T_Joinnearby_")) {
					return joinnearby;
				} else if(scene.startsWith("P_Shop_Offer")) {
					return publish;
				} else if(scene.startsWith("P_Shop_Refer")) {
					return recommend;
				}
			}
			return shop;
		}

		@Override
		public String name() {
			return "商铺菜单";
		}

		@Override
		public boolean finish() {
			return false;
		}};
		
		/**
		 * CallableGet get = new CallableGet("http://localhost:8880/spring-cloud-stream/offer/publish/%s?token=%s&what=%s&type=%s&note=%s&accept=%s&paid=%s", 
						openid(),
						"token1",
						"服务端",
						"bar",
						"这是一次测试",
						"drink,coffee",
						"55.00");
		 */
		State<Context<CorpusService>> publish  = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 6575015910538833137L;
			Required require = null;
			Required[] requests = {Required.of("what", "请输入优惠标题", r ->{
				return r.getAnswer() != null;
			},(r,db) ->{
				logger.info(openid() + ":" + r.key() + " = " + r.getAnswer());
			}), Required.of("image", "请上传图片", r ->{
				return r.getAnswer() != null && r.getAnswer().startsWith("IMAGE_");
			},(r,db)  ->{
				String icon = r.getAnswer().substring("IMAGE_".length());
				r.setAnswer(icon);
				logger.info(openid() + ":" + r.key() + " = " + r.getAnswer());
			}),Required.of("paid", "请输入门槛金额", r ->{
				if(r.getAnswer() == null) return false;
				String ans = r.getAnswer();
				try {
					Double d = Double.valueOf(ans);
					return (d > -0.001 && d < 99999);
				} catch(Exception e) {
					return false;
				}
			},(r,db) ->{
				logger.info(openid() + ":" + r.key() + " = " + r.getAnswer());
			}),Required.of("note", "请输入备注", r ->{
				return r.getAnswer() != null;
			},(r,db) ->{
				logger.info(openid() + ":" + r.key() + " = " + r.getAnswer());
			})};
			
			
			State<Context<CorpusService>> complete = new State<Context<CorpusService>>() {


				/**
				 * 
				 */
				private static final long serialVersionUID = -454553130188463125L;

				@Override
				public void accept(String t, Context<CorpusService> u) {
					String url = "http://localhost:8880/spring-cloud-stream/offer/publish/" + openid() + "?";
					StringBuffer sb = new StringBuffer();
					for(Required r : requests) {
						if(r.predictor.test(r)) {
							r.acceptor.accept(r, u.content().account());
							sb.append(r.key() + "=" + r.getAnswer() + "&");
						}
					}
					sb.append("token=" + token);
					String type = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Joinnearby", openid(), "Type"));
					sb.append("&type=" + type);
					String accept = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Joinnearby", openid(), "Accept"));
					sb.append("&accept=" + accept);
					CallableGet get = new CallableGet(url + sb.toString());
					try {
						String ret = get.call();
						Reply reply = gson.fromJson(ret, Reply.class);
						if(reply.getState() == 200) {
							u.output("发布成功" );
						} else {
							u.output("发布失败:" + reply.getMsg());
						}
					} catch (Exception e) {
						u.output("发布异常:" + e.getMessage());
					}
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
			@Override
			public void accept(String t, Context<CorpusService> u) {
				String temp = t;
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
						u.output(r.getQuestion() + "✅");
					}
					u.output("请核对以上信息，\n不正确请输入：退出，\n正确请输入：确认");
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if(complete.name().equals(t)) return complete;
				return publish;
			}

			@Override
			public String name() {
				return "发布";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		State<Context<CorpusService>> alliance = new State<Context<CorpusService>>() {
			Required require = null;
			Required[] requests = {Required.of("name", "请输入名称", r ->{
				return r.getAnswer() != null;
			},(r,db) ->{
				db.put(String.join(Const.delimiter, Const.Version.V1, "Joinnearby", openid(), "Name"), r.getAnswer());
				logger.info(openid() + ":" + r.getQuestion() + " = " + r.getAnswer());
			}), Required.of("icon", "请上传图片", r ->{
				return r.getAnswer() != null && r.getAnswer().startsWith("IMAGE_");
			},(r,db)  ->{
				String icon = r.getAnswer().substring("IMAGE_".length());
				r.setAnswer(icon);
				db.put(String.join(Const.delimiter, Const.Version.V1, "Joinnearby", openid(), "Icon"), icon);
				logger.info(openid() + ":" + r.getQuestion() + " = " + r.getAnswer());
			}),Required.of("location", "请发送位置", r ->{
				return r.getAnswer() != null && r.getAnswer().startsWith("LOC_");
			},(r,db)  ->{
				String location = r.getAnswer();
				String[] args = location.substring("LOC_".length()).split(";");
				db.put(String.join(Const.delimiter, Const.Version.V1, "Joinnearby", openid(), "Latitude"), args[0]);
				db.put(String.join(Const.delimiter, Const.Version.V1, "Joinnearby", openid(), "Longtitude"), args[1]);
				db.put(String.join(Const.delimiter, Const.Version.V1, "Joinnearby", openid(), "Location"), args[2]);
				String loc = args[0] + ";" +  args[1];
				String addr = args[2];
				r.setAnswer(loc + "&address=" + addr);
				logger.info(openid() + ":" + r.key() + " = " + r.getAnswer());
			}),Required.of("contact", "请输入电话", r ->{
				return r.getAnswer() != null;
			},(r,db) ->{
				db.put(String.join(Const.delimiter, Const.Version.V1, "Joinnearby", openid(), "Contact"), r.getAnswer());
				logger.info(openid() + ":" + r.getQuestion() + " = " + r.getAnswer());
			}),Required.of("type", "请输入商铺类型", r ->{
				return r.getAnswer() != null;
			},(r,db) ->{
				db.put(String.join(Const.delimiter, Const.Version.V1, "Joinnearby", openid(), "Type"), r.getAnswer());
				logger.info(openid() + ":" + r.getQuestion() + " = " + r.getAnswer());
			}),Required.of("what", "请输入商铺特色", r ->{
				return r.getAnswer() != null;
			},(r,db) ->{
				db.put(String.join(Const.delimiter, Const.Version.V1, "Joinnearby", openid(), "What"), r.getAnswer());
				logger.info(openid() + ":" + r.getQuestion() + " = " + r.getAnswer());
			}),Required.of("accept", "请输入接受的商铺类型", r ->{
				return r.getAnswer() != null;
			},(r,db) ->{
				db.put(String.join(Const.delimiter, Const.Version.V1, "Joinnearby", openid(), "Accept"), r.getAnswer());
				logger.info(openid() + ":" + r.getQuestion() + " = " + r.getAnswer());
			})};
			
			
			State<Context<CorpusService>> complete = new State<Context<CorpusService>>() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 7501666771663057899L;

				@Override
				public void accept(String t, Context<CorpusService> u) {
					String url = "http://localhost:8880/spring-cloud-stream/offer/shop/" + openid() + "?";
					StringBuffer sb = new StringBuffer();
					for(Required r : requests) {
						if(r.predictor.test(r)) {
							r.acceptor.accept(r, u.content().account());
							sb.append(r.key() + "=" + r.getAnswer() + "&");
						}
					}
					sb.append("token=" + token);
					CallableGet get = new CallableGet(url + sb.toString());
					try {
						String ret = get.call();
						Reply reply = gson.fromJson(ret, Reply.class);
						if(reply.getState() == 200) {
							u.output("配置成功" );
						} else {
							u.output("配置失败:" + reply.getMsg());
						}
					} catch (Exception e) {
						u.output("配置异常:" + e.getMessage());
					}
					u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Joinnearby", "List", openid()), openid());
					u.output("配置完成");
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
						u.output(r.getQuestion() + "✅");
					}
					u.output("请核对以上信息，\n不正确请输入：退出，\n正确请输入：确认");
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if(complete.name().equals(t)) return complete;
				return alliance;
			}

			@Override
			public String name() {
				return "设置";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> recommend = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -4762663422326042568L;
			State<Context<CorpusService>> complete = new State<Context<CorpusService>>() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 5931747503491849215L;

				@Override
				public void accept(String t, Context<CorpusService> u) {
					String paid = t;
					String type = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Joinnearby", openid(), "Type"));
					String url = "http://localhost:8880/spring-cloud-stream/offer/refer/" + openid() + "?paid=" + paid + "&type=" + type + "&token=" + token;
					CallableGet get = new CallableGet(url);
					try {
						String ret = get.call();
						Reply reply = gson.fromJson(ret, Reply.class);
				    	if(reply.getState() == 200) {
				    		Map<String, Object> refer = (Map) reply.getData();
					    	int id = Double.valueOf(String.valueOf(refer.get("id"))).intValue();
					    	String code = String.valueOf(refer.get("code"));
					    	String json = "{\"scene\":\""+id+";"+code+";"+paid+"\"}";
					    	String access_token = u.content().accessToken("wx662073d1ec0e14f6");
					    	String getwxacodeunlimit = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + access_token;
					    	String downloadFolder = "download/";
							String saveTo = System.getProperty("path.html") + downloadFolder;
							String name = openid() + "." + time() + ".jpg";
					    	PostDownload post = new PostDownload(getwxacodeunlimit, 2097152, saveTo, name, json);
					    	File file = post.call();
					    	logger.info("[recommend] should delete file: " + file.getAbsolutePath());
					    	WXnews news = new WXnews();
							news.setTitle("推荐超级优惠给顾客");
							news.setDescription("顾客使用微信扫一扫即可获取超级优惠列表！");
							news.setUrl("https://suppresswarnings.com/" + downloadFolder + name);
							news.setPicUrl("https://suppresswarnings.com/" + downloadFolder + name);
							String newjson = gson.toJson(news);
							u.output("news://" + newjson);
				    	} else {
				    		u.output("推荐失败：" + reply.getMsg());
				    	}
						
					} catch(Exception e) {
						logger.error("推荐异常", e);
						u.output("推荐异常：" + e.getMessage());
					}
				}

				@Override
				public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
					return init;
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
				u.output("您正在给顾客推荐超级优惠，系统根据消费金额给予最佳优惠，请输入用户消费的金额：");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				try {
					Double d = Double.valueOf(t);
					if(d > -0.001 && d < 99999) {
						return complete;
					}
				} catch(Exception e) {
				}
				return recommend;
			}

			@Override
			public String name() {
				return "推荐";
			}

			@Override
			public boolean finish() {
				return false;
			}};
			
}
