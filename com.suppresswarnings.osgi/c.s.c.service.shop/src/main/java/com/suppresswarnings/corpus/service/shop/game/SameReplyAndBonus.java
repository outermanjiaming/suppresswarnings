package com.suppresswarnings.corpus.service.shop.game;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.work.Quiz;

public class SameReplyAndBonus extends Context<CorpusService> {
	State<Context<CorpusService>> exit;
	AtomicBoolean consumer;
	AtomicBoolean first  = new AtomicBoolean(true);
	AtomicBoolean finish = new AtomicBoolean(false);
	String ownerid;
	String openid;
	String qrScene;
	int n = 3;
	public SameReplyAndBonus(AtomicBoolean consumer, String ownerid, String openid, String qrScene, State<Context<CorpusService>> exit, CorpusService service){
		super(service);
		this.state = scan;
		this.exit = exit;
		this.consumer = consumer;
		this.ownerid = ownerid;
		this.openid = openid;
		this.qrScene = qrScene;
	}

	State<Context<CorpusService>> scan = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2694036756949763339L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String keyCustomer = String.join(Const.delimiter, Const.Version.V1, ownerid, "Shop", "Customer", openid());
			u.content().account().put(keyCustomer, openid());
			String keyHistoryCustomer = String.join(Const.delimiter, Const.Version.V1, ownerid, "Shop", "History", "Customer", time(), random());
			u.content().account().put(keyHistoryCustomer, openid());
			String keyUser = String.join(Const.delimiter, Const.Version.V1, "Shop", qrScene, "Customer", time(), random());
			u.content().account().put(keyUser, openid());
			consumer.compareAndSet(false, true);
			u.output("接下来请耐心回答3个问题");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return customer;
		}

		@Override
		public String name() {
			return "答题得奖";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	State<Context<CorpusService>> customer = new State<Context<CorpusService>>() {
		Iterator<Quiz> quiz = null;
		List<KeyValue> qa = new ArrayList<>();
		Quiz next = null;
		int i=0;
		/**
		 * 
		 */
		private static final long serialVersionUID = 3937145686042893179L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			
			i ++;
			if(first.compareAndSet(true, false)) {
				String keyPhone = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "Contact", "Phone");
				String keyHistory = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "Contact", "History", "Phone", time());
				u.content().account().put(keyHistory, t);
				u.content().account().put(keyPhone, t);
				logger.info("[Shop customer] save phone: " + openid() + " => " + t);
				u.output("请回答"+n+"个问题，大奖等你来拿！");
				quiz = getQuiz(u.content(), n);
			} else {
				String keyReply = String.join(Const.delimiter, next.getQuiz().key(), "Reply", openid(), time(), random());
				u.content().data().put(keyReply, t);
				qa.add(new KeyValue(next.getQuiz().value(), t));
			}
			if(quiz != null && quiz.hasNext()) {
				next = quiz.next();
				u.output(i + ". " + next.getQuiz().value());
			} else {
				finish.compareAndSet(false, true);
				int lable = 0;
				int code = new Random().nextInt(49) + 1;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date = sdf.format(new Date());
				String keyRand = String.join(Const.delimiter, Const.Version.V1, "Shop", "Reply", date, openid(), time());
				StringBuffer sb = new StringBuffer();
				sb.append("抽奖号码：" + code).append("\n");
				String keyMycode = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "Lotus", date);
				u.content().account().put(keyMycode, ""+code);
				u.output("抽奖号码：" + code);
				for(KeyValue kv : qa) {
					lable ++;
					sb.append(lable + ".\t" + kv.key()).append("\n");
					u.output(lable + ".\t" + kv.key());
					sb.append("\t\t" + kv.value()).append("\n");
					u.output("\t\t" + kv.value());
				}
				u.content().data().put(keyRand, sb.toString());
				String keyCode = String.join(Const.delimiter, Const.Version.V1, "Shop", "Lotus", date, ""+code, openid());
				u.content().account().put(keyCode, openid());
				qa.clear();
				u.output("感谢您回复这些问题，请稍后留意我们的中奖通知！");
			}
		}
		
		public Iterator<Quiz> getQuiz(CorpusService service, int n) {
			List<Quiz> all = new ArrayList<>();
			all.addAll(service.assimilatedQuiz);
			Collections.shuffle(all);
			if(all.size() <= n) return all.iterator();
			else {
				List<Quiz> little = new ArrayList<>();
				for(int i=0;i<n;i++) {
					little.add(all.get(i));
				}
				return little.iterator();
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(finish.get()) {
				logger.info("finish = " + finish.get());
				return exit;
			}
			return customer;
		}

		@Override
		public String name() {
			return "顾客回复问题";
		}

		@Override
		public boolean finish() {
			return finish.get();
		}
	};

	public String openid() {
		return openid;
	}
	@Override
	public State<Context<CorpusService>> exit() {
		return exit;
	}
}
