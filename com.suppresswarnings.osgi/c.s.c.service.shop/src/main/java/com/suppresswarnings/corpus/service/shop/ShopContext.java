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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.shop.game.SameReplyAndBonus;
import com.suppresswarnings.corpus.service.wx.WXuser;

public class ShopContext extends WXContext {
	public static final String[] AUTH = {"Shop"};
	public static final String CMD = "我的商铺";
	public static final String Wait = "Wait";
	public static final String None = "None";
	AtomicBoolean finish = new AtomicBoolean(false);
	AtomicBoolean noneed = new AtomicBoolean(false);
	AtomicBoolean consumer = new AtomicBoolean(false);
	AtomicBoolean first = new AtomicBoolean(true);
	SameReplyAndBonus game;
	State<Context<CorpusService>> shop = new State<Context<CorpusService>>() {
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
			return finish.get();
		}
		
	};
	State<Context<CorpusService>> bind = new State<Context<CorpusService>>() {
		
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
			finish.compareAndSet(false, true);
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
			return finish.get();
		}
		
		
		
	};
	List<String> openids = new ArrayList<>();
	State<Context<CorpusService>> ad = new State<Context<CorpusService>>() {
		long limit = 100;
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			finish.compareAndSet(false, true);
			//1.check if I am Boss
			String keyBind = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "Bind");
			String binded = u.content().account().get(keyBind);
			if(binded == null || None.equals(binded)) {
				u.output("您还没有绑定商铺二维码，发广告给谁呀？");
				noneed.compareAndSet(false, true);
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
			if(!finish.get()) return ad;
			if(noneed.get()) return init;
			//get out
			finish.set(false);
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
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			finish.compareAndSet(false, true);
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
					if(game == null) {
						game = new SameReplyAndBonus(consumer, ownerid, openid(), qrScene, init, u);
						game.state().accept(t, u);
					} else {
						logger.info("重复扫码，" + game);
					}
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
			if(consumer.get()) {
				return game.state().apply(t, u);
			}
			if(!finish.get()) {
				return scan;
			}
			//get out
			finish.set(false);
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
	
	public ShopContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = shop;
	}

}
