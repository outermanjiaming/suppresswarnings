package com.suppresswarnings.corpus.service.handlers;

import java.util.Base64;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.RequestHandler;
import com.suppresswarnings.corpus.service.http.CallablePost;
import com.suppresswarnings.corpus.service.wx.ShortUrl;
import com.suppresswarnings.osgi.network.http.Parameter;

public class ShortUrlHandlerFactory {

	/**
	 * 
	 * String access = u.content().accessToken("shorturl");
				String url = "https://api.weixin.qq.com/cgi-bin/shorturl?access_token=" + access;
				String json = "{\"action\":\"long2short\",\"long_url\":\""+longurl+"\"}";
				try {
					CallablePost post = new CallablePost(url, json);
					String ret = post.call();
					ShortUrl shorty = gson.fromJson(ret, ShortUrl.class);
					if(shorty.getErrcode() == 0) {
						String shorturls = shorty.getShort_url();
						u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "ShortUrl", "Long", longurl), shorturls);
						u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "ShortUrl", "Short", shorturls), longurl);
	 */
	static org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	
	static RequestHandler long2short = (param, service, args) ->{
		String access = service.accessToken("shorturl");
		String longurl = args[0];
		String url = "https://api.weixin.qq.com/cgi-bin/shorturl?access_token=" + access;
		String json = "{\"action\":\"long2short\",\"long_url\":\""+longurl+"\"}";
		String longkey = String.join(Const.delimiter, Const.Version.V1, "ShortUrl", "Long", longurl);
		String exist = service.account().get(longkey);
		if(service.isNull(exist)) {
			try {
				Gson gson = new Gson();
				CallablePost post = new CallablePost(url, json);
				String ret = post.call();
				ShortUrl shorty = gson.fromJson(ret, ShortUrl.class);
				if(shorty.getErrcode() == 0) {
					String shorturls = shorty.getShort_url();
					service.account().put(longkey, shorturls);
					service.account().put(String.join(Const.delimiter, Const.Version.V1, "ShortUrl", "Short", shorturls), longurl);
					return shorturls;
				}
			} catch(Exception e) {
				logger.error("fail to get short url", e);
			}
			return "fail";
		} else {
			return exist;
		}
	};
	public static String handle(Parameter parameter, CorpusService service) {
		String random = parameter.getParameter("random");
		if(random == null) {
			return "fail";
		}
		String CODE = parameter.getParameter("ticket");
		if(CODE == null) {
			return "fail";
		}
		String what = parameter.getParameter("what");
		if(what == null) {
			return "fail";
		}
		String link = parameter.getParameter("link");
		if(link == null) {
			return "fail";
		}
		
		Base64.Decoder decoder = Base64.getDecoder();
		String de = new String(decoder.decode(link.getBytes()));
		if("long2short".equals(what)) {
			logger.info(parameter.toString());
			return long2short.handler(parameter, service, de);
		}
		
		return RequestHandler.simple.handler(parameter, service);
	}
	
}
