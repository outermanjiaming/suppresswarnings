/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.shop;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.work.Quiz;
import com.suppresswarnings.corpus.service.wx.WXnews;
import com.suppresswarnings.corpus.service.wx.WXuser;

public class ShopContext extends WXContext {
	public static final String[] AUTH = {"Shop"};
	public static final String CMD = "我的商铺";
	public static final String Wait = "Wait";
	public static final String None = "None";
	State<Context<CorpusService>> shop = new State<Context<CorpusService>>() {
		boolean finish = false;
		/**
		 * 
		 */
		private static final long serialVersionUID = 1572458819181499561L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("您可以输入以下命令：");
			//check binded?
			u.output("    " + bind.name());
			//check eligible
			u.output("    " + ad.name());
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			//check exist shop assistant
			logger.info("input: " + t);
			if(t.startsWith("SCAN_")) return scan;
			
			if(!u.content().authrized(openid(), AUTH[0])) {
				return reject;
			}
			
			if(CMD.equals(t)) return shop;
			if(bind.name().equals(t)) return bind;
			if(ad.name().equals(t)) return ad;
			return shop;
		}

		@Override
		public String name() {
			return "我的商铺";
		}

		@Override
		public boolean finish() {
			return finish;
		}
		
	};
	State<Context<CorpusService>> bind = new State<Context<CorpusService>>() {
		boolean finish = false;
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String keyBind = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "Bind");
			String keyBindTime = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "BindTime");
			u.content().account().put(keyBind, Wait);
			u.content().account().put(keyBindTime, time());
			u.output("请扫描商铺二维码（请联系【素朴网联】工作人员申请商铺二维码）");
			finish = true;
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(name().equals(t)) return bind;
			return init;
		}

		@Override
		public String name() {
			return "绑定二维码";
		}

		@Override
		public boolean finish() {
			return finish;
		}
		
		
		
	};
	List<String> openids = new ArrayList<>();
	State<Context<CorpusService>> ad = new State<Context<CorpusService>>() {
		long limit = 100;
		boolean finish = false;
		boolean noneed = false;
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			finish = true;
			//1.check if I am Boss
			String keyBind = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "Bind");
			String binded = u.content().account().get(keyBind);
			if(binded == null || None.equals(binded)) {
				u.output("您还没有绑定商铺二维码，发广告给谁呀？");
				noneed = true;
			} else {
				String qrScene = binded;
				String head = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "Customer");
				List<String> userids = new ArrayList<>();
				u.content().account().page(head, head, null, limit, (k, v) ->{
					userids.add(v);
				});
				logger.info("[Shop ad] qrScene: " + qrScene);
				for(String userid : userids) {
					WXuser user = u.content().getWXuserByOpenId(userid);
					if(user != null && user.getSubscribe() == 1) {
						openids.add(userid);
					} else {
						logger.error("[Shop ad] user gone: " + user);
					}
				}
				//2.Y check binded, N go to 10 quiz
				logger.info("[Shop ad] has user size: " + openids.size());
				u.output("请输入要发送的广告内容（请仔细阅读好再发送）：");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(!finish) return ad;
			if(noneed) return init;
			//get out
			finish = false;
			return send;
		}

		@Override
		public String name() {
			return "发广告";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	State<Context<CorpusService>> send = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3735870889232510969L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String keyAdquota = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "Ad", "Quota");
			String keyLimit = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "Ad", "Limit");
			String quota = u.content().account().get(keyAdquota);
			String limit = u.content().account().get(keyLimit);
			if(limit == null) {
				limit = "10";
				u.content().account().put(keyLimit, limit);
			}
			if(quota == null) {
				quota = limit;
				u.content().account().put(keyAdquota, quota);
			}

			Integer value = Integer.valueOf(quota);
			value = value - 1;
			quota = "" + value;
			u.content().account().put(keyAdquota, quota);
			
			if(value >= 0) {
				String message = t;
				openids.stream().distinct().forEach(userid ->u.content().atUser(userid, message));
				u.output("您的广告已经飞奔到顾客手中！\n珍惜广告额度，请勿重复发送！\n剩余额度：" + quota);
			} else {
				u.output("您到广告额度已经不足，请联系【素朴网联】工作人员恢复！");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return shop;
		}

		@Override
		public String name() {
			return "发送";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	
	State<Context<CorpusService>> scan = new State<Context<CorpusService>>() {
		boolean finish = false;
		boolean isCustomer = false;
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			finish = true;
			String qrScene = t.substring("SCAN_".length());
			//1.check if I am Boss
			String keyBind = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "Bind");
			String binded = u.content().account().get(keyBind);
			
			String keyOwnerid = String.join(Const.delimiter, Const.Version.V1, "Shop", "Ownerid", qrScene);
			String ownerid = u.content().account().get(keyOwnerid);
			
			logger.info("[Shop scan] bind: " + binded);
			//not binded, customer
			if(binded == null || None.equals(binded)) {
				logger.info("[Shop scan] ownerid: " + ownerid);
				if(ownerid == null) {
					u.output("该商铺二维码还未绑定！");
				} else {
					String keyCustomer = String.join(Const.delimiter, Const.Version.V1, ownerid, "Shop", "Customer", openid());
					u.content().account().put(keyCustomer, openid());
					String keyHistoryCustomer = String.join(Const.delimiter, Const.Version.V1, ownerid, "Shop", "History", "Customer", time(), random());
					u.content().account().put(keyHistoryCustomer, openid());
					String keyUser = String.join(Const.delimiter, Const.Version.V1, "Shop", qrScene, "Customer", time(), random());
					u.content().account().put(keyUser, openid());
					isCustomer = true;
					String keyGoodid = String.join(Const.delimiter, Const.Version.V1, "Shop", qrScene, "Goodid");
					String goodid = u.content().account().get(keyGoodid);
					if(goodid != null) {
						WXuser owner = u.content().getWXuserByOpenId(goodid);
						String payment = "http://suppresswarnings.com/payment.html?state=" + goodid;
						Gson gson = new GsonBuilder().disableHtmlEscaping().create(); 
						WXnews news = new WXnews();
						news.setDescription("点击进入支付页面");
						news.setTitle("商铺收银台");
						news.setUrl(payment);
						news.setPicUrl(owner.getHeadimgurl());
						String newsJson = gson.toJson(news);
						//TODO async
						String ret = u.content().sendNewsTo("Shop payment", newsJson, openid());
						logger.info("[Shop payment] sent: " + ret);
					} else {
						logger.info("[Shop scan] goodid is null, qrScene: " + qrScene);
					}
					u.output("答题抽奖！请留下您的手机号，中奖后需要凭手机号领奖！");
				}
				return;
			}
			
			if(qrScene.equals(binded)) {
				if(ownerid == null) {
					u.content().account().put(keyOwnerid, openid());
				}
				String keyGoodid = String.join(Const.delimiter, Const.Version.V1, "Shop", qrScene, "Goodid");
				String goodid = u.content().account().get(keyGoodid);
				if(goodid == null) {
					goodid = openid();
					u.content().account().put(keyGoodid, goodid);
					saveSell(u.content(), goodid);
				}
				u.content().account().put(keyBind, None);
				u.output("您已成为该商铺的主人");
			} else if(Wait.equals(binded) && ownerid == null){
				//onetime flag, reset to None
				u.content().account().put(keyBind, None);
				//1.check binded by others
				String keyBindTime = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "BindTime");
				String bindTime = u.content().account().get(keyBindTime);
				logger.info("[Shop scan] bindTime: " + bindTime);
				try {
					long time = Long.parseLong(bindTime);
					if(System.currentTimeMillis() - time > TimeUnit.DAYS.toMillis(1)) {
						u.output("超过24小时，绑定二维码命令失效！请重新进入我的商铺->绑定二维码");
					} else {
						//2.bind
						u.content().account().put(keyBind, qrScene);
						u.content().account().put(keyBindTime, time());
						u.content().account().put(keyOwnerid, openid());
						String goodid = openid();
						String keyGoodid = String.join(Const.delimiter, Const.Version.V1, "Shop", qrScene, "Goodid");
						u.content().account().put(keyGoodid, goodid);
						
						saveSell(u.content(), goodid);
						u.output("绑定成功！");
					}
				} catch (Exception e) {
					u.output("绑定异常，请稍后重试！");
				}
			} else {
				//reset onetime flag
				u.content().account().put(keyBind, None);
				if(openid().equals(ownerid)){
					u.output("你已经绑定该商铺");
				} else {
					u.output("该二维码已经被其他商铺老板绑定了！");
				}
			}
			
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(isCustomer) return customer;
			if(!finish) {
				return scan;
			}
			//get out
			finish = false;
			return init;
		}

		@Override
		public String name() {
			return "扫描商铺二维码";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	private void saveSell(CorpusService service, String bossid) {
		service.account().put(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", bossid, "Price"), "100");
		service.account().put(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", bossid, "Reason"), "商铺支付");
		service.account().put(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", bossid, "What"), "面对面支付");
		service.account().put(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", bossid, "Type"), "Data");
		service.account().put(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", bossid, "Bossid"), bossid);
	}
	
	State<Context<CorpusService>> customer = new State<Context<CorpusService>>() {
		boolean finish = false;
		boolean first = true;
		Iterator<Quiz> quiz = null;
		List<KeyValue> qa = new ArrayList<>();
		Quiz next = null;
		int i=0;
		/**
		 * 
		 */
		private static final long serialVersionUID = 3937145686042893179L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			
			i ++;
			if(first) {
				String keyPhone = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "Contact", "Phone");
				String keyHistory = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "Contact", "History", "Phone", time());
				u.content().account().put(keyHistory, t);
				u.content().account().put(keyPhone, t);
				logger.info("[Shop customer] save phone: " + openid() + " => " + t);
				u.output("请回答10个问题，大奖等你来拿！");
				first = false;
				quiz = getQuiz(u.content(), 10);
			} else {
				String keyReply = String.join(Const.delimiter, next.getQuiz().key(), "Reply", openid(), time(), random());
				u.content().data().put(keyReply, t);
				qa.add(new KeyValue(next.getQuiz().value(), t));
			}
			if(quiz != null && quiz.hasNext()) {
				next = quiz.next();
				u.output(i + ". " + next.getQuiz().value());
			} else {
				finish = true;
				int lable = 0;
				int code = new Random().nextInt(49) + 1;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date = sdf.format(new Date());
				String keyRand = String.join(Const.delimiter, Const.Version.V1, "Shop", "Reply", date, openid(), time());
				StringBuffer sb = new StringBuffer();
				sb.append("抽奖号码：" + code).append("\n");
				String keyMycode = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "Lotus", date);
				u.content().account().put(keyMycode, ""+code);
				u.output("抽奖号码：" + code);
				for(KeyValue kv : qa) {
					lable ++;
					sb.append(lable + ".\t" + kv.key()).append("\n");
					u.output(lable + ".\t" + kv.key());
					sb.append("\t\t" + kv.value()).append("\n");
					u.output("\t\t" + kv.value());
				}
				u.content().data().put(keyRand, sb.toString());
				String keyCode = String.join(Const.delimiter, Const.Version.V1, "Shop", "Lotus", date, ""+code, openid());
				u.content().account().put(keyCode, openid());
				qa.clear();
				u.output("感谢您回复这些问题，请稍后留意我们的中奖通知！");
			}
		}
		
		public Iterator<Quiz> getQuiz(CorpusService service, int n) {
			List<Quiz> all = new ArrayList<>();
			all.addAll(service.assimilatedQuiz);
			Collections.shuffle(all);
			if(all.size() <= n) return all.iterator();
			else {
				List<Quiz> little = new ArrayList<>();
				for(int i=0;i<n;i++) {
					little.add(all.get(i));
				}
				return little.iterator();
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(finish) return init;
			return customer;
		}

		@Override
		public String name() {
			return "顾客回复问题";
		}

		@Override
		public boolean finish() {
			return finish;
		}
	};
	
	public ShopContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = shop;
	}

}
