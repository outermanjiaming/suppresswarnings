package com.suppresswarnings.corpus.service.invite;

import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.QRCodeTicket;
import com.suppresswarnings.corpus.service.wx.WXnews;
import com.suppresswarnings.corpus.service.wx.WXuser;

public class InviteContext extends WXContext {
	public static final String CMD = "我要邀请";
	State<Context<CorpusService>> invite = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3542937629390320357L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(CMD.equals(t)) {
				logger.info("主动发起邀请：" + openid());
			} else {
				logger.info("[Invite] 邀请了用户: " + openid() + ", input: " + t);
				//can be invited 
				String qrScene = t.substring("SCAN_".length());
				String qrOpenidKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", qrScene, "Openid");
				String inviter = u.content().account().get(qrOpenidKey);
				if(inviter == null || "None".equals(inviter)) {
					logger.info("[Crew] inviter is null: " + qrOpenidKey);
					u.output("你遇到了一个没有主人的二维码");
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
						u.content().atUser(openid(), bossName+"说：非常高兴邀请你加入素朴网联");
						u.content().atUser(inviter, "第"+idx+"位朋友"+user().getNickname()+"被你邀请加入素朴网联，谢谢你的贡献！");
					} else {
						u.content().atUser(openid(), "你已经被"+bossName+"邀请加入素朴网联");
					}
				}
			}
			
			String code = "T_Invite_" + openid();
			String access = u.content().accessToken("Generate Temp QRCode");
			String json = u.content().qrCode(access, (int)TimeUnit.DAYS.toSeconds(7), "QR_STR_SCENE", code);
			u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "QRCode", code, "Openid"), openid());
			u.content().setGlobalCommand(code, CMD, openid(), time());
			Gson gson = new Gson();
			QRCodeTicket qrTicket = gson.fromJson(json, QRCodeTicket.class);
			WXnews news = new WXnews();
			news.setTitle("「素朴网联」你的专属二维码");
			news.setDescription("「素朴网联」全民行动，邀请好朋友加入素朴网联，邀请的朋友就是你的财富！");
			news.setUrl("http://suppresswarnings.com/vip.html?state=" + openid());
			news.setPicUrl("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + qrTicket.getTicket());
			String out = gson.toJson(news);
			u.output("news://" + out);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(CMD.equals(t) || t.startsWith("SCAN_")) {
				return invite;
			}
			return init;
		}

		@Override
		public String name() {
			return "邀请朋友";
		}

		@Override
		public boolean finish() {
			return true;
		}
	};
	public InviteContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state(invite);
	}

}
