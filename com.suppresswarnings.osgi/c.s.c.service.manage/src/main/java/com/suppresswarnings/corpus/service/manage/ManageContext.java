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
			u.output("    导入工作");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if("关闭测试".equals(t)) return examOff;
			if("打开测试".equals(t)) return examOn;
			if("导入工作".equals(t)) return examWork;
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
	State<Context<CorpusService>> examWork = new State<Context<CorpusService>>() {

		@Override
		public void accept(String t, Context<CorpusService> u) {
			int x = u.content().fillExam();
			u.output("已经导入 " + x + " 条问题和同义句任务");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return examManage;
		}

		@Override
		public String name() {
			return "导入工作";
		}

		@Override
		public boolean finish() {
			return true;
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
		Goods todo = null;
		AtomicInteger mode = new AtomicInteger(0);
		public String display(Goods todo) {
			StringBuffer sb = new StringBuffer();
			sb.append("标：" + todo.getTitle());
			sb.append("\n").append("价：" + todo.getPricecent() + "," + todo.getPriceagent() + "," + todo.getPricevip() + "," + todo.getPricesecret() + "(单位：分)");
			sb.append("\n").append("链：" + todo.getOuturl());
			sb.append("\n").append("识: " + todo.getGoodsid());
			return sb.toString();
		}
		
		State<Context<CorpusService>> goodsCreate = new State<Context<CorpusService>>() {

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.output("输入商品标题（一句话，一目了然）");
				if(todo == null) {
					todo = new Goods();
					String goodsid = time() + "_" + random();
					todo.setGoodsid(goodsid);
				}
				todo.setTime(time());
				if(mode.get() == 1) {
					u.output("旧：" + todo.getTitle());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
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
				if(!"不修改".equals(t)) todo.setTitle(t);
				u.output("输入商品外部链接地址");
				if(mode.get() == 1) {
					u.output("旧：" + todo.getOuturl());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsOuturl;
			}

			@Override
			public String name() {
				return "商品名称";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsOuturl = new State<Context<CorpusService>>() {

			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(!"不修改".equals(t)) todo.setOuturl(t);
				u.output("输入商品特色（比如：新西兰仓库，澳洲直邮）");
				if(mode.get() == 1) {
					u.output("旧：" + todo.getExtra());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsExtra;
			}

			@Override
			public String name() {
				return "商品特色";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsExtra = new State<Context<CorpusService>>() {

			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(!"不修改".equals(t)) todo.setExtra(t);
				u.output("输入商品库存");
				if(mode.get() == 1) {
					u.output("旧：" + todo.getQuota());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsQuota;
			}

			@Override
			public String name() {
				return "商品特色";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsQuota = new State<Context<CorpusService>>() {

			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(!"不修改".equals(t)) todo.setQuota(t);
				u.output("输入商品价格（单位：分）");
				if(mode.get() == 1) {
					u.output("旧：" + todo.getPricecent());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsPrice;
			}

			@Override
			public String name() {
				return "商品库存";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsPriceagent = new State<Context<CorpusService>>() {

			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(!"不修改".equals(t)) todo.setPriceagent(t);
				u.output("输入商品VIP价格（单位：分）");
				if(mode.get() == 1) {
					u.output("旧：" + todo.getPricevip());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsPricevip;
			}

			@Override
			public String name() {
				return "商品价钱Agent";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsPricevip = new State<Context<CorpusService>>() {

			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(!"不修改".equals(t)) todo.setPricevip(t);
				u.output("输入商品Secret价格（单位：分）");
				if(mode.get() == 1) {
					u.output("旧：" + todo.getPricesecret());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsPricesecret;
			}

			@Override
			public String name() {
				return "商品价钱VIP";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsPricesecret = new State<Context<CorpusService>>() {

			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(!"不修改".equals(t)) todo.setPricesecret(t);
				u.output("请上传一张商品图片");
				if(mode.get() == 1) {
					u.output("旧：" + todo.getImage());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsImage;
			}

			@Override
			public String name() {
				return "商品价钱Secret";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		
		State<Context<CorpusService>> goodsPrice = new State<Context<CorpusService>>() {

			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(!"不修改".equals(t)) todo.setPricecent(t);
				u.output("输入商品Agent价格（单位：分）");
				if(mode.get() == 1) {
					u.output("旧：" + todo.getPriceagent());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsPriceagent;
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
				if(!"不修改".equals(t)) todo.setImage(image);
				u.output("输入商品详细图片说明（连续上传图片）");
				if(mode.get() == 1) {
					u.output("旧：" + todo.getListimages());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsImagelist;
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
		
		State<Context<CorpusService>> goodsImagelist = new State<Context<CorpusService>>() {
			List<String> images = new ArrayList<>();
			@Override
			public void accept(String t, Context<CorpusService> u) {
				if("不修改".equals(t)) {
					u.output("详细描述图不修改，请输入'不修改'则结束");
					return;
				}
				String image = t;
				if(t.startsWith("IMAGE_")) {
					image = t.substring("IMAGE_".length());
				}
				images.add(image);
				u.output("请继续上传，输入'完成修改'则结束");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("不修改".equals(t)) {
					return goodsFinish;
				}
				
				if("完成修改".equals(t)) {
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
				u.output("商品详情");
				u.output(display(todo));
				u.output("请输入'确认'则保存至数据库");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("确认".equals(t)) return goodsConfirm;
				return goodsFinish;
			}

			@Override
			public String name() {
				return "查看商品数据";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		State<Context<CorpusService>> goodsConfirm = new State<Context<CorpusService>>() {

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.content().daigouHandler.saveMyGoods(openid(), todo);
				todo = null;
				mode.set(0);
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
		
		State<Context<CorpusService>> goodsModify = new State<Context<CorpusService>>() {

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.output("商品详情");
				u.output(display(todo));
				u.output("请输入'确认'进行修改产品信息，根据提示直接输入新内容，如果不需要修改，就输入'不修改'");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("确认".equals(t)) {
					mode.set(1);
					return goodsCreate;
				}
				mode.set(0);
				todo = null;
				return goodsManage;
			}

			@Override
			public String name() {
				return "修改商品数据";
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
						u.content().daigouHandler.fillGoodsWithAllPrice(goods, openid(), openid());
						u.content().daigouHandler.fillOuturl(goods);
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
				u.output("带参数的指令举例：修改商品1，下架商品3");
				u.output("id和商品信息如下");
				goodsMap.forEach((id, goods) ->{
					u.output("id: " + id);
					u.output(display(goods));
					u.output(" - - - ");
				});
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if("创建商品".equals(t)){
				logger.info("[manage] goods create");
				todo = null;
				return goodsCreate;
			} else if(t.startsWith("修改商品")) {
				logger.info("[manage] goods modify");
				String index = t.substring("修改商品".length());
				todo = goodsMap.get(index);
				return goodsModify;
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
