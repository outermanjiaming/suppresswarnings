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
			u.output("暂时无权查看，请联系管理员。（本次对话结束）");
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
	public String quizId = null;
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
				"嗯，%s"
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
				u.output("不聊算了～");
				u.content().forgetIt(openid());
				//TODO bugfixed
				return;
			}
			String command = CheckUtil.cleanStr(t);
			ContextFactory<CorpusService> cf = u.content().factories.get(command);
			if(cf == null) {
				String exchange = u.content().globalCommand(command);
				if(exchange != null) {
					cf = u.content().factories.get(exchange);
					if(cf == null) {
						cf = u.content().factories.get(command.toLowerCase());
					}
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
					String remote = u.content().remoteCall(openid(), "T_Things_MQTT_Publish_201906011732", "发布MQTT消息", String.join("#", openid(), t));
					u.output("发布消息到MQTT：" + remote);
				}
				
				if(t.startsWith("@")) {
					String[] ww = t.split("\\s+");
					if(ww.length < 2) {
						u.output("你使用了 @ 功能，但是格式不对，请在@谁谁谁之后 加一个空格再输入内容");
					} else {
						String who = ww[0];
						String what = ww[1];
						String code = who.substring(1);
						String userid = u.content().token().get(String.join(Const.delimiter, Const.Version.V1, "Token", "For", "@User", code));
						logger.info("[WX @功能] userid = " + userid);
						if(userid != null) {
							ChatContext chatContext = new ChatContext(wxid(), openid(), userid, content());
							u.content().contextx(openid(), chatContext, TimeUnit.MINUTES.toMillis(2));
							chatContext.test(what);
							return;
						}
					}
				}
				
				if(quizId == null) {
					quizId = u.content().getTodoQuizid();
					logger.info("[WXContext] quizId was null, Now = " + quizId);
				}
				String reply = CheckUtil.cleanStr(t);
				String aid = u.content().questionToAid.get(reply);
				logger.info("[WXContext] after clean: " + reply + " = " + aid);
				if(aid != null) {
					//TODO lijiaming: check cmd
					String keyCMD = String.join(Const.delimiter, Const.Version.V1, openid(), "AIIoT", reply);
					String code = u.content().account().get(keyCMD);
					//get my things code, which is unique for each things
					logger.info("Key: " + keyCMD + " => " + code);
					if(code != null) {
						String remote = u.content().aiiot(wxid(), openid(), code, reply, t, u);
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
							context = new AutoContext(init, reply, aid, answers, wxid(), openid(), u.content());
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
					String answerKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "Answer", openid(), time(), random());
					u.content().data().put(answerKey, t);
					//counter for quiz
					counter(u.content()).quiz(System.currentTimeMillis(), t);
					aid = answerKey;
				}
				//save
				u.content().questionToAid.put(reply, aid);
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
//					u.content().connectChat(wxid(), openid(), t);
					//count to 2
					//fetch a task todo
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
		this.quizId = ctx.getTodoQuizid();
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
		content().sendTxtTo("close " + getClass().getSimpleName(), content().getRandomText(openid()), openid());
		return init;
	}
	public void state(State<Context<CorpusService>> state) {
		this.state = state;
	}
	
	
}