package com.suppresswarnings.corpus.service.game;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.service.CorpusService;

public class Ally {
	String openid;
	String qrScene;
	String ownerid;
	Context<CorpusService> context;
	WaitingRoom room;
	
	@Override
	public String toString() {
		return "Ally [openid=" + openid + ", qrScene=" + qrScene + ", ownerid=" + ownerid + ", context=" + context
				+ "]";
	}
	
}
