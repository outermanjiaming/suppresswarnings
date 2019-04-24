package com.suppresswarnings.corpus.service.iot;

import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.QRCodeTicket;
import com.suppresswarnings.corpus.service.wx.WXnews;

public class IoTContext extends WXContext {
	public static final String CMD = "我要物联网";
	State<Context<CorpusService>> iot = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1981899786549046257L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String code = "T_Code_" + openid() + "_IOT_" + time();
			u.content().account().put(String.join(Const.delimiter, Const.Version.V1,  "AIIoT", "Code", code), openid());
			String accessToken = u.content().accessToken("iot qrcode");
			String json = u.content().qrCode(accessToken, (int)TimeUnit.DAYS.toMillis(30), "QR_STR_SCENE", code);
			String qrMyKey = String.join(Const.delimiter, Const.Version.V1, openid(), "AIIoT", "QRCode", code);
			u.content().account().put(qrMyKey, json);
			u.content().setGlobalCommand(code, "智能家居设备", openid(), time());
			Gson gson = new Gson();
			QRCodeTicket qrTicket = gson.fromJson(json, QRCodeTicket.class);
			u.content().account().put(String.join(Const.delimiter, Const.Version.V1,  openid(), "AIIoT", "Code", code), code);
			u.content().atUser(openid(), "@Override \npublic String code() {\nreturn '"+code+"';\n}\n//在实现Things接口的代码中增加这些代码");
			WXnews news = new WXnews();
			news.setTitle("点击查看物联网二维码");
			news.setDescription(code+"，保存二维码或收藏，长按扫码进行绑定");
			news.setUrl("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + qrTicket.getTicket());
			news.setPicUrl(news.getUrl());
			u.output("news://" + gson.toJson(news));
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(CMD.equals(t) || t.startsWith("SCAN_")) {
				return iot;
			}
			return init;
		}

		@Override
		public String name() {
			return "我要物联网二维码";
		}

		@Override
		public boolean finish() {
			return true;
		}
	};
	
	public IoTContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = iot;
	}

}
