package com.suppresswarnings.corpus.service.vip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.QRCodeTicket;
import com.suppresswarnings.corpus.service.wx.WXnews;
import com.suppresswarnings.corpus.service.wx.WXuser;

public class VIPContext extends WXContext {

	public static final String CMD = "我是vip";
	public static final String[] AUTH = {"VIP"};
	
	State<Context<CorpusService>> vip = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8973929943525506821L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			synchronized (vip) {
				//create vip qrcode
				String P_Func_Target = "P_VIP_" + openid();
				String qrKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", P_Func_Target);
				String qrSceneKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", P_Func_Target, "Scene");
				String qrOpenidKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", P_Func_Target, "Openid");
				String exist = u.content().account().get(qrKey);
				Gson gson = new Gson();
				QRCodeTicket qrTicket = null;
				if(exist == null || "None".equals(exist)) {
					String access = u.content().accessToken("Generate Permanent QRCode");
					String json = u.content().qrCode(access, Integer.MAX_VALUE, "QR_LIMIT_STR_SCENE", P_Func_Target);
					u.content().account().put(qrKey, json);
					u.content().account().put(qrSceneKey, P_Func_Target);
					u.content().account().put(qrOpenidKey, openid());
					String qrMyKey = String.join(Const.delimiter, Const.Version.V1, openid(), "VIP");
					u.content().account().put(qrMyKey, json);
					qrTicket = gson.fromJson(json, QRCodeTicket.class);
					logger.info("Create permanent qrcode: " + exist);
				} else {
					qrTicket = gson.fromJson(exist, QRCodeTicket.class);
					logger.info("Use exist permanent qrcode: " + exist);
				}
				
				String myvip = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "VIP", openid()));
				if(myvip == null || "None".equals(myvip)) {
					u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "VIP", openid()), openid());
				}
				u.content().setGlobalCommand(P_Func_Target, "加入素朴网联", openid(), time());
				
				String start = String.join(Const.delimiter, Const.Version.V1, openid(), "Crew");
				AtomicInteger val = new AtomicInteger(1);
				u.content().account().page(start, start, null, Integer.MAX_VALUE, (k,v)->{
					val.incrementAndGet();
				});
				
				WXnews news = new WXnews();
				news.setTitle("「素朴网联」专属vip二维码");
				news.setDescription("你邀请了"+ val.get() +"位朋友，这是你的财富！专属命令：我要验证码，我要领工资，我要发公告，我要激活码");
				news.setUrl("http://suppresswarnings.com/vip.html?state=" + openid());
				news.setPicUrl("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + qrTicket.getTicket());
				String json = gson.toJson(news);
				u.output("news://" + json);
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			logger.info("[VIP] input: " + t);
			if(t.toLowerCase().startsWith("我是v")) {
				return vip;
			}
			if(t.startsWith("我要验证码")) {
				return captcha;
			}
			if(activation.name().equals(t)) {
				return activation;
			}
			if(salary.name().equals(t)) {
				return salary;
			}
			if(alert.name().equals(t)) {
				return alert;
			}
			if(t.length() > 4 && Pattern.compile("\\d+").matcher(t.substring(0, 4)).matches()) {
				return update;
			}
			if(t.startsWith("SCAN_")) {
				return invite;
			}
			return init;
		}

		@Override
		public String name() {
			return CMD;
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<CorpusService>> activation = new State<Context<CorpusService>>() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -8111131627641046588L;

		private String create(Context<CorpusService> u) {
			String myKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Code", "Activate", "Scene");
			String qrScene = "T_Code_Activate_" + time() + random();
			u.content().account().put(myKey, qrScene);
			String openidKey = String.join(Const.delimiter, Const.Version.V1, "Openid", "Activate", "Scene", qrScene);
			u.content().account().put(openidKey, openid());
			String expire = String.join(Const.delimiter, Const.Version.V1, "Expire", "Activate", "Scene", qrScene);
			long expired = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(23);
			u.content().account().put(expire, ""+expired);
			u.content().setGlobalCommand(qrScene, "我要激活码", qrScene, "" + System.currentTimeMillis());
			String accessToken = u.content().accessToken("VIP 获取激活码二维码");
			String json = u.content().qrCode(accessToken, (int)TimeUnit.HOURS.toSeconds(23), "QR_STR_SCENE", qrScene);
			String jsonKey = String.join(Const.delimiter, Const.Version.V1, "Json", "Activate", "Scene", qrScene);
			u.content().account().put(jsonKey, json);
			return json;
		}
		
		public void toNews(String json, Context<CorpusService> u) {
			Gson gson = new Gson();
			QRCodeTicket qrTicket = gson.fromJson(json, QRCodeTicket.class);
			WXnews news = new WXnews();
			news.setTitle("「素朴网联」激活码的二维码");
			news.setDescription("用户扫码即可获得激活码，激活码有效期24小时，该二维码24小时后失效");
			news.setUrl("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + qrTicket.getTicket());
			news.setPicUrl("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + qrTicket.getTicket());
			u.output("news://" + gson.toJson(news));
		}

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String myKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Code", "Activate", "Scene");
			String exist = u.content().account().get(myKey);
			if(u.content().isNull(exist)) {
				String json = create(u);
				toNews(json, u);
			} else {
				String expire = String.join(Const.delimiter, Const.Version.V1, "Expire", "Activate", "Scene", exist);
				long expired = Long.parseLong(expire);
				if(expired - System.currentTimeMillis() < 0) {
					String json = create(u);
					toNews(json, u);
				} else {
					String jsonKey = String.join(Const.delimiter, Const.Version.V1, "Json", "Activate", "Scene", exist);
					String json = u.content().account().get(jsonKey);
					toNews(json, u);
				}
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(name().equals(t)) {
				return activation;
			}
			return init;
		}

		@Override
		public String name() {
			return "我要激活码";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<CorpusService>> captcha = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4072909633512715215L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String key = String.join(Const.delimiter, Const.Version.V1, "Info", "CaptchaList");
			u.content().account().page(key, key, null, 1000, (k, v) ->{
				String number = v;
				String text = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Captcha", number, "Text"));
				String stamp = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Captcha", number, "Time"));
				long report = Long.valueOf(stamp);
				if(System.currentTimeMillis() - report > TimeUnit.HOURS.toMillis(1)) {
					u.output("一个小时内没有收到验证码\n手机号：" + number);
					return;
				}
				SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
				u.output("当前时间：" + format.format(new Date()) + "\n更新时间：" + format.format(new Date(report)) +"\n手机号：" + number+ "\n验证码：" + text);
			});
			u.output("如果没有验证码，请确认手机号没错，输入：刷新");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if("刷新".equals(t)) {
				return captcha;
			}
			return init;
		}

		@Override
		public String name() {
			return "验证码";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	State<Context<CorpusService>> salary = new State<Context<CorpusService>>() {
		int unit = 30;
		AtomicBoolean first = new AtomicBoolean(true);
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5881707174758294449L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(!first.compareAndSet(true, false)) {
				u.output("不要重复提交，没有用的");
				return;
			}

			String lasttime = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, openid(), "Salary", "Lasttime"));
			if(lasttime == null || "None".equals(lasttime)) {
				logger.info("[VIP salary] 用户首次领工资: " + user());
			} else {
				long lastt = Long.parseLong(lasttime);
				if(System.currentTimeMillis() - lastt < TimeUnit.HOURS.toMillis(2)) {
					u.output("你不要这么频繁来领工资，稍等一下咯");
					u.output("你可以多邀请好朋友关注我们公众号");
					return;
				}
			}
			
			u.content().threadpool.submit(() ->{
				StringBuffer output = new StringBuffer();
				String start = String.join(Const.delimiter, Const.Version.V1, openid(), "Crew");
				AtomicInteger all = new AtomicInteger(0);
				AtomicInteger cancel = new AtomicInteger(0);
				AtomicInteger pay = new AtomicInteger(0);
				List<String> openids = new ArrayList<>();
				u.content().account().page(start, start, null, Integer.MAX_VALUE, (k,v)->{
					all.incrementAndGet();
					openids.add(v);
				});
				openids.forEach(userid-> {
					WXuser one = u.content().getWXuserByOpenId(userid);
					if(one == null || one.getSubscribe() == 0) {
						cancel.incrementAndGet();
					}
				});
				
				String paidHistoryStart = String.join(Const.delimiter, Const.Version.V1, openid(), "Salary", "History");
				u.content().account().page(paidHistoryStart, paidHistoryStart, null, Integer.MAX_VALUE, (k,v)->{
					int paid = Integer.parseInt(v);
					pay.addAndGet(paid);
				});
				String money = "(邀请总人数 %s - 取消关注人数 %s - 已结算人数 %s ) * 单价 %s\n";
				logger.info(String.format(money + " %s", all.get(), cancel.get(), pay.get(), unit, user().toString()));
				
				String paidKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Salary", "Paid");
				String paid = u.content().account().get(paidKey);
				
				String paidLastKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Salary", "Last");
				String last = u.content().account().get(paidLastKey);
				
				if(paid == null || "None".equals(paid)) {
					output.append("恭喜你解锁新功能：我要领工资。");
				} else {
					output.append("你上次领工资获得了" + last + "分，看看这次能领多少分：");
				}
				output.append("工资计算器：" + String.format(money, all.get(), cancel.get(), pay.get(), unit));
				
				int left = all.get() - cancel.get() - pay.get();
				if(left <= 1) {
					output.append("这段时间你邀请的人数太少了，先积累多一点了，再来领工资吧！加油哦～");
				} else {
					int cent = left * unit;
					
					u.content().account().put(paidLastKey, "" + cent);
					u.content().account().put(paidKey, "" + left);
					u.content().account().put(String.join(Const.delimiter, Const.Version.V1, openid(), "Salary", "Lasttime"), time());
					u.content().account().put(String.join(Const.delimiter, Const.Version.V1, openid(), "Salary", "History", time()), "" + left);
					String approve = u.content().requestApprove(openid(), cent);
					output.append("本次总工资：" + cent + "分，继续加油哦～");
					output.append(approve);
				}
				
				u.content().atUser(openid(), output.toString());
			});
			u.output("已经提交了请求，稍后将收到消息，请勿重复提交，避免误操作清零。");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(name().equals(t)) {
				return salary;
			}
			return init;
		}

		@Override
		public String name() {
			return "我要领工资";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	State<Context<CorpusService>> invite = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4072909633512715215L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String myvip = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "VIP", openid()));
			logger.info("[VIP] myvip: " + myvip);
			if(myvip == null || "None".equals(myvip)) {
				//can be invited 
				String qrScene = t.substring("SCAN_".length());
				String qrOpenidKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", qrScene, "Openid");
				String inviter = u.content().account().get(qrOpenidKey);
				if(inviter == null || "None".equals(inviter)) {
					logger.info("[VIP] inviter is null: " + qrOpenidKey);
					vip.accept(t, u);
				} else {
					String bossKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Boss");
					String vipBoss = String.join(Const.delimiter, Const.Version.V1, "Boss", inviter);
					String counterBoss = String.join(Const.delimiter, Const.Version.V1, "Counter", "Boss", inviter);
					u.content().account().put(vipBoss, time());
					
					String boss = u.content().account().get(bossKey);
					WXuser user = u.content().getWXuserByOpenId(inviter);
					String bossName = "名字保密";
					if(user != null) bossName = user.getNickname();
					if(boss == null || "None".equals(boss)) {
						u.content().account().put(bossKey, inviter);
						String crewKey = String.join(Const.delimiter, Const.Version.V1, inviter, "Crew", openid());
						u.content().account().put(crewKey, openid());
						String start = String.join(Const.delimiter, Const.Version.V1, inviter, "Crew");
						String idx = u.content().increment(counterBoss, start);
						u.content().atUser(openid(), bossName+"说：很荣幸，你被我邀请加入素朴网联");
						u.content().atUser(inviter, "第"+idx+"位朋友"+user().getNickname()+"被你邀请加入素朴网联，邀请的用户成为你的资产，未来广告收益会给你分成，可以提现！");
					} else {
						u.content().atUser(openid(), "你已经被"+bossName+"邀请成为素朴网联VIP了");
					}
					u.content().atUser(openid(), "你随时可以输入以下命令查看自己的邀请二维码："+CMD);
				}
			}
			
			logger.info("[VIP] you are VIP already: " + openid());
			vip.accept(t, u);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "邀请关注";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	
	State<Context<CorpusService>> update = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3851369518154268501L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			
			String[] textNumber = t.split("\\s+");
			if(textNumber.length == 1) textNumber = new String[] {t, "13727872757"};
			String text = textNumber[0];
			String number = textNumber[1];
			String key = String.join(Const.delimiter, Const.Version.V1, "Info", "CaptchaList", number);
			u.content().account().put(key, number);
			u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Captcha", number, "Text"), text);
			u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Captcha", number, "Time"), time());
			
			u.content().token().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Captcha", number, time()), text);
			u.output("请按如下格式输入：\n${验证码} [${手机号}]\n比如：544464 13727872757");
			u.output("已记录：" + text + " " + number);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(t.length() > 4 && Pattern.compile("\\d+").matcher(t.substring(0, 4)).matches()) {
				return update;
			}
			return init;
		}

		@Override
		public String name() {
			return "更新验证码";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	State<Context<CorpusService>> alert = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1352576423633422537L;
		String ad = null;
		State<Context<CorpusService>> send = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 4871517349629706826L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(ad == null) {
					u.output("你还没有设置公告");
				} else {
					openids.forEach(userid -> {
						u.content().atUser(userid, ad);
					});
					u.output("已经向"+ openids.size() + "位用户发送了公告");
					u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Alert", "Info", openid(), time()), ad);
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return init;
			}

			@Override
			public String name() {
				return "发送公告";
			}

			@Override
			public boolean finish() {
				return true;
			}
			
		};
		State<Context<CorpusService>> info = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1679422214009820876L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss");
				String date = sdf.format(new Date());
				ad = "「素朴网联公告：" + t + "，时间：" + date + "」";
				u.output(ad);
				u.output("请仔细阅读你要发送的公告");
				u.output("如果没有问题，请输入：发送");
				u.output("如果放弃发送，请输入：放弃");
				u.output("如果要修改，请重新输入公告");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("发送".equals(t)) {
					return send;
				}
				if("放弃".equals(t)) {
					return init;
				}
				return info;
			}

			@Override
			public String name() {
				return "公告内容";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		List<String> openids = null;
		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(openids == null) {
				String start = String.join(Const.delimiter, Const.Version.V1, openid(), "Crew");
				AtomicInteger all = new AtomicInteger(0);
				AtomicInteger cancel = new AtomicInteger(0);
				List<String> userids = new ArrayList<>();
				u.content().account().page(start, start, null, Integer.MAX_VALUE, (k,v)->{
					all.incrementAndGet();
					userids.add(v);
				});
				if(userids.isEmpty()) {
					
				} else {
					openids = new ArrayList<>();
				}
				userids.forEach(userid-> {
					WXuser one = u.content().getWXuserByOpenId(userid);
					if(one == null || one.getSubscribe() == 0) {
						cancel.incrementAndGet();
					} else {
						openids.add(userid);
					}
				});
			}
			
			u.output("请输入公告，将发送给" + openids.size() + "个用户：");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(name().equals(t)) {
				return alert;
			}
			
			return info;
		}

		@Override
		public String name() {
			return "我要发公告";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	public VIPContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = vip;
	}
}
