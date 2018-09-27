/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.similar;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.common.Type;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class SimilarContext extends WXContext {
	public static final String CMD = "我要回答同义句";
	public static final String[] AUTH = {"Similar"};
	State<Context<CorpusService>> similar = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7065053790006573967L;
		@Override
		public void accept(String t, Context<CorpusService> u) {
			boolean x = u.content().iWantJob(openid(), Type.Similar);
			if(x) {
				u.output("打卡成功，一会儿有在线任务优先派发给您,\n不想接任务就输入「打卡下班」，\n如果发现错误数据就输入「删除这一条」，\n如果不知道怎么答就输入「跳过」");
			} else {
				u.output("现在暂时没有在线任务，\n不想接任务就输入「打卡下班」，\n如果发现错误数据就输入「删除这一条」，\n如果不知道怎么答就输入「跳过」");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if("打卡下班".equals(t)) {
				boolean off = u.content().offWork(openid());
				logger.info("[SimilarContext] off work: " + openid() + "= " + off);
				return init;
			}
			return similar;
		}
		
		@Override
		public String name() {
			return "同义句回复";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	public SimilarContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = similar;
	}

}
