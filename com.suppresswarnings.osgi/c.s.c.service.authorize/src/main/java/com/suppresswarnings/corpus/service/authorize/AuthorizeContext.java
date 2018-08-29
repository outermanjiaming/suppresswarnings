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
import com.suppresswarnings.corpus.service.wx.WXuser;

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
	
	State<Context<CorpusService>> enter = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3254062919413456936L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("授权流程：1.未授权用户扫码进入，获得授权申请码，交给管理员即可，授权通过之后可以使用对应的功能。2.管理员用户扫码进入或者输入'我要授权'进入，输入申请者的授权申请码，确认无误之后输入'同意'完成授权。");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			String adminKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Admin");
			String admin = u.content().account().get(adminKey);
			if(admin == null) {
				return others;
			} else {
				return administrator;
			}
		}

		@Override
		public String name() {
			return "授权入口";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	State<Context<CorpusService>> others = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7037487523889828495L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(CMD.equals(t)) {
				u.output("你不是管理员，无权使用该命令，请通过扫码进入。");
				return;
			}
			if(t.startsWith("SCAN_")) {
				qrScene = t.substring("SCAN_".length());
			}
			String[] args = qrScene.split("_");
			String func = args[1];
			String target = args[2];
			String authKey = String.join(Const.delimiter, Const.Version.V1, func, target, openid());
			String authorized = u.content().account().get(authKey);
			if(authorized != null) {
				u.output("你已被授权同意。");
				return;
			}
			String userKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Auth", qrScene);
			String authCode = u.content().account().get(userKey);
			if(authCode == null) {
				authCode = String.join("_", "Auth", time(), random()) + ":" + qrScene;
				String codeOpenIdKey = String.join(Const.delimiter, Const.Version.V1, authCode);
				u.content().account().put(codeOpenIdKey, openid());
				u.content().account().put(userKey, authCode);
			}
			u.output("请将授权申请码交给管理员：\n" + authCode);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "未授权用户";
		}

		@Override
		public boolean finish() {
			return true;
		}
		
	};
	State<Context<CorpusService>> administrator = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7037487523889828495L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("你是管理员，请输入授权申请码：");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return decide;
		}

		@Override
		public String name() {
			return "管理员授权";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	String authCode = null;
	String userId = null;
	String func   = null;
	String target = null;
	String name   = null;
	State<Context<CorpusService>> decide = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7715622108090665337L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			authCode = t;
			String[] args = authCode.split(":");
			qrScene = args[1];
			String[] funcTarget = qrScene.split("_");
			func = funcTarget[1];
			target = funcTarget[2];
			
			String codeOpenIdKey = String.join(Const.delimiter, Const.Version.V1, authCode);
			userId = u.content().account().get(codeOpenIdKey);
			if(userId == null) {
				u.output("授权申请码错误");
				authCode = null;
				return;
			}
			WXuser user = u.content().getWXuserByOpenId(userId);
			name = user.getNickname();
			
			String authKey = String.join(Const.delimiter, Const.Version.V1, func, target, userId);
			String authorized = u.content().account().get(authKey);
			if("Authorized".equals(authorized)) {
				u.output("该用户已经被授权：" + name + ", " + func + "," + target);
			} else { 
				u.output("你正在执行"+func+"授权" + target + "权限给'" + name + "', 请输入同意或者拒绝");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(authCode == null) {
				return administrator;
			}
			if("同意".equals(t)) {
				return agree;
			}
			return refuse;
		}

		@Override
		public String name() {
			return "查看授权申请码信息以做决定";
		}

		@Override
		public boolean finish() {
			return false;
		}};
	State<Context<CorpusService>> refuse = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1620890128174109709L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("你没有同意，对方授权失败。");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "拒绝授权";
		}

		@Override
		public boolean finish() {
			return false;
		}};
	State<Context<CorpusService>> agree = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5223665807439517873L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String authKey = String.join(Const.delimiter, Const.Version.V1, func, target, userId);
			u.content().account().put(authKey, "Authorized");
			String authInfoKey = String.join(Const.delimiter, Const.Version.V1, "Info", func, target, userId);
			u.content().account().put(authInfoKey, String.join(Const.delimiter, openid(), time()));
			u.output("授权完成");
			u.content().sendTxtTo(func + "_" + target, "授权同意(Code:" + target+")", userId);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "同意授权";
		}

		@Override
		public boolean finish() {
			return false;
		}};
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
			//TODO lijiaming 2018.8.24
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
		this.state = enter;
	}

}
