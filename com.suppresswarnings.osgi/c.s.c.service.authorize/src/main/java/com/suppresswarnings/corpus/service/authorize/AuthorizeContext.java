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

import java.util.HashMap;
import java.util.Map;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
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
	Map<String, AuthHandler> handlers;
	
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
			u.content().sendTxtTo("sell authority", "请购买相应的权限，购买权限之后，请将授权申请码交给管理员。购买地址：\n：http://suppresswarnings.com/payment.html?state=" + target, openid());
			u.output(authCode);
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
			String paidKey = String.join(Const.delimiter, Const.Version.V1, "Paid", target, userId);
			String paidState = u.content().account().get(paidKey);
			String paid = "未知";
			String authKey = String.join(Const.delimiter, Const.Version.V1, func, target, userId);
			String authorized = u.content().account().get(authKey);
			if(paidState == null) {
				paid = "未支付";
			} else {
				if("Paid".equals(paidState)) {
					paid = "已支付";
				} else {
					String keyState = String.join(Const.delimiter, Const.Version.V1, "Order", paidState, "State");
					String state = u.content().account().get(keyState);
					if("Paid".equals(state)) {
						paid = "已支付";
					} else {
						paid = state == null ? "未购买" : "未支付成功";
					}
				}
			}
			if("Authorized".equals(authorized)) {
				u.output("该用户已经被授权：" + name + "，" + target+ "(" + paid + ")");
			} else {
				u.output("你正在授权给" + name+", " + target + "(" + paid + ")");
				u.output("（如需购买该权限，请访问：http://suppresswarnings.com/payment.html?state=" + target);
				u.output("请输入「同意」或者「拒绝」");
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
			u.content().sendTxtTo(func + "_" + target, "拒绝授权(" + target + ")", userId);
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
			AuthHandler handler = handlers.get(target);
			if(handler != null) {
				boolean handled = handler.apply(u, target, openid(), userId, time(), random());
				logger.info("[AuthorizeContext] agree handle of " + target + ", apply: " + handled);
			}
			u.content().sendTxtTo(func + "_" + target, "授权同意(" + target + ")", userId);
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
		}
	};
	
	public AuthorizeContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = enter;
		handlers = new HashMap<>();
		DaigouAuthHandler handler = new DaigouAuthHandler();
		for(String func : DaigouAuthHandler.INTEREST) {
			handlers.put(func, handler);
		}
	}

}
