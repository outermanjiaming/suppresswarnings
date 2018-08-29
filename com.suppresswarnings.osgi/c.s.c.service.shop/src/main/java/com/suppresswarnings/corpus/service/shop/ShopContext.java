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

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.QRCodeTicket;

public class ShopContext extends WXContext {
	public static final String CMD = "我要商铺客服";
	String func = null;
	String userOpenId = null;
	List<KeyValue> quiz = new ArrayList<KeyValue>();
	State<Context<CorpusService>> shop = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1572458819181499561L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("如果需要申请商铺客服二维码请联系我们，将为您分配工作人员。谢谢");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			//check exist shop assistant
			logger.info("input: " + t);
			if(t.startsWith("SCAN_")) {
				func = t.substring("SCAN_".length());
				userOpenId = func.substring("p_shopauth_".length());
				logger.info("user openid: " + userOpenId);
				return create;
			} 
			return init;
		}

		@Override
		public String name() {
			return "创建商铺客服";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	State<Context<CorpusService>> create = new State<Context<CorpusService>>() {
		final int INITIAL = -1, ANSWER = 1, QUESTION = 2, DONE = 3;
		int status = INITIAL;
		Iterator<KeyValue> itr = null;
		KeyValue current = null;
		String qrScene = null;
		String qrcode = null;
		/**
		 * 
		 */
		private static final long serialVersionUID = 2261677273563400377L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(status == INITIAL) {
				//start create shop qr
				//create authorization code for userOpenId
				String myShopKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop");
				String qrKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", "P", "Shop", openid());
				String qrSceneKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", "P", "Shop", "Scene", openid());
				String exist = u.content().data().get(qrKey);
				Gson gson = new Gson();
				QRCodeTicket qrTicket = null;
				if(exist != null) {
					qrTicket = gson.fromJson(exist, QRCodeTicket.class);
					qrScene = u.content().data().get(qrSceneKey);
					logger.info("Use exist permanent qrcode: " + qrTicket.getUrl());
				} else {
					qrScene = String.join("_", "P", "Shop", openid(), time(), random());
					String access = u.content().accessToken("Generate Permanent QRCode");
					String json = u.content().qrCode(access, Integer.MAX_VALUE, "QR_LIMIT_STR_SCENE", qrScene);
					String shopKey = String.join(Const.delimiter, Const.Version.V1, "Shop", qrScene);
					u.content().data().put(qrKey, json);
					u.content().data().put(qrSceneKey, qrScene);
					Map<String, String> map = new HashMap<>();
					map.put("openid", openid());
					map.put("agentid", userOpenId);
					map.put("time", time());
					map.put("random", random());
					map.put("qrcodejson", json);
					u.content().data().put(shopKey, gson.toJson(map));
					
					String shopOpenIdKey = String.join(Const.delimiter, Const.Version.V1, "Shop", "OpenId", qrScene);
					u.content().account().put(shopOpenIdKey, openid());
					
					String qrMyKey = String.join(Const.delimiter, Const.Version.V1, openid(), "QRCode", "P", "Shop", time(), random());
					u.content().account().put(qrMyKey, json);
					u.content().account().put(myShopKey, qrScene);
					String agentKey = String.join(Const.delimiter, Const.Version.V1, userOpenId, "Agent", "Shop", time(), random());
					u.content().account().put(agentKey, qrScene);
					
					qrTicket = gson.fromJson(json, QRCodeTicket.class);
					logger.info("Create permanent qrcode: " + qrTicket.getUrl());
				}
				u.content().setGlobalCommand(qrScene, "商铺客服", openid(), time());
				
				//end--
				
				qrcode = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket="+qrTicket.getTicket();
				itr = quiz.iterator();
				u.output("创建商铺客服二维码，\n" + qrcode + "\n请回答几个简单问题进行配置：");
				status = ANSWER;
			}
			
			if(status == QUESTION) {
				String value = t;
				String questionKey = String.join(Const.delimiter, Const.Version.V1, openid(), current.key(), qrScene);
				u.content().account().put(questionKey, value);
				String shopKey = String.join(Const.delimiter, Const.Version.V1, current.key(), qrScene);
				u.content().account().put(shopKey, value);
				status = ANSWER;
			}
			
			if(status == ANSWER) {
				if(itr.hasNext()) {
					current = itr.next();
					u.appendLine(current.value());
					String questionKey = String.join(Const.delimiter, Const.Version.V1, openid(), current.key(), qrScene);
					String existAnswer = u.content().account().get(questionKey);
					if(existAnswer != null) {
						u.appendLine("(旧：" + existAnswer + "，将会被覆盖)");
					}
					logger.info("[lijiaming] shop: " + questionKey + " = " + existAnswer);
					status = QUESTION;
				} else {
					u.appendLine("现在你可以把二维码打印出来给顾客使用了。\n" + qrcode);
					status = DONE;
				}
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(status == DONE) return init;
			return create;
		}

		@Override
		public String name() {
			return "创建商铺客服二维码";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	public ShopContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = shop;
		quiz.add(new KeyValue("Shop.Name", "你的商铺名称？"));
		quiz.add(new KeyValue("Shop.Goods", "你的商铺主要卖哪些商品或服务？"));
	}

}
