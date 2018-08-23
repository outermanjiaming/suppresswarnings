/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.collect;

import java.io.File;
import java.io.FileWriter;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.QRCodeTicket;
import com.suppresswarnings.corpus.service.wx.WXnews;
import com.suppresswarnings.corpus.service.wx.WXuser;

public class CollectContext extends WXContext {
	public static final String CMD = "我要收集语料";
	String quizId;
	String quiz;
	
	State<Context<CorpusService>> collect = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7919283991709878795L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("请用一句话描述你要收集的语料(不懂可以去官网查看攻略，或请教他人)");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(CMD.equals(t)) return collect;
			return question;
		}

		@Override
		public String name() {
			return "发起收集语料任务";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	State<Context<CorpusService>> question = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 633172892869816895L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			quiz = t;
			//check quiz exist or valid
			String quizIdKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "QuizId", openid(), quiz);
			String exist = u.content().data().get(quizIdKey);
			if(exist!= null) {
				quizId = exist;
			} else {
				quizId = "T_Corpus_" + openid() + "_" + time() + "_" + random();
				u.content().data().put(quizIdKey, quizId);
				
				String quizKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId);
				String quizOpenIdKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "OpenId");
				String quizTimeKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "Time");
				u.content().data().put(quizKey, quiz);
				u.content().data().put(quizOpenIdKey, openid());
				u.content().data().put(quizTimeKey, time());
				
				String myQuizKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Collect", "Corpus");
				String allQuizKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", openid(), time(), random());
				
				String current = u.content().account().get(myQuizKey);
				if(current != null) {
					//not first time
					String lastQuizKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Collect", "Corpus", time(), random());
					u.content().account().put(lastQuizKey, current);
				}
				u.content().account().put(myQuizKey, quizId);
				u.content().account().put(allQuizKey, quizId);
			}
			u.output("恭喜你，语料收集任务创建成功。现在请举例示范该如何回答");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return welldone;
		}

		@Override
		public String name() {
			return "语料问题";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	State<Context<CorpusService>> welldone = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8401731135201783674L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String answer = t;
			String answerKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "Sample");
			u.content().data().put(answerKey, answer);
			int second = 2592000;
			String access = u.content().accessToken("Collect Corpus QRCode");
			String json = u.content().qrCode(access, second, "QR_STR_SCENE", quizId);
			String quizQrcodeKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "QRCode");
			u.content().data().put(quizQrcodeKey, json);
			
			Gson gson = new Gson();
			QRCodeTicket qrTicket = null;
			qrTicket = gson.fromJson(json, QRCodeTicket.class);
			logger.info("Create 30Days qrcode: " + qrTicket.getUrl());
			
			String rootPath = System.getProperty("path.html");
			String filename = quizId + ".html";
			File file = new File(rootPath + filename);
			WXnews news = new WXnews();
			news.setTitle("我在收集语料，帮我说句话！");
			news.setDescription("分享给朋友们收集语料，也可以打开后分享到朋友圈，让更多人参与收集语料。语料数据可以卖钱！");
			news.setUrl("http://suppresswarnings.com/" + filename);
			news.setPicUrl("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + qrTicket.getTicket());
			String jsonnews = gson.toJson(news);
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
				context.put("qrscene", quizId);
				context.put("quiz", quiz);
				context.put("answer", answer);
				context.put("qrcode", "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + qrTicket.getTicket());
				FileWriter writer = new FileWriter(file);
				boolean s = engine.mergeTemplate("collect.vm", "UTF-8", context, writer);
				logger.info("html file created: " + s);
				writer.flush();
				writer.close();
				String nowCommandKey = String.join(Const.delimiter, "Setting", "Global", "Command", quizId.toLowerCase());
				u.content().account().put(nowCommandKey, "我要上报语料");
			} catch (Exception e) {
				logger.error("lijiaming: fail to use velocity engine", e);
			}
			
			u.output("news://" + jsonnews);
			
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "输出收集码";
		}

		@Override
		public boolean finish() {
			return true;
		}
	};
	public CollectContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = collect;
	}
	
}
