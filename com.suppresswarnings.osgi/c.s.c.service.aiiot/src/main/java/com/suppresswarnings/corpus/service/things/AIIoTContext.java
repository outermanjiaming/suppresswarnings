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
		String info = null;
		String mine = null;
		String ownerid = null;
		String keyMine = null;
		String keyInfo = null;
		String keyOwner = null;
		String keyCmds = null;
		String cmds = null;
		State<Context<CorpusService>> bind = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 232704324972443142L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(mine == null || "None".equals(mine)) {
					//new things register
					mine = time();
					//3.check owner
					if(ownerid != null && !"None".equals(ownerid)) {
						if(ownerid.equals(openid())) {
							//save owner
							u.content().account().put(keyMine, mine);
							u.output("你绑定了该设备: " + info);
						} else {
							//ask the owner to accept it
							//1.send text to owner
							//2.ask owner first
							
							//1.send text to owner
							u.content().sendTxtTo("ask auth aiiot", user().getNickname() + "请求控制设备:" + info+",如果你同意，请扫码并输入「解除绑定」", ownerid);
							u.output("已经通知主人你想绑定该设备，让主人扫码并输入「解除绑定」");
							return;
						}
					} else {
						//new things registered
						u.content().account().put(keyOwner, openid());
						u.content().account().put(keyMine, mine);
						String[] commands = cmds.split(";");
						for(String cmd : commands) {
							String keyCmd = String.join(Const.delimiter, Const.Version.V1, openid(), "AIIoT", cmd);
							String exist = u.content().account().get(keyCmd);
							if(exist != null) {
								if(exist.contains(code)) {
									//
									logger.info("[AIIoTContext] no need to add");
								} else {
									u.content().account().put(keyCmd, exist + ";" + code);
								}
							} else {
								u.content().account().put(keyCmd, code);
							}
						}
						u.output("你绑定了该设备，你可以通过命令进行控制: " + cmds);
					}
				} else {
					u.output("该设备好像不属于任何人：" + info);
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return init;
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
				if(info == null) {
					u.output("未知设备");
				}
				
				if(mine != null) {
					u.content().account().put(keyMine, "None");
					//TODO usage inform
					u.output("你解除了该设备：" + info);
				} else {
					u.output("设备信息："  + info);
				}
				
				if(ownerid != null) {
					if(ownerid.equals(openid())) {
						//delete owner
						u.content().account().put(keyOwner, "None");
						u.output("你是设备的主人，你解除绑定之后，别人可以绑定该设备");
					} else {
						u.content().atUser(ownerid, "用户正在解除绑定设备：" + info);
						u.output("已经通知设备的主人");
						return;
					}
				}
				String left = u.content().account().get(keyMine);
				if(left == null || "None".equals(left)) {
					u.output("已经解除绑定：" + info);
				} else {
					u.content().account().put(keyMine, "None");
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return init;
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
			keyInfo = String.join(Const.delimiter, Const.Version.V1, "AIIoT", "Info", code);
			info = u.content().account().get(keyInfo);
			u.output("设备信息：" + info);
			
			keyMine = String.join(Const.delimiter, Const.Version.V1, openid(), "AIIoT", code);
			mine = u.content().account().get(keyMine);
			logger.info("[AIIoTContext] Key: " + keyMine + " => " + mine);
			
			keyOwner = String.join(Const.delimiter, Const.Version.V1, "AIIoT", "Owner", code);
			ownerid = u.content().account().get(keyOwner);
			logger.info("[AIIoTContext] owner : " + keyOwner + " => " + ownerid);
			
			keyCmds = String.join(Const.delimiter, Const.Version.V1, "AIIoT", "CMD", code);
			cmds = u.content().account().get(keyCmds);
			logger.info("[AIIoTContext] owner : " + keyOwner + " => " + ownerid);
			if(mine == null || "None".equals(mine)) {
				bind.accept(t, u);
				state(init);
			} else {
				u.output("你已经绑定该设备，无须重复扫码，直接输入命令["+cmds+"]即可。如果你需要解绑可以输入：解除绑定。");
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
			return init;
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
	
	public AIIoTContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = scan;
	}

}
