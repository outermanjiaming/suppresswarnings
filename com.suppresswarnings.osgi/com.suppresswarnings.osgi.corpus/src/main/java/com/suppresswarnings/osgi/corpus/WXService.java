package com.suppresswarnings.osgi.corpus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;
import com.suppresswarnings.osgi.data.Context;

public class WXService implements HTTPService {
	public static final String SUCCESS = "success";
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	Map<String, Context<?>> contexts = new ConcurrentHashMap<String, Context<?>>();
	Content ctx = new Content();
	
	@Override
	public String getName() {
		return "wx.http";
	}

	@Override
	public String start(Parameter arg0) throws Exception {
		
		String openid = arg0.getParameter("openid");
		Context<?> context = contexts.get(openid);
		if(context == null) {
			context = new WXContext(ctx, WXState.s0);
			contexts.put(openid, context);
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

}
