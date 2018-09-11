/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.daigou.Goods;

public class ManageContext extends WXContext {
	public static final String CMD = "我的后台管理";
	public static final String[] AUTH = {"Manage"};
	
	State<Context<CorpusService>> enter = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7540314541740975610L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("后台管理，等级森严，你可以输入以下指令：");
			u.output("    " + goodsManage.name());
			u.output("    " + examManage.name());
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(goodsManage.name().equals(t)) return goodsManage;
			if(examManage.name().equals(t)) return examManage;
			return enter;
		}

		@Override
		public String name() {
			return "后台管理入口";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<CorpusService>> examManage = new State<Context<CorpusService>>() {


		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("你可以输入");
			u.output("    关闭测试");
			u.output("    打开测试");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if("关闭测试".equals(t)) return examOff;
			if("打开测试".equals(t)) return examOn;
			return examManage;
		}

		@Override
		public String name() {
			return "测试管理";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<CorpusService>> examOff = new State<Context<CorpusService>>() {


		@Override
		public void accept(String t, Context<CorpusService> u) {
			String onKey = String.join(Const.delimiter, Const.Version.V2, "Collect", "Corpus", "ON");
			String on = u.content().data().get(onKey);
			if("off".equals(on)) {
				u.output("已经关闭");
			}else {
				u.content().data().put(onKey, "off");
				u.output("关闭测试了");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return examManage;
		}

		@Override
		public String name() {
			return "关闭测试";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<CorpusService>> examOn = new State<Context<CorpusService>>() {


		@Override
		public void accept(String t, Context<CorpusService> u) {
			String onKey = String.join(Const.delimiter, Const.Version.V2, "Collect", "Corpus", "ON");
			String on = u.content().data().get(onKey);
			if("on".equals(on)) {
				u.output("已经打开");
			} else {
				u.content().data().put(onKey, "on");
				u.output("打开测试了");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return examManage;
		}

		@Override
		public String name() {
			return "关闭测试";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<CorpusService>> goodsManage = new State<Context<CorpusService>>() {
		Goods todo;
		State<Context<CorpusService>> goodsCreate = new State<Context<CorpusService>>() {

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.output("输入商品标题（一句话，一目了然）");
				todo = new Goods();
				String goodsid = time() + "_" + random();
				todo.setGoodsid(goodsid);
				todo.setTime(time());
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return goodsName;
			}

			@Override
			public String name() {
				return "创建商品";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		State<Context<CorpusService>> goodsName = new State<Context<CorpusService>>() {

			@Override
			public void accept(String t, Context<CorpusService> u) {
				todo.setTitle(t);
				u.output("输入商品价格（单位：分）");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return goodsPrice;
			}

			@Override
			public String name() {
				return "商品价钱";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsPrice = new State<Context<CorpusService>>() {

			@Override
			public void accept(String t, Context<CorpusService> u) {
				todo.setPricecent(t);
				u.output("请上传一张商品图片");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return goodsImage;
			}

			@Override
			public String name() {
				return "商品图片";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsImage = new State<Context<CorpusService>>() {

			@Override
			public void accept(String t, Context<CorpusService> u) {
				String image = t;
				if(t.startsWith("IMAGE_")) {
					image = t.substring("IMAGE_".length());
				}
				todo.setImage(image);
				u.output("输入商品详细图片说明（连续上传图片）");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return goodsImagelist;
			}

			@Override
			public String name() {
				return "创建商品";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsImagelist = new State<Context<CorpusService>>() {
			List<String> images = new ArrayList<>();
			@Override
			public void accept(String t, Context<CorpusService> u) {
				String image = t;
				if(t.startsWith("IMAGE_")) {
					image = t.substring("IMAGE_".length());
				}
				images.add(image);
				u.output("请继续上传，输入'完成'则结束");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成".equals(t)) {
					todo.setListimages(String.join(",", images));
					images.clear();
					return goodsFinish;
				}
				return goodsImagelist;
			}

			@Override
			public String name() {
				return "商品图片描述";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsFinish = new State<Context<CorpusService>>() {

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.content().daigouHandler.saveMyGoods(openid(), todo);
				todo = null;
				u.output("已保存商品数据");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return goodsManage;
			}

			@Override
			public String name() {
				return "保存商品数据";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		Map<String, Goods> goodsMap;

		/**
		 * 
		 */
		private static final long serialVersionUID = -3941602152166765573L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(goodsMap == null) {
				goodsMap = new HashMap<>();
				String mygoodsidKey = String.join(Const.delimiter, Const.Version.V1, "Daigou", openid(), "Goodsid");
				AtomicInteger integer = new AtomicInteger(0);
				u.content().account().page(mygoodsidKey, mygoodsidKey, null, 1000, (x, key) -> {
					Goods goods = u.content().daigouHandler.getByGoodsid(key);
					if(goods != null) {
						goodsMap.put("" + integer.getAndIncrement(), goods);
					}
				});
			}
			if(goodsMap.isEmpty()) {
				u.output("你没有商品。你可以输入");
				u.output("    创建商品");
			} else {
				u.output("你可以输入");
				u.output("    创建商品");
				u.output("    修改商品+id");
				u.output("    下架商品+id");
				u.output("带参数的指令举例：修改商品1，下架商品3，id和商品如下");
				goodsMap.forEach((id, goods) ->{
					u.output(id + ". " + goods.toString());
				});
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if("创建商品".equals(t)){
				logger.info("[manage] goods create");
				return goodsCreate;
			} else if(t.startsWith("修改商品")) {
				logger.info("[manage] goods modify");
			} else if(t.startsWith("下架商品")) {
				logger.info("[manage] goods off");
			}
			return half;
		}

		@Override
		public String name() {
			return "商品管理";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<CorpusService>> half = new State<Context<CorpusService>>() {


		/**
		 * 
		 */
		private static final long serialVersionUID = 3437670267016168193L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("哈哈哈，还没实现");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return enter;
		}

		@Override
		public String name() {
			return "暂未实现";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	public ManageContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = enter;
	}

}
