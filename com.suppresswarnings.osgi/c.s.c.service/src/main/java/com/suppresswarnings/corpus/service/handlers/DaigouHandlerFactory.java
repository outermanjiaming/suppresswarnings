package com.suppresswarnings.corpus.service.handlers;

import java.util.List;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.RequestHandler;
import com.suppresswarnings.corpus.service.daigou.Cart;
import com.suppresswarnings.corpus.service.daigou.Goods;
import com.suppresswarnings.corpus.service.daigou.Order;
import com.suppresswarnings.corpus.service.wx.JsAccessToken;
import com.suppresswarnings.osgi.network.http.Parameter;

public class DaigouHandlerFactory {
	static org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	
	static RequestHandler manageOrders = (parameter, service, args) ->{
		Gson gson = new Gson();
		String state = parameter.getParameter("state");
		if(state == null) {
			logger.info("[daigou] state == null");
			return "fail";
		}
		String CODE = parameter.getParameter("ticket");
		//https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
		JsAccessToken accessToken = service.jsAccessToken(CODE);
		if(accessToken == null) {
			logger.info("[daigou] accessToken == null");
			return "fail";
		}
		String openId = accessToken.getOpenid();
		if(openId == null) {
			logger.info("[daigou] get openid failed");
			return "fail";
		}
		logger.info("[daigou] orders");
		if(!service.authrized(openId, "ManageOrders")) {
			logger.info("[daigou] check auth failed");
			return "fail";
		}
		return gson.toJson(service.daigouHandler.listOrders(openId));
	};
	
	static RequestHandler changeoOrderState = (parameter, service, args) ->{
		String userid = parameter.getParameter("userid");
		String orderid = parameter.getParameter("orderid");
		String newstate = parameter.getParameter("newstate");
		service.daigouHandler.updateOrderState(orderid, userid, newstate);
		return newstate;
	};
	
	static RequestHandler index = (parameter, service, args) ->{
		Gson gson = new Gson();
		logger.info("[daigou] index");
		String CODE = parameter.getParameter("ticket");
		String code2OpenIdKey = String.join(Const.delimiter, Const.Version.V1, "To", "OpenId", CODE);
		String openid = service.token().get(code2OpenIdKey);
		String agentid = parameter.getParameter("state");
		List<Goods> list  = service.daigouHandler.listGoods();
		if(service.daigouHandler.replacePricecentVIPPrice(list, openid)) {
			logger.info("[daigou] use vip price");
		} else if(service.daigouHandler.replacePricecentAgentPrice(list, agentid)){
			logger.info("[daigou] use agent price");
		}
		return gson.toJson(list);
	};
	public static String handle(Parameter parameter, CorpusService service) {
		String todo = parameter.getParameter("todo");
		if("manageorders".equals(todo)){
			return manageOrders.handler(parameter, service);
		}
		
		if("changeorderstate".equals(todo)) {
			return changeoOrderState.handler(parameter, service);
		}
		
		String CODE = parameter.getParameter("ticket");
		String code2OpenIdKey = String.join(Const.delimiter, Const.Version.V1, "To", "OpenId", CODE);
		String openid = service.token().get(code2OpenIdKey);
		if(openid == null) {
			logger.info("[daigou] openid == null");
			return "fail";
		}
		String agentid = parameter.getParameter("state");
		if(agentid == null) {
			logger.info("[daigou] state == null");
			return "fail";
		}
		/**
		 * TODO: Daigou
		 */
		if("index".equals(todo)) {
			return index.handler(parameter, service);
		}
		
		Gson gson = new Gson();
		String random = parameter.getParameter("random");
		String ip = parameter.getParameter(Parameter.COMMON_KEY_CLIENT_IP);
		if("addgoodstocart".equals(todo)) {
			logger.info("[daigou] addgoodstocart");
			String goodsid = parameter.getParameter("goodsid");
			if(goodsid == null) {
				logger.info("[daigou] goodsid == null");
				return "fail";
			}
			Cart cart = service.daigouHandler.addGoodsToCart(agentid, openid, goodsid);
			if(cart == null) {
				logger.info("[daigou] cart == null");
				return "fail";
			}
			return "success";
		}
		if("mycarts".equals(todo)) {
			logger.info("[daigou] mycarts");
			List<Cart> carts = service.daigouHandler.myCarts(openid);
			service.daigouHandler.fillGoodsToCart(carts);
			return gson.toJson(carts);
		}
		if("makeanorder".equals(todo)) {
			logger.info("[daigou] makeanorder");
			String username = parameter.getParameter("username");
			if(username == null || username.length() < 1) return "fail";
			String idcard = parameter.getParameter("idcard");
			if(idcard == null || idcard.length() < 15) return "fail";
			String mobile = parameter.getParameter("mobile");
			if(mobile == null || mobile.length() < 10) return "fail";
			String address = parameter.getParameter("address");
			if(address == null || address.length() < 3) return "fail";
			String comment = parameter.getParameter("comment");
			
			List<Cart> carts = service.daigouHandler.myCarts(openid);
			if(carts.isEmpty()) {
				logger.info("[lijiaming] 订单为空");
				return "fail";
			}
			service.daigouHandler.fillGoodsToCart(carts);
			StringBuffer sb = new StringBuffer();
			long totalcent = 0;
			int totalcount = 0;
			int totaltype = 0;
			for(Cart cart : carts) {
				int count = Integer.parseInt(cart.getCount());
				int price = Integer.parseInt(cart.getActualpricecent());
				long pricecent = price * count;
				totaltype = totaltype + 1;
				totalcent = totalcent + pricecent;
				totalcount = totalcount + count;
				sb.append("[").append(cart.getGoodsid()).append("*").append(cart.getCount()).append("] ");
			}
			double totalprice = totalcent * 0.01d;
			
			String openIdEnd = openid.substring(openid.length() - 7);
			String randEnd = random.substring(random.length() - 4);
			long current = System.currentTimeMillis();
			String orderid = "DG"+ current + openIdEnd + randEnd;
			sb.append(openid);
			String detail = sb.toString();
			String clientip = ip.split(",")[0];
			String body = orderid +" 共¥" + totalprice;
			String attach = openid;
			String prepay = service.prepay(orderid, body, detail, attach, ""+ totalcount, ""+totalcent, clientip, openid, current, "DG");
			if(prepay != null) {
				Order order = new Order();
				order.setAddress(address);
				order.setCarts(carts);
				order.setIdcard(idcard);
				order.setComment(comment);
				order.setGoodscount(""+totalcount);
				order.setGoodstypes(""+totaltype);
				order.setMobile(mobile);
				order.setOpenid(openid);
				order.setOrderid(orderid);
				order.setState(Order.State.Create);
				order.setTime(""+current);
				order.setTotalcent(""+totalcent);
				order.setUsername(username);
				order.setDetail(detail);
				service.daigouHandler.saveOrder(order);
				return prepay;
			} else {
				return "fail";
			}
		}
		if("goodsdetail".equals(todo)) {
			String goodsid = parameter.getParameter("goodsid");
			if(goodsid == null) {
				logger.info("[daigou] goodsid == null");
				return "fail";
			}
			Goods goods = service.daigouHandler.getByGoodsid(goodsid);
			if(service.daigouHandler.replacePricecentVIPPrice(goods, openid)) {
				logger.info("[daigou] use vip price");
			} else if(service.daigouHandler.replacePricecentAgentPrice(goods, agentid)){
				logger.info("[daigou] use agent price");
			}
			return gson.toJson(goods);
		}
		
		if("myorders".equals(todo)) {
			//TODO check openid
			logger.info("[daigou] myorders");
			List<Order> myOrders = service.daigouHandler.myOrders(openid);
			//TODO clean input for xss
			return gson.toJson(myOrders);
		}
		if("removecart".equals(todo)) {
			logger.info("[daigou] remove cart");
			String cartid = parameter.getParameter("cartid");
			if(cartid == null) {
				logger.info("[daigou] cartid == null");
				return "fail";
			}
			String deleted = service.daigouHandler.deleteCart(openid, cartid);
			if(!Cart.State.Delete.equals(deleted)) {
				return "fail";
			}
			return "success";
		}
		if("updatecartnum".equals(todo)) {
			logger.info("update cart num");
			String cartid = parameter.getParameter("cartid");
			if(cartid == null) {
				logger.info("cartid == null");
				return "fail";
			}
			String newnum = parameter.getParameter("newnum");
			if(newnum == null) {
				logger.info("newnum == null");
				return "fail";
			}
			try {
				Integer num = Integer.parseInt(newnum);
				if(num <= 0 || num > 10000) {
					return "fail";
				}
			} catch (Exception e) {
				logger.info("wrong number for cart: " + newnum + ", openid: " + openid + ", cartid: " + cartid);
				return "fail";
			}
			String result = service.daigouHandler.updateCartCount(openid, cartid, newnum);
			return result;
		} else {
			return RequestHandler.simple.handler(parameter, service);
		}
	}
}
