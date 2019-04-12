package com.suppresswarnings.corpus.service.shop.game;

import java.util.concurrent.atomic.AtomicBoolean;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.game.Ally;

public class SameReplyAndBonus extends Context<CorpusService> {
	State<Context<CorpusService>> exit;
	AtomicBoolean consumer;
	AtomicBoolean first  = new AtomicBoolean(true);
	AtomicBoolean finish = new AtomicBoolean(false);
	Ally myself;
	String ownerid;
	String openid;
	String qrScene;
	int n = 3;
	public SameReplyAndBonus(AtomicBoolean consumer, String ownerid, String openid, String qrScene, State<Context<CorpusService>> exit, Context<CorpusService> context){
		super(context.content());
		this.state = game;
		this.exit = exit;
		this.consumer = consumer;
		this.ownerid = ownerid;
		this.openid = openid;
		this.qrScene = qrScene;
		this.myself = context.content().guard().joinOrCreateWaitingRoom(openid, ownerid, qrScene);
	}

	State<Context<CorpusService>> game = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2694036756949763339L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(first.compareAndSet(true, false)) {
				String keyCustomer = String.join(Const.delimiter, Const.Version.V1, ownerid, "Shop", "Customer", openid());
				u.content().account().put(keyCustomer, openid());
				String keyHistoryCustomer = String.join(Const.delimiter, Const.Version.V1, ownerid, "Shop", "History", "Customer", time(), random());
				u.content().account().put(keyHistoryCustomer, openid());
				String keyUser = String.join(Const.delimiter, Const.Version.V1, "Shop", qrScene, "Customer", time(), random());
				u.content().account().put(keyUser, openid());
				consumer.compareAndSet(false, true);
				u.output("接下来请耐心回答"+n+"个问题");
				myself.start(u);
			} else {
				if(myself.finish()) {
					if(finish.compareAndSet(false, true)) {
						myself.chat(t, u);
					}
				} else {
					myself.reply(t, u);
				}
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(finish.get()) return exit;
			return game;
		}

		@Override
		public String name() {
			return "答题得奖";
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
