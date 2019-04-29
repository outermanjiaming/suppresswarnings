package com.suppresswarnings.corpus.service.crew;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.QRCodeTicket;
import com.suppresswarnings.corpus.service.wx.WXnews;
import com.suppresswarnings.corpus.service.wx.WXuser;

public class CrewContext extends WXContext {
	public static final String CMD = "加入素朴网联";
	State<Context<CorpusService>> crew = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4072909633512715215L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String myvip = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "VIP", openid()));
			logger.info("[Crew] am i vip: " + myvip);
			if(myvip == null || "None".equals(myvip)) {
				logger.info("[Crew] user invited " + openid() + t);
				//can be invited 
				String qrScene = t.substring("SCAN_".length());
				String qrOpenidKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", qrScene, "Openid");
				String inviter = u.content().account().get(qrOpenidKey);
				if(inviter == null || "None".equals(inviter)) {
					logger.info("[Crew] inviter is null: " + qrOpenidKey);
					u.output("你居然自己发现了怎么加入素朴网联");
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
						u.content().atUser(openid(), "你已经被"+bossName+"邀请加入素朴网联");
					}
				}
			} else {
				logger.info("[Crew] you are VIP already: " + openid());
				synchronized (crew) {
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
					
					WXnews news = new WXnews();
					news.setTitle("尊敬的素朴网联VIP");
					news.setDescription("点击进入VIP页面，通过你的二维码关注素朴网联公众号的用户，就是你的财富！");
					news.setUrl("http://suppresswarnings.com/vip.html?state=" + qrTicket.getTicket());
					news.setPicUrl("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + qrTicket.getTicket());
					String json = gson.toJson(news);
					u.output("news://" + json);
				}
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(CMD.equals(t) || t.startsWith("SCAN_")) {
				return crew;
			}
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

	public CrewContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = crew;
	}

}
