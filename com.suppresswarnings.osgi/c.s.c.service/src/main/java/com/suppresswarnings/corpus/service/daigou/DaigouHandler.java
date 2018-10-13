/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.daigou;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.service.CorpusService;

public class DaigouHandler {
	Logger logger = LoggerFactory.getLogger("SYSTEM");
	CorpusService service;
	public DaigouHandler(CorpusService service) {
		this.service = service;
	}
	public boolean replacePricecentAgentPrice(List<Goods> goodslist, String agentid) {
		if(isAgent(agentid)) {
			for(Goods goods : goodslist) {
				String goodsid = goods.getGoodsid();
				String pricecent = goods.getPricecent();
				logger.info("[DaigouHandler] fill agent price: " + goodsid);
				String keyPriceagent = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Priceagent");
				String priceagent = service.account().get(keyPriceagent);
				goods.setPricecent(priceagent);
				goods.setPriceagent(pricecent);
				goods.setUsewhichprice(Cart.Reason.Agent);
			}
			return true;
		}
		return false;
	}
	
	public boolean replacePricecentAgentPrice(Goods goods, String agentid) {
		if(isAgent(agentid)) {
			String goodsid = goods.getGoodsid();
			String pricecent = goods.getPricecent();
			logger.info("[DaigouHandler] fill agent price: " + goodsid);
			String keyPriceagent = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Priceagent");
			String priceagent = service.account().get(keyPriceagent);
			goods.setPricecent(priceagent);
			goods.setPriceagent(pricecent);
			goods.setUsewhichprice(Cart.Reason.Agent);
			return true;
		}
		return false;
	}
	public boolean replacePricecentVIPPrice(Goods goods, String openid) {
		if(isVIP(openid)) {
			String goodsid = goods.getGoodsid();
			String pricecent = goods.getPricecent();
			logger.info("[DaigouHandler] fill agent price: " + goodsid);
			String keyPricevip = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Pricevip");
			String pricevip = service.account().get(keyPricevip);
			goods.setPricecent(pricevip);
			goods.setPricevip(pricecent);
			goods.setUsewhichprice(Cart.Reason.VIP);
			return true;
		}
		return false;
	}
	
	public boolean replacePricecentVIPPrice(List<Goods> goodslist, String openid) {
		if(isVIP(openid)) {
			for(Goods goods : goodslist) {
				String goodsid = goods.getGoodsid();
				String pricecent = goods.getPricecent();
				logger.info("[DaigouHandler] fill agent price: " + goodsid);
				String keyPricevip = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Pricevip");
				String pricevip = service.account().get(keyPricevip);
				goods.setPricecent(pricevip);
				goods.setPricevip(pricecent);
				goods.setUsewhichprice(Cart.Reason.VIP);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * just fill all prices into goods
	 * @param goods
	 * @param agentid
	 * @param openid
	 */
	public void fillGoodsWithAllPrice(Goods goods, String agentid, String openid) {
		String goodsid = goods.getGoodsid();
		String pricecent = goods.getPricecent();
		boolean isAdmin = service.isAdmin(openid, "DaigouHandler.fillGoodsWithAllPrice", "" + System.currentTimeMillis());
		logger.info("[DaigouHandler] fill all price goodsid: " + goodsid + ", pricecent: " + pricecent);
		if(isAdmin || isAgent(agentid)) {
			logger.info("[DaigouHandler] fill agent price: " + goodsid);
			String keyPriceagent = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Priceagent");
			String priceagent = service.account().get(keyPriceagent);
			goods.setPriceagent(priceagent);
		}
		
		if(isAdmin || isVIP(openid)) {
			logger.info("[DaigouHandler] fill vip price: " + goodsid);
			String keyPricevip = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Pricevip");
			String pricevip = service.account().get(keyPricevip);
			goods.setPricevip(pricevip);
		}
		
		if(isAdmin){
			logger.info("[DaigouHandler] fill secret price: " + goodsid);
			String keyPricesecret = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Pricesecret");
			String pricesecret = service.account().get(keyPricesecret);
			goods.setPricesecret(pricesecret);
		}
		
	}
	
	public Goods getByGoodsid(String goodsid) {
		String keyPricecent = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Pricecent");
		String pricecent = service.account().get(keyPricecent);
		if(pricecent == null) {
			return null;
		}
		
		String keyState = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "State");
		String state = service.account().get(keyState);
		if("DELETE".equals(state)) {
			logger.info("[DaigouHandler] get goods: never show deleted goods");
			return null;
		}
		
		Goods goods = new Goods();
		
		String keyImage = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Image");
		String keyTitle = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Title");
		String keyExtra = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Extra");
		String keyQuota = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Quota");
		String keyListimages = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Listimages");
		String keyTime = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Time");
		
		String image = service.account().get(keyImage);
		String title = service.account().get(keyTitle);
		String extra = service.account().get(keyExtra);
		String quota = service.account().get(keyQuota);
		String listimages = service.account().get(keyListimages);
		String time = service.account().get(keyTime);
		
		goods.setGoodsid(goodsid);
		goods.setPricecent(pricecent);
		//TODO lijiaming no need to fill other price?
		goods.setExtra(extra);
		goods.setImage(image);
		goods.setListimages(listimages);
		goods.setQuota(quota);
		goods.setTime(time);
		//TODO lack or sell
		goods.setState(state);
		goods.setTitle(title);
		return goods;
	}
	
	public List<Goods> listGoods() {
		List<Goods> list = new ArrayList<>();
		String listKey = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Goods");
		service.account().page(listKey, listKey, null, Integer.MAX_VALUE, (k,v) ->{
			String goodsid = v;
			Goods goods = getByGoodsid(goodsid);
			if(goods != null) {
				list.add(goods);
			}
		});
		return list;
	}
	
	public void saveMyGoods(String openid, Goods goods) {
		String goodsid = goods.getGoodsid();
		String goodsidKey = String.join(Const.delimiter, Const.Version.V1, "Daigou","Goods", goodsid);
		String myGoodsidKey = String.join(Const.delimiter, Const.Version.V1, "Daigou", openid, "Goodsid", goodsid);
		
		String keyPricecent = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Pricecent");
		String keyPriceagent = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Priceagent");
		String keyPricevip = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Pricevip");
		String keyPricesecret = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Pricesecret");
		
		String keyImage = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Image");
		String keyTitle = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Title");
		String keyListimages = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Listimages");
		String keyTime = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Time");
		String keyExtra = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Extra");
		String keyQuota = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Quota");
		String keyState = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Time");
		
		String keyOuturl = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Outurl");
		
		service.account().put(goodsidKey, goods.getGoodsid());
		service.account().put(myGoodsidKey, goods.getGoodsid());
		
		service.account().put(keyPricecent, goods.getPricecent());
		service.account().put(keyPriceagent, goods.getPriceagent());
		service.account().put(keyPricevip, goods.getPricevip());
		service.account().put(keyPricesecret, goods.getPricesecret());
		//TODO lijiaming for admin to make an order
		service.account().put(keyOuturl, goods.getOuturl());
		
		service.account().put(keyImage, goods.getImage());
		service.account().put(keyTitle, goods.getTitle());
		service.account().put(keyListimages, goods.getListimages());
		service.account().put(keyTime, goods.getTime());
		service.account().put(keyExtra, goods.getExtra());
		service.account().put(keyQuota, goods.getQuota());
		service.account().put(keyState, goods.getState());
	}
	
	public Cart getByOpenidCartid(String openid, String cartid, String wantedState) {
		String stateKey = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "State");
		String state = service.account().get(stateKey);
		logger.info("[daigouHandler] get Cart By Openid " + openid + ", Cartid " + cartid + ", wantedState " + wantedState + ", state " + state);
		if(wantedState.equals(state)) {
			String keyCount = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "Count");
			String keyGoodsid = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "Goodsid");
			
			String count = service.account().get(keyCount);
			String goodsid = service.account().get(keyGoodsid);
			if(goodsid == null || count == null) {
				logger.info(String.format("[daigouHandler] getByOpenidCartid return null, goodsid:%s, count:%s", goodsid, count));
				return null;
			}
			String keyTime = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "Time");
			String keyAgentid = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "Agentid");
			String keyActualPricecent = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "ActualPricecent");
			String keyActualReason = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "ActualReason");
			
			
			String time = service.account().get(keyTime);
			String agentid = service.account().get(keyAgentid);
			String actualpricecent = service.account().get(keyActualPricecent);
			String actualreason = service.account().get(keyActualReason);
			
			Cart cart = new Cart();
			cart.setAgentid(agentid);
			cart.setOpenid(openid);
			cart.setCartid(cartid);
			cart.setState(state);
			cart.setCount(count);
			cart.setActualpricecent(actualpricecent);
			cart.setActualreason(actualreason);
			cart.setGoodsid(goodsid);
			cart.setTime(time);
			return cart;
		}
		return null;
	}
	
	public Cart addGoodsToCart(String agentid, String openid, String goodsid) {
		Goods goods = getByGoodsid(goodsid);
		if(goods == null) {
			logger.info("goods not exists");
			return null;
		}
		List<Cart> carts = myCarts(openid);
		logger.info("my carts: " + carts.toString());
		for(Cart cart : carts) {
			logger.info(String.format("[daigouHandler] compare, goodsid:%s, cart.goodsid:%s", goodsid, cart.getGoodsid()));
			if(cart.goodsExist(goodsid)) {
				cart.addOne();
				//TODO bug:agentid is different maybe
				updateCartCount(openid, cart.getCartid(), cart.getCount());
				return cart;
			}
		}
		Cart cart = createWaitCartAndSave(agentid, openid, goodsid, "1");
		return cart;
	}
	
	public List<Cart> myCarts(String openid){
		
		List<Cart> carts = new ArrayList<>();
		List<String> ids = new ArrayList<>();
		String cartKey = String.join(Const.delimiter, Const.Version.V1, "Daigou", openid, "Cartid");
		service.account().page(cartKey, cartKey, null, Integer.MAX_VALUE, (k,v) ->{
			ids.add(v);
		});
		logger.info(openid + " carts id: " + ids.toString());
		for(String cartid : ids) {
			Cart cart = getByOpenidCartid(openid, cartid, Cart.State.Wait);
			if(cart != null) {
				carts.add(cart);
			}
		}
		return carts;
	}
	
	public void fillGoodsToCart(List<Cart> carts) {
		for(Cart cart : carts) {
			Goods goods = getByGoodsid(cart.goodsid);
			cart.setGoods(goods);
			logger.info("fill goods to cart: " + goods.toString());
		}
	}
	public void fillOuturl(Goods goods) {
		String goodsid = goods.getGoodsid();
		String keyOuturl = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Outurl");
		String outurl = service.account().get(keyOuturl);
		goods.setOuturl(outurl);
	}
	
	public void fillOuturl(boolean isAdmin, Order order) {
		if(!isAdmin) return;
		logger.info("[daigouHandler] fillOuturl: " + order.getOrderid());
		List<Cart> carts = order.getCarts();
		for(Cart cart : carts) {
			Goods goods = cart.getGoods();
			fillOuturl(goods);
		}
	}
	
	public boolean isVIP(String openid) {
		String vipKey = String.join(Const.delimiter, Const.Version.V1, "Daigou", "VIP", openid);
		String vip = service.account().get(vipKey);
		if(vip == null || "None".equals(vip)) {
			return false;
		}
		String myVipKey = String.join(Const.delimiter, Const.Version.V1, openid, "Daigou", "VIP");
		String vipTime = service.account().get(myVipKey);
		if(vipTime == null || "None".equals(vipTime)) {
			return false;
		}
		long time = Long.parseLong(vipTime);
		if(System.currentTimeMillis() - time > TimeUnit.DAYS.toMillis(365)) {
			logger.error("[daigouHandler] vip is out of time: " + time);
			return false;
		}
		return true;
	}
	
	public boolean isAgent(String agentid) {
		String agentKey = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Agent", agentid);
		String agent = service.account().get(agentKey);
		if(agent == null || "None".equals(agent)) {
			return false;
		}
		String myVipKey = String.join(Const.delimiter, Const.Version.V1, agentid, "Daigou", "Agent");
		String vipTime = service.account().get(myVipKey);
		if(vipTime == null || "None".equals(vipTime)) {
			return false;
		}
		long time = Long.parseLong(vipTime);
		if(System.currentTimeMillis() - time > TimeUnit.DAYS.toMillis(365)) {
			logger.error("[daigouHandler] agent is out of time: " + time);
			return false;
		}
		return true;
	}
	
	
	public Cart createWaitCartAndSave(String agentid, String openid, String goodsid, String count) {
		String time = "" + System.currentTimeMillis();
		String random = "." + new Random().nextInt(10000);
		String cartid = time + random;
		String state = Cart.State.Wait;
		Cart cart = new Cart();
		cart.setAgentid(agentid);
		cart.setOpenid(openid);
		cart.setCartid(cartid);
		cart.setState(state);
		cart.setCount(count);
		cart.setGoodsid(goodsid);
		cart.setTime(time);
		Goods goods = getByGoodsid(goodsid);
		fillGoodsWithAllPrice(goods, agentid, openid);
		boolean vip = false;
		boolean agent = false;
		//TODO lijiaming: check openid is vip first
		if(isVIP(openid)) {
			cart.setActualpricecent(goods.getPricevip());
			cart.setActualreason(Cart.Reason.VIP);
			vip = true;
		} else if(isAgent(agentid)){
			cart.setActualpricecent(goods.getPriceagent());
			cart.setActualreason(Cart.Reason.Agent);
			agent = true;
		} else {
			cart.setActualpricecent(goods.getPricecent());
			cart.setActualreason(Cart.Reason.Public);
		}
		
		
		//--->
		saveCart(cart, agent, vip);
		
		return cart;
	}
	public String updateCartCount(String openid, String cartid, String newcount) {
		String keyCount = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "Count");
		service.account().put(keyCount, newcount);
		return service.account().get(keyCount);
	}
	public String deleteCart(String openid, String cartid) {
		String keyState = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "State");
		service.account().put(keyState, Cart.State.Delete);
		return service.account().get(keyState);
	}
	/**
	 * click pay on cart page, create a order with all Wait carts, and update them to Done
	 * @param openid
	 * @param cartid
	 * @return
	 */
	public String doneCart(String orderid, Cart cart) {
		String cartid = cart.getCartid();
		String openid = cart.getOpenid();
		String agentid = cart.getAgentid();
		if(isAgent(agentid)) {
			//save profit for agent
			String agentCartidKey = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Profit", "Agent", "Cartid", "Orderid", agentid, cartid);
			service.account().put(agentCartidKey, orderid);
			
			String agentCartStateKey = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Profit", "Agent", "Cartid", "State", agentid, cartid);
			service.account().put(agentCartStateKey, "NOTPAY");
		}
		String keyState = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "State");
		service.account().put(keyState, Cart.State.Done);
		return service.account().get(keyState);
	}
	
	public void saveCart(Cart cart, boolean agent, boolean vip) {
		String openid = cart.getOpenid();
		String cartid = cart.getCartid();
		String keyCount = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "Count");
		String keyGoodsid = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "Goodsid");
		String keyState = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "State");
		String keyTime = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "Time");
		
		String keyActualPricecent = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "ActualPricecent");
		String keyActualReason = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "ActualReason");
		
		
		String keyAgentid = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "Agentid");
		String keycartId = String.join(Const.delimiter, Const.Version.V1, "Daigou", openid, "Cartid", cartid);
		String keyOpenid = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", "Openid", openid, cartid);
		//TODO lijiaming
		service.account().put(keyActualPricecent, cart.getActualpricecent());
		service.account().put(keyActualReason, cart.getActualreason());
		
		service.account().put(keyTime, cart.getTime());
		service.account().put(keyCount, cart.getCount());
		service.account().put(keyGoodsid, cart.getGoodsid());
		service.account().put(keyState, cart.getState());
		service.account().put(keycartId, cartid);
		service.account().put(keyOpenid, openid);
		service.account().put(keyAgentid, cart.getAgentid());
		logger.info("[daigouHandler] cart saved: " + cartid);
	}

	public void saveOrder(Order order) {
		String orderid = order.getOrderid();
		String openid = order.getOpenid();
		List<Cart> carts = order.getCarts();
		
		String keymyOrderId = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, "Orderid", orderid);
		String keyOrderid = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", "Orderid", "Openid", orderid);
		
		String keyAddress = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Address");
		String keyMobile = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Mobile");
		String keyUsername = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Username");
		String keyIdcard = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Idcard");
		String keyComment = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Comment");
		String keyOpenid = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Openid");
		String keyTime = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Time");
		String keyState = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "State");
		String keyTotalcent = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Totalcent");
		String keyGoodscount = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Goodscount");
		String keyGoodstypes = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Goodstypes");
		String keyDetail = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Detail");
		
		service.account().put(keyTime, order.getTime());
		service.account().put(keymyOrderId, orderid);
		//TODO important: list orderid and openid, then getBy(openid, orderid)
		service.account().put(keyAddress, order.getAddress());
		service.account().put(keyMobile, order.getMobile());
		service.account().put(keyUsername, order.getUsername());
		service.account().put(keyIdcard, order.getIdcard());
		service.account().put(keyComment, order.getComment());
		service.account().put(keyOpenid, order.getOpenid());
		service.account().put(keyState, order.getState());
		service.account().put(keyTotalcent, order.getTotalcent());
		service.account().put(keyOrderid, openid);
		service.account().put(keyGoodscount, order.getGoodscount());
		service.account().put(keyGoodstypes, order.getGoodstypes());
		
		service.account().put(keyDetail, order.getDetail());
		for(Cart cart : carts) {
			//it just means from cart to order, doesn't paid yet
			doneCart(orderid, cart);
			
			String cartid = cart.getCartid();
			String keyCartid = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Cartid", cartid);
			service.account().put(keyCartid, cartid);
		}
	}

	public void updateOrderState(String orderid, String openid, String paid) {
		String keyState = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "State");
		String oldState = service.account().get(keyState);
		logger.info("old state: " + oldState + ", openid: " + openid + ", orderid: " + orderid);
		service.account().put(keyState, paid);
	}
	
	public Order getByOpenidOrderid(String openid, String orderid) {
		Order order = new Order();
		order.setOpenid(openid);
		order.setOrderid(orderid);
		
        List<Cart> carts = new ArrayList<>();
		String keymyOrderId = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, "Orderid", orderid);
		String exist = service.account().get(keymyOrderId);
		if(exist == null) {
			return null;
		}
		
		String keyAddress = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Address");
		String keyMobile = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Mobile");
		String keyUsername = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Username");
		String keyComment = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Comment");
		String keyTime = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Time");
		String keyState = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "State");
		String keyTotalcent = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Totalcent");
		String keyGoodscount = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Goodscount");
		String keyGoodstypes = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Goodstypes");
		String keyDetail = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Detail");
		//////////
		String address = service.account().get(keyAddress);
		String mobile = service.account().get(keyMobile);
		String username = service.account().get(keyUsername);
		String comment = service.account().get(keyComment);
		String time = service.account().get(keyTime);
		String state = service.account().get(keyState);
		String totalcent = service.account().get(keyTotalcent);
		String goodscount = service.account().get(keyGoodscount);
		String goodstypes = service.account().get(keyGoodstypes);
		String detail = service.account().get(keyDetail);
		
		List<String> cartids = new ArrayList<>();
		String keyCartid = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Cartid");
		service.account().page(keyCartid, keyCartid, null, Integer.MAX_VALUE, (k, cartid) ->{
			cartids.add(cartid);
		});
		
		for(String cartid : cartids) {
			Cart cart = getByOpenidCartid(openid, cartid, Cart.State.Done);
			carts.add(cart);
		}
		fillGoodsToCart(carts);
		order.setAddress(address);
		order.setCarts(carts);
		order.setComment(comment);
		order.setGoodscount(goodscount);
		order.setGoodstypes(goodstypes);
		order.setMobile(mobile);
		order.setOpenid(openid);
		order.setOrderid(orderid);
		order.setState(state);
		order.setTime(time);
		order.setTotalcent(totalcent);
		order.setUsername(username);
		order.setDetail(detail);
		order.setCarts(carts);
		return order;
	}
	
	public List<Order> myOrders(String openid) {
		List<Order> orders = new ArrayList<>();
		String keymyOrderId = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, "Orderid");
		List<String> ids = new ArrayList<>();
		service.account().page(keymyOrderId, keymyOrderId, null, Integer.MAX_VALUE, (k, orderid) ->{
			ids.add(orderid);
		});
		for(String orderid : ids) {
			Order order = getByOpenidOrderid(openid, orderid);
			orders.add(order);
		}
		Collections.reverse(orders);
		return orders;
	}

	public List<Order> listOrders(String adminid) {
		boolean isAdmin = service.isAdmin(adminid, "daigouHandler.listOrders", ""+System.currentTimeMillis());
		if(!isAdmin) return null;
		
		String keyOrderids = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", "Orderid", "Openid");
		List<KeyValue> orderidOpenids = new ArrayList<>();
		service.account().page(keyOrderids, keyOrderids, null, Integer.MAX_VALUE, (keyOrderid, openid) ->{
			String orderid = keyOrderid.substring(keyOrderids.length() + 1);
			KeyValue orderidOpenid = new KeyValue(orderid, openid);
			orderidOpenids.add(orderidOpenid);
		});
		
		List<Order> orders = new ArrayList<>();
		for(KeyValue orderidOpenid : orderidOpenids) {
			Order order = getByOpenidOrderid(orderidOpenid.value(), orderidOpenid.key());
			fillOuturl(isAdmin, order);
			orders.add(order);
		}
		Collections.reverse(orders);
		return orders;
	}
}
