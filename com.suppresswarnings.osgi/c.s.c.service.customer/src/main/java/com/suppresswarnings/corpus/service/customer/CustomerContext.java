/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.customer;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.Format;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class CustomerContext extends WXContext {
	public static final String CMD = "商铺客服";
	String qrScene = null;
	String shopName = null;
	String shopGoods = null;
	Format shopFormat = null;
	
	public String[] interested(String input) {
		String[] result = new String[2];
		if(shopFormat == null) {
			result[0] = "Quiz";
		} else {
			List<KeyValue> match = shopFormat.matches(input);
			if(match.isEmpty()) {
				result[0] = "Fail";
			} else {
				KeyValue goods = match.get(0);
				result[0] = "Have";
				result[1] = goods.value();
			}
		}
		return result;
	}
	State<Context<CorpusService>> assistant = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4679875981273935166L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(CMD.equals(t)) {
				u.output("查看你关注了几家商铺，如果可以每个店里查一下，查到了就说，查不到就让你选择一个店。");
			} else if(t.startsWith("SCAN_")) {
				qrScene = t.substring("SCAN_".length());
				
				//build format
				String HB_HOME = System.getenv("HB_HOME");
				String shopPath = HB_HOME + "/shop/" + qrScene;
				File shopFile = new File(shopPath, "format.line");
				if(shopFile.exists()) {
					try {
						shopFormat = new Format();
						Files.lines(shopFile.toPath()).filter(line -> line.length() > 1).forEach(line -> shopFormat.compile(line));
					} catch (Exception e) {
						shopFormat = null;
						logger.info("[customer] shop file fails to compile");
					}
				} else {
					logger.info("[customer] shop file not exists");
				}
				
				String welcome = "";
				//check
				String myShopKey = String.join(Const.delimiter, Const.Version.V1, openid(), "MyShop", qrScene);
				String myShop = u.content().account().get(myShopKey);
				if(myShop == null) {
					u.content().account().put(myShopKey, time());
					welcome = "欢迎初次关注本店";
				} else {
					welcome = "欢迎再次关注本店";
				}
				//name
				String nameKey = String.join(Const.delimiter, Const.Version.V1, "Shop.Name", qrScene);
				shopName = u.content().account().get(nameKey);
				if(shopName == null) shopName = "未知小店";
				else {
					String alterCommandKey = String.join(Const.delimiter, Const.Version.V1, openid(), "AlterCommand", shopName);
					String alterCommand = u.content().account().get(alterCommandKey);
					if(alterCommand == null) {
						u.content().account().put(alterCommandKey, qrScene);
					} else {
						if(alterCommand.contains(qrScene)) {
							//ignore the same
						} else {
							alterCommand = alterCommand + ";" + qrScene;
							u.content().account().put(alterCommandKey, alterCommand);
						}
					}
					u.appendLine(welcome + "'"+shopName+"', 今后你可以输入'"+shopName+"'重新进入本店");
				}
				
				String goodsKey = String.join(Const.delimiter, Const.Version.V1, "Shop.Goods", qrScene);
				shopGoods = u.content().account().get(goodsKey);
				if(shopGoods == null) shopGoods = "经营未知商品";
				else {
					u.appendLine("本店主要经营'"+shopGoods+"', 请直接输入商品名称，或者直接说你需要什么");
				}
			} else {
				String[] interested = interested(t);
				if("Have".equals(interested[0])) {
					u.output("我这正好有" + interested[1]);
				} else if("Fail".equals(interested[0])){
					u.output("请直接说商品名称：");
				} else if("Quiz".equals(interested[0])) {
					u.output("店主还没录入商品，我将为你转发：" + t);
				} else {
					u.output("很难理解这句话");
				}
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			logger.info("[customer] " + t);
			if("训练客服".equals(t)) {
				return train;
			}
			return assistant;
		}

		@Override
		public String name() {
			return "商铺客服";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	State<Context<CorpusService>> train = new State<Context<CorpusService>>() {
		final int INITIAL = -1, ANSWER = 1, QUESTION = 2, DONE = 3;
		int status = INITIAL;
		int index = 0;
		String current = null;
		/**
		 * 
		 */
		private static final long serialVersionUID = -2520059662255761593L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			
			if(status == QUESTION) {
				String answer = t;
				index ++;
				String qKey = String.join(Const.delimiter, Const.Version.V1, "Shop", "QA", qrScene, "Q", openid(), time(), ""+index);
				String aKey = String.join(Const.delimiter, Const.Version.V1, "Shop", "QA", qrScene, "A", openid(), time(), ""+index);
				u.content().data().put(qKey, current);
				u.content().data().put(aKey, answer);
				status = DONE;
			}
			
			if(status == ANSWER) {
				current = t;
				u.appendLine("该如何应对该咨询？");
				status = QUESTION;
			}
			
			if(status == INITIAL) {
				u.appendLine("教程：\n1.请向本店咨询商品或服务，（例如：请问你们店有包子卖吗？）\n2.该如何应对该咨询？(例如：查 某商品，或者：回 具体内容)\n\n请向本店咨询商品或服务：");
				status = ANSWER;
			}
			
			if(status == DONE) {
				u.appendLine("请向本店咨询商品或服务：");
				status = ANSWER;
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return train;
		}

		@Override
		public String name() {
			return "商铺训练";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	public CustomerContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = assistant;
	}

}
