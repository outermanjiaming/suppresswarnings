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

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.service.CorpusService;

public class DaigouAuthHandler implements AuthHandler {
	public static final String[] INTEREST = {"DaigouAgent", "DaigouVIP"};
	
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
			String vipKey = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Agent", userid);
			service.content().account().put(vipKey, openid);
			String myVipKey = String.join(Const.delimiter, Const.Version.V1, userid, "Daigou", "Agent");
			service.content().account().put(myVipKey, time);
			return true;
		}
		
		return false;
	}
}
