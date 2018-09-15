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

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.service.CorpusService;

public interface AuthHandler {
	public boolean apply(Context<CorpusService> service, String target, String openid, String userid, String time, String random);
}
