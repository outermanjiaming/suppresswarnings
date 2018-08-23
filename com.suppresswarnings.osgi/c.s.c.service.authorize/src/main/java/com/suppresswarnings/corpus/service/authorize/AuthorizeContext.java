/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.authorize;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.QRCodeTicket;

/**
 * from a QR code to start authorizing, 
 *   for other user, ask them for token.
 *   for admin user, they can create a token for particular action, valid for a moment
 * @author lijiaming
 *
 */
public class AuthorizeContext extends WXContext {
	public static final String CMD = "我要授权";
	String qrScene = null;
	State<Context<CorpusService>> auth = new State<Context<CorpusService>>() {
		boolean isAdmin = false;
		/**
		 * 
		 */
		private static final long serialVersionUID = -3527086511539531515L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String adminKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Admin");
			String admin = u.content().account().get(adminKey);
			logger.info("[lijiaming] input: " + t);
			if(admin != null) {
				isAdmin = true;
				u.output("你是管理员，请输入对方申请码，进行授权");
			} else {
				if(t.startsWith("SCAN_")) {
					qrScene = t.substring("SCAN_".length());
				}
				
				String userKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Auth", qrScene);
				String existAuth = u.content().account().get(userKey);
				logger.info("[lijiaming] existAuth: " + existAuth);
				if(CMD.equals(t)) {
					String[] tokenAuth = existAuth.split(":");
					qrScene = existAuth.substring(tokenAuth[0].length() + 1);
				}
				
				String authKey = String.join(Const.delimiter, Const.Version.V1, openid(), qrScene);
				String authorized = u.content().account().get(authKey);
				logger.info("[lijiaming] authorized: " + authorized);
				if(authorized != null) {
					//TODO it's been authorized, can use this function
					// t is the authorization tail
					String qrKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", "P", "ShopAuth", openid());
					String qrSceneKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", "P", "ShopAuth", "Scene", openid());
					String exist = u.content().data().get(qrKey);
					String qrScene = null;
					Gson gson = new Gson();
					QRCodeTicket qrTicket = null;
					if(exist != null) {
						qrTicket = gson.fromJson(exist, QRCodeTicket.class);
						qrScene = u.content().data().get(qrSceneKey);
						logger.info("Use exist permanent qrcode: " + qrTicket.getUrl());
						u.output(String.format("Scene:%s\n二维码地址：\nhttps://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=%s\n任务完成，点击上面地址查看二维码。", qrScene, qrTicket.getTicket()));
					} else {
						u.output("请重新联系管理员进行授权，这是你的授权码：" + existAuth);
					}
				} else {
					if(existAuth != null) {
						u.output("请提供该申请码给管理员：\n" + existAuth);
					} else {
						String code = "Auth_" + random() + "_" + time() + ":" + qrScene;
						String codeKey = String.join(Const.delimiter, Const.Version.V1, code);
						u.content().token().put(codeKey, openid());
						u.content().account().put(userKey, code);
						u.output("你正在申请授权，请提供该申请码给管理员：\n" + code);
					}
				}
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(isAdmin) return accept;
			return auth;
		}

		@Override
		public String name() {
			return "授权码入口";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	/**
	 * the Admin accept the code to authorize the user.
	 * then create a QRCode into userOpenId
	 */
	State<Context<CorpusService>> accept = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5428342985810245754L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String codeKey = String.join(Const.delimiter, Const.Version.V1, t);
			String userOpenId = u.content().token().get(codeKey);
			if(userOpenId == null) {
				u.output("授权码错误或失效，请确认");
				return;
			}
			String[] tokenAuth = t.split(":");
			String authFunc = t.substring(tokenAuth[0].length() + 1);
			String authKey = String.join(Const.delimiter, Const.Version.V1, userOpenId, authFunc);
			u.content().account().put(authKey, openid());
			//create authorization code for userOpenId
			String qrKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", "P", "ShopAuth", userOpenId);
			String qrSceneKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", "P", "ShopAuth", "Scene", userOpenId);
			String exist = u.content().data().get(qrKey);
			String qrScene = null;
			Gson gson = new Gson();
			QRCodeTicket qrTicket = null;
			if(exist != null) {
				qrTicket = gson.fromJson(exist, QRCodeTicket.class);
				qrScene = u.content().data().get(qrSceneKey);
				logger.info("Use exist permanent qrcode: " + qrTicket.getUrl());
			} else {
				String sceneStr = String.join("_", "P", "ShopAuth", userOpenId);
				String access = u.content().accessToken("Generate Permanent QRCode");
				String json = u.content().qrCode(access, Integer.MAX_VALUE, "QR_LIMIT_STR_SCENE", sceneStr);
				u.content().data().put(qrKey, json);
				u.content().data().put(qrSceneKey, sceneStr);
				qrScene = sceneStr;
				String qrMyKey = String.join(Const.delimiter, Const.Version.V1, userOpenId, "QRCode", "P", time(), random());
				u.content().account().put(qrMyKey, json);
				qrTicket = gson.fromJson(json, QRCodeTicket.class);
				logger.info("Create permanent qrcode: " + qrTicket.getUrl());
			}
			String nowCommandKey = String.join(Const.delimiter, "Setting", "Global", "Command", qrScene.toLowerCase());
			u.content().account().put(nowCommandKey, "我要商铺客服");
			u.output(String.format("Scene:%s\n二维码地址：\nhttps://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=%s\n任务完成，点击上面地址查看二维码。", qrScene, qrTicket.getTicket()));
			u.appendLine("授权完成：\nopenid:" + userOpenId + "\nfunc:" + authFunc);
			u.content().sendTxtTo("Auth ShopAuth", "恭喜，授权完成，二维码：https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + qrTicket.getTicket(), userOpenId);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "接受授权";
		}

		@Override
		public boolean finish() {
			return false;
		}};
	
	public AuthorizeContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = auth;
	}

}
