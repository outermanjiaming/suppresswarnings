/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.things;

import java.util.List;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class AIIoTContext extends WXContext {
	public static final String CMD = "智能家居设备";
	
	String code = null;
	State<Context<CorpusService>> scan = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 732776875790276170L;
		boolean seen = false;
		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("请通过扫描智能家居设备上的二维码进入");
			seen = true;
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			logger.info("[AIIoTContext] input " + t);
			if(t.startsWith("SCAN_")) {
				code = t.substring("SCAN_".length());
				return things;
			}
			if(seen) {
				seen = false;
				return init;
			}
			return scan;
		}

		@Override
		public String name() {
			return "扫码进入智能家居设备";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<CorpusService>> things = new State<Context<CorpusService>>() {
		String type = null;
		List<String> cmds = null;
		State<Context<CorpusService>> bind = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 232704324972443142L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				String keyMine = String.join(Const.delimiter, Const.Version.V1, openid(), "AIIoT", code);
				String mine = u.content().account().get(keyMine);
				if(mine == null) {
					//new things register
					mine = time();
					//3.check owner
					String ownerKey = String.join(Const.delimiter, Const.Version.V1, "AIIoT", "Owner", code);
					String ownerid = u.content().account().get(ownerKey);
					if(ownerid != null) {
						if(ownerid.equals(openid())) {
							//save owner
							u.content().account().put(keyMine, mine);
							u.output("你绑定了该设备");
						} else {
							//ask the owner to accept it
							//1.send text to owner
							//2.ask owner first
							u.output("请联系主人同意你绑定该设备");
							return;
						}
					} else {
						//new things registered
						u.content().account().put(ownerKey, openid());
						u.content().account().put(keyMine, mine);
						String keyCMDFormat = String.join(Const.delimiter, Const.Version.V1, openid(), "AIIoT", "%s");
						for(String cmd : cmds) {
							String keyCmd = String.format(keyCMDFormat, cmd);
							u.content().account().put(keyCmd, code);
						}
						u.output("你已经绑定该设备，你可以通过命令进行控制：" + cmds.toString());
					}
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return done;
			}

			@Override
			public String name() {
				return "绑定设备";
			}

			@Override
			public boolean finish() {
				return false;
			}
		};
		State<Context<CorpusService>> unbind = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 2708336035491268779L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.output("正在解除绑定");
				
				String keyType = String.join(Const.delimiter, Const.Version.V1, "AIIoT", "Type", code);
				String type = u.content().account().get(keyType);
				if(type == null) {
					u.output("未知设备代码");
					return;
				}
				//2.check mine
				List<String> cmds = u.content().aiiot.typesCMD.get(type);
				
				String keyMine = String.join(Const.delimiter, Const.Version.V1, openid(), "AIIoT", code);
				String mine = u.content().account().get(keyMine);
				if(mine != null) {
					u.content().account().del(keyMine);
					//TODO usage inform
					u.output("你解除了该设备");
				}
				
				String ownerKey = String.join(Const.delimiter, Const.Version.V1, "AIIoT", "Owner", code);
				String ownerid = u.content().account().get(ownerKey);
				if(ownerid != null) {
					if(ownerid.equals(openid())) {
						//delete owner
						u.content().account().del(ownerKey);
						u.output("你是设备的主人，你解除绑定之后，别人可以绑定该设备");
					} else {
						//1.send text to owner
						u.output("已经通知设备的主人");
						return;
					}
				}

				String keyCMDFormat = String.join(Const.delimiter, Const.Version.V1, openid(), "AIIoT", "%s");
				for(String cmd : cmds) {
					String keyCmd = String.format(keyCMDFormat, cmd);
					u.content().account().del(keyCmd);
				}
				u.output("已经解除绑定");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return done;
			}

			@Override
			public String name() {
				return "解除绑定";
			}

			@Override
			public boolean finish() {
				return true;
			}
		};
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -362991751462890914L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(code == null) {
				u.output("智能家居设备代码为空");
				return;
			}
			
			//1.check type
			String keyType = String.join(Const.delimiter, Const.Version.V1, "AIIoT", "Type", code);
			type = u.content().account().get(keyType);
			if(type == null) {
				u.output("未知设备代码");
				return;
			}
			cmds = u.content().aiiot.typesCMD.get(type);
			
			u.output("你可以输入：");
			
			String keyMine = String.join(Const.delimiter, Const.Version.V1, openid(), "AIIoT", code);
			String mine = u.content().account().get(keyMine);
			if(mine == null) {
				u.output("    绑定设备");
			} else {
				u.output("    解除绑定");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if("解除绑定".equals(t)) {
				return unbind;
			}
			if("绑定设备".equals(t)){
				return bind;
			}
			return things;
		}

		@Override
		public String name() {
			return "智能家居设备绑定或查看";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<CorpusService>> done = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8682282892710656687L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("现在试试几个对话或者命令吧");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "绑定完成";
		}

		@Override
		public boolean finish() {
			return true;
		}
	};
	
	public AIIoTContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = scan;
	}

}
