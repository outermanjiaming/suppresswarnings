package com.suppresswarnings.osgi.corpus;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;
import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;
import com.suppresswarnings.osgi.data.ExampleContent;
import com.suppresswarnings.osgi.data.ExampleContext;
import com.suppresswarnings.osgi.data.ExampleState;

public class WXService implements HTTPService {
	public static final String SUCCESS = "success";
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	
	Content ctx = new Content();
	
	@Override
	public String getName() {
		return "wx.http";
	}

	@Override
	public String start(Parameter arg0) throws Exception {
		
		String openid = arg0.getParameter("openid");
		Context<?> context = ctx.get(openid);
		if(context == null) {
			context = new WXContext(ctx, WXState.s0);
			ctx.put(openid, context);
		}
		String text = arg0.getParameter("content");
		boolean finish = context.test(text);
		if(finish) {
			System.out.println("this stage finished: " + context.state());
		} else {
			return context.output();
		}
		
		return SUCCESS;
	}
	
	public static void main(String[] args) {
		WXService se = new WXService();
		Content ctx = new Content();
		WXContext a = new WXContext(ctx, WXState.s0);
		
		State<Context<ExampleContent>> init = new State<Context<ExampleContent>>() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 8838543767339719170L;

			@Override
			public State<Context<ExampleContent>> apply(String t, Context<ExampleContent> u) {
				if("final".equals(t)) 
					return ExampleState.Final;
				if("S0".equals(t))
					return ExampleState.S0;
				return this;
			}
			
			@Override
			public void accept(String t, Context<ExampleContent> u) {
				u.println(t + " = " + u.content().username);
			}
			
			@Override
			public String name() {
				return "hahaha this what";
			}

			@Override
			public boolean finish() {
				return false;
			}
		};
		ExampleContent content     = new ExampleContent();
		Context<?> b     = new ExampleContext(content, init);
		
		se.ctx.put("a", a);
		se.ctx.put("b", b);
		
		Context<?> x = se.ctx.get("b");
		boolean y = x.test("final");
		System.out.println(y);
		System.out.println(x);
	}

}
