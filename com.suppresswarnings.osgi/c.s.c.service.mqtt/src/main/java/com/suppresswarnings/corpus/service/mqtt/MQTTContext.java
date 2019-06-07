package com.suppresswarnings.corpus.service.mqtt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class MQTTContext extends WXContext {
	public static final String CMD = "我要消息中间件";
	public static final String[] AUTH = {"MQTT"};
	ExecutorService service = Executors.newFixedThreadPool(5);
	String stateKey = String.join(Const.delimiter, Const.Version.V1, "Info", "MQTT", "State");
	Map<String, Map<String, Set<String>>> map = new HashMap<>();
	Map<String, Set<String>> wantMap = new HashMap<>();
	Map<String, Set<String>> haveMap = new HashMap<>();
	Map<String, Set<String>> amMap = new HashMap<>();
	MqttSubscribe iwant, ihave, iam;
	
	
	State<Context<CorpusService>> mqtt = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7395251645847372950L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			enter.accept(t, u);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return enter;
		}

		@Override
		public String name() {
			return "MQTT";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<CorpusService>> subscribe = new State<Context<CorpusService>>() {
		AtomicBoolean did = new AtomicBoolean(false);
		/**
		 * 
		 */
		private static final long serialVersionUID = 7395251645847372950L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(!did.compareAndSet(false, true)) {
				u.output("请勿重复订阅");
				return;
			}
			
			String exist = u.content().account().get(stateKey);
			if(exist == null || "None".equals(exist) || "OFF".equals(exist)) {
				u.content().account().put(stateKey, "ON");
				
				IMqttMessageListener want = (s, mqttMessage) -> {
					try {
						logger.info("1.加入要集合，2.发消息给有这些或者是这些的人:" + s);
		                String userid = mqttMessage.toString();
		                String[] topics = s.split("/");
		                String what = topics[2];
		                if(!wantMap.containsKey(what)) {
		                	wantMap.put(what, new HashSet<>());
		                }
		                Set<String> userids = wantMap.get(what);
		                userids.add(userid);
		                u.content().atUser(userid, "订阅成功，现在有" + userids.size() + "个用户和你一样");
		                String code = u.content().generateRandomToken(userid, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(12), "@User");
		                if(haveMap.containsKey(what)) {
		                	Set<String> subs = haveMap.get(what);
		                	subs.forEach(e -> {
		                		if(!e.equals(userid)) {
		                			u.content().atUser(e, "有人需要(" + what + ")，正好你有这方面资源，如需联系对方，请输入：@" + code + " + 你想说的") ;
		                		}
		                	});
		                }
		                
		                if(amMap.containsKey(what)) {
		                	Set<String> subs = amMap.get(what);
		                	subs.forEach(e -> {
		                		if(!e.equals(userid)) {
		                			u.content().atUser(e, "有人在找(" + what + ")，正好你是他要找的，如需联系对方，请输入：@" + code + " + 你想说的") ;
		                		}
		                	});
		                }
		                
					} catch (Exception e) {
						logger.error("fail to sub msg", e);
					}
	                
		        };
		        IMqttMessageListener have = (s, mqttMessage) -> {
		            logger.info("1.加入有集合，2.给需要这些的人发信息:" + s);
		            try {
		                String userid = mqttMessage.toString();
		                String[] topics = s.split("/");
		                String what = topics[2];
		                if(!haveMap.containsKey(what)) {
		                	haveMap.put(what, new HashSet<>());
		                }
		                Set<String> userids = haveMap.get(what);
		                userids.add(userid);
		                u.content().atUser(userid, "订阅成功，现在有" + userids.size() + "个用户和你一样");
		                String code = u.content().generateRandomToken(userid, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(12), "@User");
		                if(wantMap.containsKey(what)) {
		                	Set<String> subs = wantMap.get(what);
		                	subs.forEach(e -> {
		                		if(!e.equals(userid)) {
		                			u.content().atUser(e, "有人有(" + what + ")，正好你需要，如需联系对方，请输入：@" + code + " + 你想说的") ;
		                		}
		                	});
		                }
					} catch (Exception e) {
						logger.error("fail to sub msg", e);
					}
		        };
		
		        IMqttMessageListener am = (s, mqttMessage) -> {
		            logger.info("1.加入是集合，2.给需要要这些和有这些的人发消息:" + s);
		            try {
		                String userid = mqttMessage.toString();
		                String[] topics = s.split("/");
		                String what = topics[2];
		                if(!amMap.containsKey(what)) {
		                	amMap.put(what, new HashSet<>());
		                }
		                Set<String> userids = amMap.get(what);
		                userids.add(userid);
		                u.content().atUser(userid, "订阅成功，现在有" + userids.size() + "个用户和你一样");
		                String code = u.content().generateRandomToken(userid, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(12), "@User");
		                if(wantMap.containsKey(what)) {
		                	Set<String> subs = wantMap.get(what);
		                	subs.forEach(e -> {
		                		if(!e.equals(userid)) {
		                			u.content().atUser(e, "有人是(" + what + ")，正好你在找这方面资源，如需联系对方，请输入：@" + code + " + 你想说的") ;
		                		}
		                	});
		                }
		                
		                if(haveMap.containsKey(what)) {
		                	Set<String> subs = haveMap.get(what);
		                	subs.forEach(e -> {
		                		if(!e.equals(userid)) {
		                			u.content().atUser(e, "有人是(" + what + ")，正好你有这方面资源，需要扩展资源吗？如需联系对方，请输入：@" + code + " + 你想说的") ;
		                		}
		                	});
		                }
					} catch (Exception e) {
						logger.error("fail to sub msg", e);
					}
		        };
		        iwant = new MqttSubscribe(want, "tcp://suppresswarnings.com:1883", "corpus/我要/+", "lijiaming", "lijiaming", "mqtt-want");
		        ihave = new MqttSubscribe(have, "tcp://suppresswarnings.com:1883", "corpus/我有/+", "lijiaming", "lijiaming", "mqtt-have");
		        iam   = new MqttSubscribe(am,   "tcp://suppresswarnings.com:1883", "corpus/我是/+", "lijiaming", "lijiaming", "mqtt-am");
		
		        service.submit(iam);
		        service.execute(ihave);
		        service.execute(iwant);
		
		        service.shutdown();
		        u.output("消息订阅服务器已经运行");
			} else {
				u.output("状态不对：" + exist);
				return;
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "监听消息";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	
	State<Context<CorpusService>> publish = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7395251645847372950L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(t.length() < 3) {
				u.output("你要什么");
				u.output("你有什么");
				u.output("你是什么");
				return;
			}
			
			if(t.startsWith("我要") || t.startsWith("我是") || t.startsWith("我有")) {
				String subtopic = t.substring(0, 2);
				String what     = t.substring(2);
				StringBuffer topic = new StringBuffer();
				topic.append("corpus").append("/").append(subtopic).append("/").append(what);
				MqttPublish command = new MqttPublish(openid(), "tcp://suppresswarnings.com:1883", topic.toString(), "lijiaming", "lijiaming", "mqtt-publish-" + openid());
				service.execute(command);
				u.output("你已经发布消息成功:" + topic.toString());
			}
			
			u.output("输入格式：我要xxx/我是xxx/我有xxx");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(t.startsWith("我要") || t.startsWith("我是") || t.startsWith("我有")) {
				return publish;
			}
			return init;
		}

		@Override
		public String name() {
			return "发布消息";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	
	State<Context<CorpusService>> enter = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 9192390025098986206L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("请输入命令：");
			u.output(subscribe.name());
			u.output(publish.name());
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(subscribe.name().equals(t)) return subscribe;
			if(publish.name().equals(t)) return publish;
			return init;
		}

		@Override
		public String name() {
			return "消息中间件";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	
	
	@Override
	public State<Context<CorpusService>> exit() {
		//1.要不要关闭连接？
		//2.通知哪些用户？
		logger.info("[MQTT] 会话超时");
		content().account().put(stateKey, "OFF");
		return super.exit();
	}



	public MQTTContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.map.put("我要", wantMap);
		this.map.put("我是", amMap);
		this.map.put("我有", haveMap);
		this.state(mqtt);
	}

}
