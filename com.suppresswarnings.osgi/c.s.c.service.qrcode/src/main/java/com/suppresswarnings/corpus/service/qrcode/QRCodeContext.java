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

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.QRCodeTicket;
import com.suppresswarnings.corpus.service.wx.WXnews;
import com.suppresswarnings.corpus.service.wx.WXuser;

public class QRCodeContext extends WXContext {
	public org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	public static final String CMD = "我要二维码";
	
	State<Context<CorpusService>> permanent = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 526711966605403390L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("请按如下格式输入属性：\nP_{func}_{random|fix:owner}\n注：func保持唯一，可以通过func找到已存在的二维码");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(!t.startsWith("P_") || t.split("_").length != 3) return permanent;
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
			String[] args = t.split("_");
			String p = args[0];
			String func = args[1];
			String random = args[2];
			if("random".equals(random)) {
				random = random();
			}
			String now = time();
			
			String qrKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", "P", func);
			String qrSceneKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", "P", func, "Scene");
			String exist = u.content().data().get(qrKey);
			String qrScene = null;
			Gson gson = new Gson();
			QRCodeTicket qrTicket = null;
			if(exist != null) {
				qrTicket = gson.fromJson(exist, QRCodeTicket.class);
				qrScene = u.content().data().get(qrSceneKey);
				logger.info("Use exist permanent qrcode: " + qrTicket.getUrl());
			} else {
				String sceneStr = String.join("_", p, func, random, now);
				String access = u.content().accessToken("Generate Permanent QRCode");
				String json = u.content().qrCode(access, Integer.MAX_VALUE, "QR_LIMIT_STR_SCENE", sceneStr);
				u.content().data().put(qrKey, json);
				u.content().data().put(qrSceneKey, sceneStr);
				qrScene = sceneStr;
				String qrMyKey = String.join(Const.delimiter, Const.Version.V1, openid(), "QRCode", "P", time(), random());
				u.content().account().put(qrMyKey, json);
				qrTicket = gson.fromJson(json, QRCodeTicket.class);
				logger.info("Create permanent qrcode: " + qrTicket.getUrl());
			}
			String rootPath = "/usr/share/nginx/suppresswarnings/com.suppresswarnings.html/src/html/";
			String filename = qrScene + ".html";
			File file = new File(rootPath + filename);
			WXnews news = new WXnews();
			news.setTitle(qrScene);
			news.setDescription("点击查看二维码");
			news.setUrl("http://suppresswarnings.com/" + filename);
			news.setPicUrl(news.getUrl());
			String json = gson.toJson(news);
			WXuser user = user();
			try {
				
				Properties properties=new Properties();
				properties.setProperty(Velocity.ENCODING_DEFAULT, "UTF-8");
		        properties.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
		        properties.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8");
		        VelocityEngine engine = new VelocityEngine(properties);
				VelocityContext context = new VelocityContext();
				context.put("userimg", user.getHeadimgurl());
				context.put("username", user.getNickname());
				context.put("filename", filename);
				context.put("qrscene", qrScene);
				context.put("qrcode", "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + qrTicket.getTicket());
				FileWriter writer = new FileWriter(file);
				boolean s = engine.mergeTemplate("template.vm", "UTF-8", context, writer);
				logger.info("html file created: " + s);
				writer.flush();
				writer.close();
			} catch (Exception e) {
				logger.error("lijiaming: fail to use velocity engine", e);
			}
			
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

	public static void main(String[] args) {
		try {
			Properties properties=new Properties();
			properties.setProperty(Velocity.ENCODING_DEFAULT, "UTF-8");
	        properties.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
	        properties.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8");
	        VelocityEngine engine = new VelocityEngine(properties);
			VelocityContext context = new VelocityContext();
			context.put("userimg", "aaaaaaaa");
			context.put("username", "bbbbbb");
			context.put("filename", "ssssss");
			context.put("qrscene", "rrrr");
			context.put("qrcode", "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=dddd");
			
			StringWriter writer = new StringWriter();
			boolean s = engine.mergeTemplate("template.vm", "UTF-8", context, writer);
			System.out.println(writer.toString());
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
