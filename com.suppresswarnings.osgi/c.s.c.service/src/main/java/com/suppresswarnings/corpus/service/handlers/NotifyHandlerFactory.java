package com.suppresswarnings.corpus.service.handlers;

import java.util.Map;
import java.util.Random;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.RequestHandler;
import com.suppresswarnings.corpus.service.daigou.Order;
import com.suppresswarnings.corpus.service.sdk.WXPay;
import com.suppresswarnings.corpus.service.sdk.WXPayConfig;
import com.suppresswarnings.corpus.service.sdk.WXPayConfigImpl;
import com.suppresswarnings.corpus.service.sdk.WXPayUtil;
import com.suppresswarnings.corpus.service.sdk.WXPayConstants.SignType;
import com.suppresswarnings.osgi.network.http.Parameter;

public class NotifyHandlerFactory {
	static org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	static RequestHandler invest = (parameter, service, args) ->{
		String result = args[0];
		String orderid = args[1];
		String openid = args[2];
		String cashfee = args[3];
		String projectid = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Orderid", "Projectid", orderid));
		if("SUCCESS".equals(result)) {
			logger.info("invest success: " + projectid);
			if(projectid != null) {
				String user = service.getWXuserByOpenId(openid).toString();
				service.atUser("oDqlM1TyKpSulfMC2OsZPwhi-9Wk", "投资金额："+cashfee+"分\n投资用户："+user);
				service.account().put(String.join(Const.delimiter, Const.Version.V1, "Invest", projectid, orderid, openid), cashfee);
				service.account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Invest", "Openid", orderid), openid);
				service.account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Invest", "Projectid", orderid), projectid);
				service.account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Invest", "User", orderid), user);
				service.account().put(String.join(Const.delimiter, Const.Version.V1, openid, "Invest", projectid, orderid), cashfee);
			} else {
				logger.error("projectid is null");
			}
			service.atUser(openid, "恭喜你投资点赞项目成功！当项目点赞达到目标之后，会将投资回报通过企业付款发送到你的微信零钱。");
		} else {
			logger.error("invest fail");
		}
		return "success";
	};
	
	static RequestHandler daigou = (parameter, service, args) ->{
		String result = args[0];
		String orderid = args[1];
		String openid = args[2];
		if("SUCCESS".equals(result)) {
			service.daigouHandler.updateOrderState(orderid, openid, Order.State.Paid);
			service.daigouHandler.afterPaidAtAgentAndUser(orderid, openid);
		} else {
			service.daigouHandler.updateOrderState(orderid, openid, Order.State.Failed);
		}
		return "success";
	};
	
	static RequestHandler auth = (parameter, service, args) ->{
		String result = args[0];
		String orderid = args[1];
		String openid = args[2];
		String goodid = args[3];
		
		if("SUCCESS".equals(result)) {
			String paidKey = String.join(Const.delimiter, Const.Version.V1, "Paid", goodid, openid);
			service.account().put(paidKey, "Paid");
			//authorize 
			if(goodid != null) {
				String authKey = String.join(Const.delimiter, Const.Version.V1, "Info", "Auth", goodid, openid);
				String value = System.currentTimeMillis() + "." + orderid;
				service.account().put(authKey, value);
				logger.info("[notify] lijiaming: authorize " + openid + " with " + goodid + ", orderid: " + orderid + ", newState: Paid");
			}
		} else {
			logger.error("fail to pay auth: " + openid + ", orderid: " + orderid);
		}
		
		return "success";
	};
	public static String handle(Parameter parameter, CorpusService service) {
		try {
			String postbody = parameter.getParameter(Parameter.POST_BODY);
			logger.info("postbody = " + postbody);
			Map<String, String> map = WXPayUtil.xmlToMap(postbody);
			logger.info("notify map: " + map.toString());
			WXPayConfig config = new WXPayConfigImpl();
			WXPay wxPay = new WXPay(config);
			String check = wxPay.sign(map, SignType.HMACSHA256);
			String sign = map.get("sign");
			boolean equal = check.equals(sign);
			logger.info("[corpus] lijiaming: equal = " + equal + "\ncheck = " + check + "\nsign=" + sign);
			if(!equal) {
				return "fail";
			}
			String goodid = map.get("attach");
			String orderid = map.get("out_trade_no");
			String openid = map.get("openid");
			String result = map.get("result_code");
			String cashfee = map.get("cash_fee");
			String transactionid = map.get("transaction_id");
			double fee = Double.valueOf(cashfee) / 100;
			String money = fee + "元";
			
			if(goodid != null) {
				service.account().put(String.join(Const.delimiter, Const.Version.V1, "Paid", goodid, openid, orderid), "" + System.currentTimeMillis());
				service.account().put(String.join(Const.delimiter, Const.Version.V1, openid, "Paid", goodid, orderid), "" + System.currentTimeMillis());
				String keyBossid = String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", goodid, "Bossid");
				String bossid = service.account().get(keyBossid);
				if(bossid != null) {
					service.atUser(bossid, "收款成功：" + money);
				} else {
					logger.info("[notify] bossid is null, orderid: " + orderid);
				}
			}
			
			String keyState = String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "State");
			String state = service.account().get(keyState);
			logger.info("old state: " + state);
			if("SUCCESS".equals(result)) {
				state =  "Paid" ;
			} else {
				state =  "Fail" ;
				service.atUser(openid, "支付失败，请稍后再试，或联系「素朴网联」客服，电话：13263267623，邮箱：13263267623@163.com，或直接在公众号「素朴网联」输入：我要人工客服");
			}
			service.account().put(keyState,  state);
			service.account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Transactionid"), transactionid);
			service.account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Notify"), postbody);
			long current = System.currentTimeMillis();
			int random = new Random().nextInt(100000);
			String ip = parameter.getParameter(Parameter.COMMON_KEY_CLIENT_IP);
			service.account().put(String.join(Const.delimiter, Const.Version.V1, "Notify", ""+current, ""+random, ip), postbody);
			
			if(orderid.startsWith("Invest")) {
				return invest.handler(parameter, service, result, orderid, openid, cashfee, goodid);
			} else if(orderid.startsWith("DG")) {
				return daigou.handler(parameter, service, result, orderid, openid);
			} else if(orderid.startsWith("Auth")) {
				return auth.handler(parameter, service, result, orderid, openid, goodid);
			} else if(orderid.startsWith("Software")) {
				String code = service.generateActivateCode(openid);
				service.atUser(openid, code);
				return "success";
			}else {
				return RequestHandler.simple.handler(parameter, service);
			}
		} catch (Exception e) {
			logger.error("fail to handle notify", e);
			return RequestHandler.simple.handler(parameter, service);
		}
	}
}
