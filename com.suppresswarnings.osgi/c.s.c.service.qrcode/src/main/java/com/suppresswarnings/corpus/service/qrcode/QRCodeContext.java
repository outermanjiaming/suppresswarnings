/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.qrcode;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.QRCodeUtil;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.http.CallablePost;
import com.suppresswarnings.corpus.service.wx.QRCodeTicket;
import com.suppresswarnings.corpus.service.wx.WXnews;

public class QRCodeContext extends WXContext {
	public org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	public static final String CMD = "我要二维码";
	public static final String[] AUTH = {"QRCode"};
	State<Context<CorpusService>> shorturl = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1373656939884632232L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("请输入文章链接地址(http***)");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(!t.startsWith("http")) return shorturl;
			return shorturlGenerate;
		}

		@Override
		public String name() {
			return "我要短链接";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	public QRCodeTicket getQRCodeByScene(CorpusService service, String qrScene) {
		String qrKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", qrScene);
		String qrSceneKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", qrScene, "Scene");
		String exist = service.account().get(qrKey);
		Gson gson = new Gson();
		QRCodeTicket qrTicket = null;
		if(exist != null) {
			qrTicket = gson.fromJson(exist, QRCodeTicket.class);
			logger.info("Use exist permanent qrcode: " + qrTicket.getUrl());
		} else {
			String access = service.accessToken("Generate Permanent QRCode");
			String json = service.qrCode(access, Integer.MAX_VALUE, "QR_LIMIT_STR_SCENE", qrScene);
			service.account().put(qrKey, json);
			service.account().put(qrSceneKey, qrScene);
			String qrMyKey = String.join(Const.delimiter, Const.Version.V1, openid(), "QRCode", qrScene);
			service.account().put(qrMyKey, json);
			qrTicket = gson.fromJson(json, QRCodeTicket.class);
			logger.info("Create permanent qrcode: " + qrTicket.getUrl());
		}
		return qrTicket;
	}
	
	State<Context<CorpusService>> shorturlGenerate = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 526711966605403390L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String longurl = t;
			Gson gson = new Gson();
			String exist = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "ShortUrl", "Long", longurl));
			if(exist != null) {
				logger.warn("shorturl already exists: " + exist);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				QRCodeUtil.createQrCode(outputStream, exist, 1, "png");
				byte[] bs = outputStream.toByteArray();
				Base64.Encoder encoder = Base64.getEncoder();
				String encodedText = encoder.encodeToString(bs);
				u.content().token().put(exist, encodedText);
				WXnews news = new WXnews();
				news.setTitle(exist);
				news.setDescription("点击查看二维码(进去之后可以长按扫码)");
				news.setUrl("http://suppresswarnings.com/short.html?state=" + exist);
				news.setPicUrl(news.getUrl());
				u.output("news://" + gson.toJson(news));
			} else {
				String access = u.content().accessToken("shorturl");
				String url = "https://api.weixin.qq.com/cgi-bin/shorturl?access_token=" + access;
				String json = "{\"action\":\"long2short\",\"long_url\":\""+longurl+"\"}";
				try {
					CallablePost post = new CallablePost(url, json);
					String ret = post.call();
					ShortUrl shorty = gson.fromJson(ret, ShortUrl.class);
					if(shorty.getErrcode() == 0) {
						String shorturls = shorty.getShort_url();
						u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "ShortUrl", "Long", longurl), shorturls);
						u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "ShortUrl", "Short", shorturls), longurl);
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						QRCodeUtil.createQrCode(outputStream, shorturls, 1, "png");
						byte[] bs = outputStream.toByteArray();
						Base64.Encoder encoder = Base64.getEncoder();
						String encodedText = encoder.encodeToString(bs);
						//String pngurl = "data:image/png;base64," + encodedText;
						u.content().token().put(shorturls, encodedText);
						WXnews news = new WXnews();
						news.setTitle(shorturls);
						news.setDescription("点击查看二维码(进去之后可以长按扫码)");
						news.setUrl("http://suppresswarnings.com/short.html?state=" + shorturls);
						news.setPicUrl(news.getUrl());
						u.output("news://" + gson.toJson(news));
					}else {
						u.output("转化短链接失败");
					}
				} catch (Exception e) {
					logger.error("fail to call long url to short url", e);
					u.output("获取短链接失败");
				}
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "长链接-短链接";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	State<Context<CorpusService>> permanent = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 526711966605403390L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("请按如下格式输入属性：\nP_{Func}_{Target}\n注：保持唯一，相同输入可以找到已存在的二维码");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(shorturl.name().equals(t)) return shorturl;
			if(!t.startsWith("P_") || t.split("_").length != 3) return permanent;
			String adminKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Admin");
			String admin = u.content().account().get(adminKey);
			if(admin == null) return reject;
			return permanentGenerate;
		}

		@Override
		public String name() {
			return "永久二维码";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};

	State<Context<CorpusService>> permanentGenerate = new State<Context<CorpusService>>() {


		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String P_Func_Target = t;
			String[] args = P_Func_Target.split("_");
			String p = args[0];
			String func = args[1];
			String target = args[2];
			if(!"P".equals(p)){
				u.output("格式不对");
				return;
			}
			if("Admin".equals(target)) {
				u.output("不能生成Admin授权");
				return;
			}
			String qrKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", P_Func_Target);
			String qrSceneKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", P_Func_Target, "Scene");
			String exist = u.content().account().get(qrKey);
			String qrScene = u.content().account().get(qrSceneKey);
			Gson gson = new Gson();
			QRCodeTicket qrTicket = null;
			if(exist != null) {
				qrTicket = gson.fromJson(exist, QRCodeTicket.class);
				logger.info("Use exist permanent qrcode: " + qrTicket.getUrl());
			} else {
				qrScene = String.join("_", P_Func_Target, time(), random());
				String access = u.content().accessToken("Generate Permanent QRCode");
				String json = u.content().qrCode(access, Integer.MAX_VALUE, "QR_LIMIT_STR_SCENE", qrScene);
				u.content().account().put(qrKey, json);
				u.content().account().put(qrSceneKey, qrScene);
				String qrMyKey = String.join(Const.delimiter, Const.Version.V1, openid(), "QRCode", qrScene);
				u.content().account().put(qrMyKey, json);
				qrTicket = gson.fromJson(json, QRCodeTicket.class);
				logger.info("Create permanent qrcode: " + qrTicket.getUrl());
			}
			if("Auth".equals(func)) {
				u.content().setGlobalCommand(qrScene, "我要授权", openid(), time());
			}
			WXnews news = new WXnews();
			news.setTitle(qrScene);
			news.setDescription("点击查看二维码");
			news.setUrl("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + qrTicket.getTicket());
			news.setPicUrl(news.getUrl());
			String json = gson.toJson(news);
			
			u.output("news://" + json);
//			u.output(String.format("Scene:%s\n二维码地址：\nhttps://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=%s\n任务完成，点击上面地址查看二维码。", qrScene, qrTicket.getTicket()));
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "生成永久二维码";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	public QRCodeContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = permanent;
	}

}
