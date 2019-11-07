/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.CheckUtil;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.http.CallableGet;
import com.suppresswarnings.corpus.service.work.Counter;
import com.suppresswarnings.corpus.service.work.Quiz;
import com.suppresswarnings.corpus.service.wx.WXuser;


public class WXContext extends Context<CorpusService> {
	String openid;
	String wxid;
	WXuser user;
	
	State<Context<CorpusService>> template = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1154267650726164000L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if("hi".equals(t)) u.output("hello");
			else u.output("how do you do?");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "示例状态";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};

	public final State<Context<CorpusService>> reject = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1154267650726164000L;
		boolean first = true;
		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("（请联系我们：0756-6145606）高级功能，暂时无权使用该服务。");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(first) {
				first = false;
				return reject;
			}
			first = true;
			return init;
		}

		@Override
		public String name() {
			return "无权查看";
		}

		@Override
		public boolean finish() {
			return true;
		}
		
	};
	public static int bear = 1;
	public Counter counter = null;
	Random random = new Random();
	public Counter counter(CorpusService service) {
		if(counter == null) {
			counter = service.counters.get(openid());
			if(counter == null) {
				counter = new Counter(openid());
				service.counters.put(openid(), counter);
			}
		}
		//Counter the quiz answer
		return counter;
	}
	public final State<Context<CorpusService>> init = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4836433217450201449L;

		Iterator<Quiz> askQuiz = null;
		String[] FORMAT = {"嗯嗯，%s",
				"该我说了,%s", 
				"轮到我了,%s",
				"我想一下哈，%s", 
				"你咋说了那么多？\n%s",
				"我们说点别的吧，\n%s",
				"停，%s",
				"呃，%s",
				"不好意思，%s",
				"知道了，%s",
				"好的，%s",
				"啥？%s",
				"等一下没事吧，%s",
				"哦，%s",
				"嗯，%s",
				"什么？%s"
				};
		int count = bear;
		Map<String, AutoContext> contexts = new HashMap<>();

		
		@Override
		public void accept(String t, Context<CorpusService> u) {
			Set<String> commands = u.content().factories.keySet();
			if(commands.size() < 1) {
				u.output("稍等，我现在还没有准备好！");
			}
			if(exit(t, "exit()")) {
				String hitokoto = u.content().hitokoto();
				u.output(hitokoto);
				u.content().forgetIt(openid());
				return;
			}
			u.content().collectV2(t);
			String command = CheckUtil.cleanStr(t);
			ContextFactory<CorpusService> cf = u.content().factories.get(command);
			if(cf == null) {
				String exchange = u.content().globalCommand(command);
				if(exchange != null) {
					cf = u.content().factories.get(exchange);
				}
			}
			
			if(cf == null) {
				cf = u.content().factories.get(command.toLowerCase());
			}
			logger.info("ContextFactory: " + cf);
			if(cf != null) {
				//leave from worker user
				u.content().forgetIt(openid());
				u.content().uniqueKey(command);
				Context<CorpusService> ctx = cf.getInstance(wxid(), openid(), u.content());
				if(cf.ttl() != ContextFactory.forever) {
					u.content().contextx(openid, ctx, cf.ttl());
				} else {
					u.content().contextx(openid, ctx, TimeUnit.MINUTES.toMillis(5));
				}
				ctx.test(t);
				u.output(ctx.output());
			} else {
				logger.info("[WXContext] "+ openid() + "\tAccost words: " + t);
				
				if(t.startsWith("我要") || t.startsWith("我是") || t.startsWith("我有")) {
		            String subtopic = t.substring(0, 2);
		            String what     = t.substring(2);
		            StringBuffer topic = new StringBuffer();
		            topic.append("corpus").append("/").append(subtopic).append("/").append(what);
		            u.content().publish(topic.toString(), openid());
		            logger.info("发布消息稍后执行");
		        }
				
				if(t.startsWith("@")) {
					String[] ww = t.split("\\s+", 2);
					if(ww.length == 2) {
						String who = ww[0];
						String what = ww[1];
						String code = who.substring(1);
						String userid = u.content().token().get(String.join(Const.delimiter, Const.Version.V1, "Token", "For", "@User", code));
						logger.info("[WX @功能] userid = " + userid);
						if(userid != null) {
							ChatContext chatContext = new ChatContext(wxid(), openid(), userid, u.content());
							u.content().contextx(openid(), chatContext, TimeUnit.MINUTES.toMillis(2));
							chatContext.test(what);
							u.output(chatContext.output());
							return;
						}
					}
				} else if(t.startsWith("#")) {
					String[] ww = t.split("\\s+", 2);
					String who = ww[0];
					String reply = ww[1];
					String id = who.substring(1);
					CallableGet get = new CallableGet("https://suppresswarnings.com/cloud/offer/reply/" + openid() + "?id=" + id + "&reply=" + reply);
					try {String ret = get.call();u.output("回复成功:" + ret);}
					catch(Exception e) {u.output("回复异常："+e.getMessage());}
					return;
				}

				String aid = u.content().questionToAid.get(command);
				logger.info("[WXContext] after clean: " + command + " = " + aid);
				if(aid != null) {
					//TODO lijiaming: check cmd
					String keyCMD = String.join(Const.delimiter, Const.Version.V1, openid(), "AIIoT", command);
					String code = u.content().account().get(keyCMD);
					//get my things code, which is unique for each things
					logger.info("Key: " + keyCMD + " => " + code);
					if(code != null) {
						String remote = u.content().aiiot(wxid(), openid(), code, command, t, u);
						logger.info("[WXContext] remote: " + remote);
						u.output("远程状态："+ remote);
						if(remote != null) {
							logger.info("remove call aiiot success, just return");
							return;
						}
					}
					
					HashSet<String> answers = u.content().aidToAnswers.get(aid);
					if(answers != null && answers.size() > 0) {
						AutoContext context = contexts.get(aid);
						if(context == null) {
							context = new AutoContext(init, command, aid, answers, wxid(), openid(), u.content());
							contexts.put(aid, context);
						}
						context.test(t);
						u.output(context.output());
						count = bear;
						return;
					}
				}
				
				if(aid == null) {
					//save new question
					String answerKey = String.join(Const.delimiter, Const.Version.V2, "Collect", "Corpus", "Quiz", wxid(), "Answer", openid(), time(), random());
					u.content().data().put(answerKey, t);
					//counter for quiz
					counter(u.content()).quiz(System.currentTimeMillis(), t);
					aid = answerKey;
				}
				//save
				u.content().questionToAid.put(command, aid);
				//send task
				String result = u.content().youGotMe(openid(), t, aid);
				if(result != null) {
					HashSet<String> answers = u.content().aidToAnswers.get(aid);
					if(answers == null) {
						answers = new HashSet<>();
						u.content().aidToAnswers.put(aid, answers);
					}
					answers.add(result);
					u.output(result);
					count = bear;
				} else {
					count --;
					if(count < 0) {
						count = bear;
						if(askQuiz == null) {
							List<Quiz> quizs = u.content().assimilatedQuiz;
							Collections.shuffle(quizs);
							askQuiz = quizs.iterator();
						}
						if(askQuiz.hasNext()) {
							try {
								Quiz ask = askQuiz.next();
								int pointer = random.nextInt(FORMAT.length);
								u.output(String.format(FORMAT[pointer], ask.getQuiz().value()));
							} catch (Exception e) {
								askQuiz = null;
								u.output("我有点笨，我得想想");
							}
						}
					}
					String string = u.content().remoteCall(openid(), "T_Things_Tencent_201907201450", "获取腾讯智能回复", t);
					u.output(string);
				}
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if("打卡下班".equals(t)) {
				u.content().offWork(openid());
				logger.info("[WXContext] off work");
				u.output(counter(u.content()).report());
			}
			return this;
		}

		@Override
		public String name() {
			return "微信上下文初始化状态";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	public State<Context<CorpusService>> set(State<Context<CorpusService>> start) {
		return new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -2352514490121930101L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				start.accept(t, u);
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return start.apply(t, u);
			}

			@Override
			public String name() {
				return "set: " + start.name();
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
	}
	
	public WXContext(String wxid, String openid, CorpusService ctx) {
		super(ctx);
		this.wxid = wxid;
		this.openid = openid;
		this.state = init;
	}
	
	public WXuser user() {
		if(user == null) user = content().getWXuserByOpenId(openid());
		return user;
	}
	public String openid(){
		return openid;
	}
	public String wxid() {
		return wxid;
	}
	
	@Override
	public State<Context<CorpusService>> exit() {
		return init;
	}
	public void state(State<Context<CorpusService>> state) {
		this.state = state;
	}
	
	
}