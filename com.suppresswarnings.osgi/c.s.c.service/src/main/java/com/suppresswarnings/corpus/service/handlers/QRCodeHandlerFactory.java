package com.suppresswarnings.corpus.service.handlers;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.RequestHandler;
import com.suppresswarnings.osgi.network.http.Parameter;

public class QRCodeHandlerFactory {
	static RequestHandler likeshare = (param, service, args) ->{
		String projectid = param.getParameter("projectid");
		String openid = args[0];
		String accessToken = service.accessToken("like share");
		String qrScene = "T_Like_Share$" + projectid + "$" + openid;
		service.account().put(String.join(Const.delimiter, Const.Version.V1, "QRCode", qrScene), qrScene);
		service.setGlobalCommand(qrScene, "我要发起点赞", openid, "" + System.currentTimeMillis());
		return service.qrCode(accessToken, 120, "QR_STR_SCENE", qrScene);
	};
	static RequestHandler joinnearby = (param, service, args) ->{
		String offerid = param.getParameter("offerid");
		String referid = param.getParameter("referid");
		String openid = param.getParameter("openid");
		String dealid = param.getParameter("dealid");
		String accessToken = service.accessToken("joinnearby");
		String qrScene = "T_Joinnearby_$" + offerid + "$" + referid + "$" + dealid;
		service.account().put(String.join(Const.delimiter, Const.Version.V1, "QRCode", qrScene), qrScene);
		service.setGlobalCommand(qrScene, "我的商铺", openid, "" + System.currentTimeMillis());
		return service.qrCode(accessToken, 7200, "QR_STR_SCENE", qrScene);
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
		if("joinnearby".equals(what)) {
			return joinnearby.handler(parameter, service);
		}
		String code2OpenIdKey = String.join(Const.delimiter, Const.Version.V1, "To", "OpenId", CODE);
		String openid = service.token().get(code2OpenIdKey);
		if(openid == null) {
			return "fail";
		}
		
		if("likeshare".equals(what)) {
			return likeshare.handler(parameter, service, openid);
		}
		return RequestHandler.simple.handler(parameter, service);
	}

}
