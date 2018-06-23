/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.warehouse;

import java.util.function.BiConsumer;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class WarehouseContext extends WXContext {
	public static final String CMD = "我的仓库";

	String theWarehouse = null;
	String goods = null;
	
	public State<Context<CorpusService>> start = new State<Context<CorpusService>>() {
		int status = 0;
		final int statusCreate = 1, statusQuery = 2;
		/**
		 * 
		 */
		private static final long serialVersionUID = 3195628413242412113L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String warehouseKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Warehouse");
			String warehouseName = u.content().data().get(warehouseKey);
			if(warehouseName != null) {
				String theWarehouseKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Warehouse", warehouseName);
				theWarehouse = u.content().data().get(theWarehouseKey);
				if(theWarehouse != null) {
					status = statusQuery;
					u.output("进入仓库：\n" + theWarehouse + "\n请输入要查询的商品：");
				} else {
					u.output("很奇怪怎么会没有仓库ID");
				}
			} else {
				status = statusCreate;
				u.output("你还没有仓库，现在创建。\n请问仓库叫什么名字:");
			}
			u.appendLine("(提示：你也可以输入'" + specify.name() + "')");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(exit(t, "我要退出")) {
				return init;
			} else if(specify.name().equals(t)) {
				return specify;
			} else if(manage.name().equals(t)) {
				return manage;
			} else if(list.name().equals(t)) {
				return list;
			} else if(status == statusQuery) {
				return query;
			} else if(status == statusCreate) {
				return create;
			}
			return this;
		}

		@Override
		public String name() {
			return "start warehouse";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	public State<Context<CorpusService>> list = new State<Context<CorpusService>>() {
		String head = null;
		int index = 0;
		/**
		 * 
		 */
		private static final long serialVersionUID = -5380139023473035972L;

		@Override
		public void accept(String inpt, Context<CorpusService> contxt) {
			StringBuffer sb = new StringBuffer();
			String begin = String.join(Const.delimiter, Const.Version.V1, "Warehouse", theWarehouse);
			if(head == null) {
				head = begin;
			}
			contxt.content().data().page(begin, head, null, 20, new BiConsumer<String, String>() {
				
				int length = begin.length();
				
				@Override
				public void accept(String t, String u) {
					index ++;
					head = t;
					sb.append(index + t.substring(length)).append("、");
				}
			});
			contxt.output(sb.toString());
			contxt.appendLine("你可以输入'下一页'继续\n或者输入商品进行查询");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if("下一页".equals(t)) {
				return list;
			}
			return query;
		}

		@Override
		public String name() {
			return "商品列表";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	public State<Context<CorpusService>> specify = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5380139023473035972L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(theWarehouse == null && specify.name().equals(t)) {
				u.output("请输入仓库ID：");
				return ;
			}
			
			String warehouseOpenidKey = String.join(Const.delimiter, Const.Version.V1, t);
			String warehouseOpenid = u.content().account().get(warehouseOpenidKey);
			if(warehouseOpenid == null) {
				u.output("请输入正确的仓库ID：");
				theWarehouse = null;
				return ;
			}
			theWarehouse = t.trim().toLowerCase();
			String warehouseKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Warehouse");
			String theWarehouseKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Warehouse", warehouseOpenid);
			
			u.content().data().put(warehouseKey, warehouseOpenid);
			u.content().data().put(theWarehouseKey, theWarehouse);
			u.output("进入仓库：\n" + theWarehouse + "\n请输入要查询的商品：");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(exit(t, "我要退出")) {
				return init;
			} else if(theWarehouse == null) {
				return this;
			}
			return query;
		}

		@Override
		public String name() {
			return "指定仓库";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	public State<Context<CorpusService>> query = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5380139023473035972L;
		
		@Override
		public void accept(String t, Context<CorpusService> u) {
			goods = t.trim().toLowerCase();
			String key = String.join(Const.delimiter, Const.Version.V1, "Warehouse", theWarehouse, goods);
			String value = u.content().data().get(key);
			if(value != null) {
				u.output("商品在：" + value);
			} else {
				u.output("不知道放在哪\n你可以输入'"+manage.name()+"'\n或输入'/商品位置'录入\n或继续查询其他商品");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(exit(t, "我要退出")) {
				return start;
			}
			if(manage.name().equals(t)) {
				return manage;
			} else if(t.startsWith("/")) {
				return record;
			}
			return this;
		}

		@Override
		public String name() {
			return "warehouse-" + theWarehouse;
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	public State<Context<CorpusService>> record = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1408593804375016735L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(goods == null || !t.startsWith("/")) {
				u.output("状态不对，商品是null或者输入格式不是/开头？");
			} else {
				String location = t.substring(1);
				String key = String.join(Const.delimiter, Const.Version.V1, "Warehouse", theWarehouse, goods);
				String exist = u.content().data().get(key);
				if(exist != null) {
					if(location.startsWith("更改")) {
						location = location.substring(2);
					} else {
						location = exist + " + " + location;
					}
					u.content().data().put(key, location);
					u.output("商品位置更新为："+ location +"\n'更改'开头的位置将直接替换");
				} else {
					u.content().data().put(key, location);
					u.output("商品位置记录完成");
				}
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return query;
		}

		@Override
		public String name() {
			return "记录位置";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	public State<Context<CorpusService>> manage = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1622178463272433378L;
		
		int status = 0;
		
		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(status == 0) {
				status = 1;
				u.output("请按照这种句式输入：\n商品/位置");
				return ;
			}
			String[] goodsLocation = t.split("/");
			if(goodsLocation.length != 2) {
				u.output("格式不正确，请确认输入格式为：\n商品/位置");
				return ;
			}
			String goods = goodsLocation[0].trim().toLowerCase();
			String location = goodsLocation[1].trim().toLowerCase();
			String key = String.join(Const.delimiter, Const.Version.V1, "Warehouse", theWarehouse, goods);
			String exist = u.content().data().get(key);
			if(exist != null) {
				if(location.startsWith("更改")) {
					location = location.substring(2);
				} else {
					location = exist + " + " + location;
				}
				u.content().data().put(key, location);
				u.output("商品位置更新为："+ location +"\n'更改'开头的位置将直接替换");
			} else {
				u.content().data().put(key, location);
				u.output("商品位置记录完成");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(exit(t, "我要退出")) {
				return start;
			}
			return manage;
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
	public State<Context<CorpusService>> create = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8362292968429947580L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String warehouseName = t;
			String warehouseKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Warehouse");
			String theWarehouseKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Warehouse", warehouseName);
			theWarehouse = String.join(Const.delimiter, warehouseName, time(), random());
			String warehouseOpenidKey = String.join(Const.delimiter, Const.Version.V1, theWarehouse);
			u.content().data().put(warehouseKey, warehouseName);
			u.content().data().put(theWarehouseKey, theWarehouse);
			u.content().account().put(warehouseOpenidKey, openid());
			
			u.output("仓库创建完成：\n" + theWarehouse + "\n因为新创建仓库\n所以直接到管理商品");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(exit(t, "我要退出")) {
				return init;
			}
			return manage;
		}

		@Override
		public String name() {
			return "create warehouse";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	public WarehouseContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = set(start);
	}
}
