/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.corpus.accost;

import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.osgi.leveldb.LevelDB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.common.State;

/**
 * 搭讪的leveldb前缀=001.Data.accost.
 * @author lijiaming
 *
 */
public class AccostContext extends WXContext {
	public static final int begin = 0, answer = 1, holdon = 2, end = 3;
	public int reply(String key, String response, LevelDB db) {
		String k = String.join(Const.delimiter, Const.Version.V1, "reply", key, time(), openid());
		update();
		return db.put(k, response);
	}
	
	State<Context<CorpusService>> my = new State<Context<CorpusService>>() {
		/**
		 * 回答我之前的搭讪
		 */
		private static final long serialVersionUID = -573575076883181758L;
		
		int status = begin;
		int step = 10;
		int size = 30;
		int index = 0;
		String prefix = String.join(Const.delimiter, Const.Version.V1, Const.data, "accost");
		String start = prefix;
		List<KeyValue> accosts = new ArrayList<>();
		String key;
		
		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(status == end) {
				LevelDB db = u.content().data();
				int result = reply(key, t, db);
				if(result != LevelDB.OK) {
					logger.error("fail to save to Data leveldb: " + db);
				}
				u.output("您已经回答了很多，累了吧。\n是继续，还是退出？");
				return;
			}

			if(status == begin) {
				LevelDB db = u.content().data();
				String s = start;
				db.page(prefix, s, null, size, new BiConsumer<String, String>() {
					
					@Override
					public void accept(String t, String u) {
						KeyValue kv = new KeyValue(t, u);
						accosts.add(kv);
						start = t;
					}
				});
				Collections.shuffle(accosts);
			}
			
			if(status == answer){
				LevelDB db = u.content().data();
				int result = reply(key, t, db);
				if(result != LevelDB.OK) {
					logger.error("fail to save to Data leveldb: " + db);
				}
			}
			
			KeyValue todo = accosts.get(index);
			++ index;
			key = todo.key();
			u.output(todo.value());
			status = answer;
			
			if(index % step == 0) {
				status = holdon;
			}
			
			if(index >= size) {
				status = end;
				index = 0;
				accosts.clear();
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(exit(t, "我要退出")) {
				status = begin;
				start = prefix;
				accosts.clear();
				return init;
			}
			if(yes(t, "继续")) {
				status = begin;
			}
			return this;
		}

		@Override
		public String name() {
			return "accost";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	public AccostContext(String openid, CorpusService ctx) {
		super(openid, ctx);
		this.state = my;
	}
}
