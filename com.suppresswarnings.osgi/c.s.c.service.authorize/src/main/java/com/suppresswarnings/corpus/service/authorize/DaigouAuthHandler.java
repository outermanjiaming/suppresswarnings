/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.authorize;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.wx.WXuser;

public class DaigouAuthHandler implements AuthHandler {
	public static final String[] INTEREST = {"DaigouAgent", "DaigouVIP"};
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	@Override
	public boolean apply(Context<CorpusService> service, String target, String openid, String userid, String time, String random) {
		if("DaigouVIP".equals(target)) {
			String vipKey = String.join(Const.delimiter, Const.Version.V1, "Daigou", "VIP", userid);
			service.content().account().put(vipKey, openid);
			String myVipKey = String.join(Const.delimiter, Const.Version.V1, userid, "Daigou", "VIP");
			service.content().account().put(myVipKey, time);
			return true;
		}
		
		if("DaigouAgent".equals(target)) {
			String agentKey = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Agent", userid);
			String myAgentKey = String.join(Const.delimiter, Const.Version.V1, userid, "Daigou", "Agent");
			
			WXuser user = service.content().getWXuserByOpenId(userid);
			if(user == null || user.getSubscribe() ==0) {
				logger.info("[daigouAuthHandler] you have to subscribe us first: " + userid);
				service.content().sendTxtTo("Auth DaigouAgent", "请首先关注公众号「素朴网联」", userid);
				return false;
			}
			
			String agentpageKey = String.join(Const.delimiter, Const.Version.V1, userid, "Daigou", "Agentpage");
			String agentpage = service.content().account().get(agentpageKey);
			
			if(agentpage == null) {
				//create page
				//and save
				String rootPath = System.getProperty("path.html");
				String filename = "daigou_" + userid + ".html";
				File file = new File(rootPath + filename);
				File daigou = new File(rootPath + "daigou.html");
				if(!daigou.exists()) {
					logger.info("[daigouAuthHandler] fail to get daigou.html as model");
					return false;
				}
				if(!file.exists()) {
					try {
						List<String> lines = Files.lines(Paths.get(daigou.getAbsolutePath())).map(line -> {
							if(line.contains("agentlijiaming")) {
								return line.replace("agentlijiaming", userid);
							}
							return line;
						}).collect(Collectors.toList());
						Files.write(Paths.get(file.getAbsolutePath()), lines, StandardOpenOption.CREATE_NEW);
					    service.content().account().put(agentpageKey, filename);

						service.content().account().put(agentKey, openid);
						service.content().account().put(myAgentKey, time);
						
					} catch (Exception e) {
						logger.error("[daigouAuthHandler] DaigouAgent fail to create agent page");
					}
				}
				StringBuffer sb = new StringBuffer("你的专属代购页面\n");
				sb.append("http://suppresswarnings.com/" + filename);
				sb.append("\n用户通过你的专属代购页面购买价格较低，用户下单并支付成功之后你将得到利润。");
				service.content().sendTxtTo("DaigouAgent", sb.toString(), userid);
			}
			
			return true;
		}
		
		return false;
	}
}
