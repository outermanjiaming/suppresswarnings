package com.suppresswarnings.corpus.service.handlers;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
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
	static Gson gson = new Gson();
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
	static RequestHandler minipay = (parameter, service, args) ->{
		logger.info("processing minipay");
		String result = args[0];
		String orderid = args[1];
		String openid = args[2];
		String goodsid = args[3];
		String cashfee = args[4];
		if("SUCCESS".equals(result)) {
			if(goodsid.startsWith("coin")) {
				String coinKey = String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", goodsid, "Coin");
				String coin = service.account().get(coinKey);
				String myFeeKey = String.join(Const.delimiter, Const.Version.V1, openid, "iBeacon", "MyCoin", "Fee", orderid);
				service.account().put(myFeeKey, cashfee);
				service.publish("corpus/mini/coin/" + openid, orderid + ";" + openid + ";" + openid + ";" + goodsid + ";充值金币" + coin + ";" + cashfee);
				if(!service.isNull(coin)) {
					//unlock the coin
					AtomicBoolean lock = service.switches("mycoin" + openid);
					lock.set(false);
					service.updateCoin(openid, Integer.parseInt(coin));
				} else {
					logger.error("coin is not set");
				}
			} else {
				//unlock the coin
				AtomicBoolean lock = service.switches("mycoin" + openid);
				lock.set(false);
				//every time you buy something, you got 1 coin
				service.updateCoin(openid, 1);
				String groupid = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Groupid"));
				String notice = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Notice"));
				if(goodsid.startsWith("packet")) {
					String count = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", goodsid, "Count"));
					AtomicBoolean redlock = service.switches("haspacket" + groupid);
					service.publish("corpus/mini/packet/" + groupid, orderid + ";" + openid + ";" + openid + ";" + goodsid + ";" + notice + ";" + cashfee);
					synchronized(redlock) {
						String packetKey = String.join(Const.delimiter, Const.Version.V1, "iBeacon", groupid, "Redpacket", orderid);
						service.account().put(packetKey, count);
						//this is I have sent, not I owned
						String sentPacketKey = String.join(Const.delimiter, Const.Version.V1, openid, "iBeacon", "Sent", "Redpacket", groupid, orderid);
						service.account().put(sentPacketKey, count);
						redlock.set(false);
					}
				} else if(goodsid.startsWith("gift")) {
					String userid = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Userid"));
					String sendGiftKey = String.join(Const.delimiter, Const.Version.V1, "iBeacon", openid, "Gift", "Send", orderid);
					String haveGiftKey = String.join(Const.delimiter, Const.Version.V1, "iBeacon", userid, "Gift", "Have", orderid);
					service.account().put(sendGiftKey, userid+ ";" + openid+ ";" + goodsid + ";" + notice);
					service.account().put(haveGiftKey, userid+ ";" + openid+ ";" + goodsid + ";" + notice);
					service.publish("corpus/mini/gift/" + groupid, orderid + ";" + openid + ";" + userid + ";" + goodsid + ";" + notice + ";" + cashfee);
				}
			}
				
			
			return "success";
		} else {
			
			return "fail";
		}
	};
	
	static RequestHandler reserve = (parameter, service, args) ->{
		logger.info("processing reserve");
		String result = args[0];
		String orderid = args[1];//can get everything we need
		String openid = args[2];//who paid this
		String goodsid = args[3];//here must be someone's openid
		String cashfee = args[4];
		if("SUCCESS".equals(result)) {
			String userid = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Userid"));
			String notice = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Notice"));
			String groupid = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Groupid"));
			String sendReserveKey = String.join(Const.delimiter, Const.Version.V1, "iBeacon", openid, "Reserve", "Send", orderid);
			String haveReserveKey = String.join(Const.delimiter, Const.Version.V1, "iBeacon", userid, "Reserve", "Have", orderid);
			service.account().put(sendReserveKey, userid+ ";" + openid+ ";" + goodsid + ";" + notice);
			service.account().put(haveReserveKey, userid+ ";" + openid+ ";" + goodsid + ";" + notice);
			service.publish("corpus/mini/reserve/" + groupid, orderid + ";" + openid + ";" + userid + ";" + goodsid + ";" + notice + ";" + cashfee);
			return "success";
		} else {
			
			return "fail";
		}
	};
	
	static RequestHandler sponsor = (parameter, service, args) ->{
		String result = args[0];
		String orderid = args[1];
		String openid = args[2];
		String state = args[3];
		String cashfee = args[4];
		if("SUCCESS".equals(result)) {
			String projectid = orderid;
			if(state.contains("_Template_")) {
				projectid = state.split("_Template_")[1];
			}
			service.account().put(String.join(Const.delimiter, Const.Version.V2, "Project", "Sponsor", projectid), cashfee);
			service.atUser(openid, "谢谢，你成功赞助了"+cashfee+"分，赞助金额将鼓励用户关注点赞！");
			return "success";
		} else {
			service.atUser(openid, "你赞助支付失败");
			return "fail";
		}
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
				String info = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", goodid, "What"));
				service.atUser(openid, "恭喜你，你已经开通权限：" + info);
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
			if(!equal) {
				return "fail";
			}
			String attach = map.get("attach");
			String orderid = map.get("out_trade_no");
			String openid = map.get("openid");
			String result = map.get("result_code");
			String cashfee = map.get("cash_fee");
			String transactionid = map.get("transaction_id");
			double fee = Double.valueOf(cashfee) / 100;
			String money = fee + "元";
			String goodid = attach;
			if(attach != null) {
				if(attach.contains("_Template_")) {
					goodid = attach.split("_Template_")[0];
				}
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
			} else if(orderid.startsWith("Like")) {
				logger.info("赞助金额：" + cashfee + " for " + goodid);
				return sponsor.handler(parameter, service, result, orderid, openid, attach, cashfee);
			} else if(orderid.startsWith("mini")) {
				minipay.handler(parameter, service, result, orderid, openid, attach, cashfee);
				return "";
			} else if(orderid.startsWith("reserve")) {
				reserve.handler(parameter, service, result, orderid, openid, attach, cashfee);
				return "";
			} else {
				return RequestHandler.simple.handler(parameter, service);
			}
		} catch (Exception e) {
			logger.error("fail to handle notify", e);
			return RequestHandler.simple.handler(parameter, service);
		}
	}
}
