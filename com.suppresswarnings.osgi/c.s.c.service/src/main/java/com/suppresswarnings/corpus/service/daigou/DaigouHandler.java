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
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.service.CorpusService;

public class DaigouHandler {
	Logger logger = LoggerFactory.getLogger("SYSTEM");
	CorpusService service;
	public DaigouHandler(CorpusService service) {
		this.service = service;
	}
	
	public Goods getByGoodsid(String goodsid) {
		Goods goods = new Goods();
		String keyPricecent = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Pricecent");
		String pricecent = service.account().get(keyPricecent);
		goods.setPricecent(pricecent);
		if(pricecent == null) {
			return null;
		}
		
		String keyImage = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Image");
		String image = service.account().get(keyImage);
		String keyTitle = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Title");
		String title = service.account().get(keyTitle);
		String keyExtra = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Extra");
		String extra = service.account().get(keyExtra);
		String keyQuota = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Quota");
		String quota = service.account().get(keyQuota);
		String keyListimages = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Listimages");
		String listimages = service.account().get(keyListimages);
		String keyTime = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Time");
		String time = service.account().get(keyTime);
		
		goods.setGoodsid(goodsid);
		goods.setExtra(extra);
		goods.setImage(image);
		goods.setListimages(listimages);
		goods.setQuota(quota);
		goods.setTime(time);
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
		service.account().put(goodsidKey, goods.getGoodsid());
		String myGoodsidKey = String.join(Const.delimiter, Const.Version.V1, "Daigou", openid, "Goodsid", goodsid);
		service.account().put(myGoodsidKey, goods.getGoodsid());
		
		String keyPricecent = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Pricecent");
		service.account().put(keyPricecent, goods.getPricecent());
		
		String keyImage = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Image");
		service.account().put(keyImage, goods.getImage());
		String keyTitle = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Title");
		service.account().put(keyTitle, goods.getTitle());
		String keyListimages = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Listimages");
		service.account().put(keyListimages, goods.getListimages());
		String keyTime = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Time");
		service.account().put(keyTime, goods.getTime());
		
		String keyExtra = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Extra");
		service.account().put(keyExtra, goods.getExtra());
		String keyQuota = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Quota");
		service.account().put(keyQuota, goods.getQuota());
		String keyState = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "Time");
		service.account().put(keyState, goods.getState());
	}
	
	public Cart getByOpenidCartid(String openid, String cartid, String wantedState) {
		String stateKey = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "State");
		String state = service.account().get(stateKey);
		logger.info("get Cart By Openid " + openid + ", Cartid " + cartid + ", wantedState " + wantedState + ", state " + state);
		if(wantedState.equals(state)) {
			String keyCount = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "Count");
			String count = service.account().get(keyCount);
			String keyGoodsid = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "Goodsid");
			String goodsid = service.account().get(keyGoodsid);
			if(goodsid == null || count == null) {
				logger.info(String.format("return null, goodsid:%s, count:%s", goodsid, count));
				return null;
			}
			String keyTime = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "Time");
			String time = service.account().get(keyTime);
			String keyAgentid = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "Agentid");
			String agentid = service.account().get(keyAgentid);
			
			Cart cart = new Cart();
			cart.setAgentid(agentid);
			cart.setOpenid(openid);
			cart.setCartid(cartid);
			cart.setState(state);
			cart.setCount(count);
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
			logger.info(String.format("compare, goodsid:%s, cart.goodsid:%s", goodsid, cart.getGoodsid()));
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
		//--->
		saveCart(cart);
		
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
	public String doneCart(String openid, String cartid) {
		String keyState = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "State");
		service.account().put(keyState, Cart.State.Done);
		return service.account().get(keyState);
	}
	public void saveCart(Cart cart) {
		String openid = cart.getOpenid();
		String cartid = cart.getCartid();
		String keyCount = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "Count");
		String keyGoodsid = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "Goodsid");
		String keyState = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "State");
		String keyTime = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "Time");
		String keyAgentid = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", openid, cartid, "Agentid");
		String keycartId = String.join(Const.delimiter, Const.Version.V1, "Daigou", openid, "Cartid", cartid);
		String keyOpenid = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Cart", "Openid", openid, cartid);
		service.account().put(keyTime, cart.getTime());
		service.account().put(keyCount, cart.getCount());
		service.account().put(keyGoodsid, cart.getGoodsid());
		service.account().put(keyState, cart.getState());
		service.account().put(keycartId, cartid);
		service.account().put(keyOpenid, openid);
		service.account().put(keyAgentid, cart.getAgentid());
	}

	public void saveOrder(Order order) {
		String orderid = order.getOrderid();
		String openid = order.getOpenid();
		List<Cart> carts = order.getCarts();
		
		String keymyOrderId = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, "Orderid", orderid);
		String keyOrderid = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", "Orderid", orderid);
		service.account().put(keymyOrderId, orderid);
		service.account().put(keyOrderid, orderid);
		String keyAddress = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Address");
		service.account().put(keyAddress, order.getAddress());
		String keyMobile = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Mobile");
		service.account().put(keyMobile, order.getMobile());
		String keyUsername = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Username");
		service.account().put(keyUsername, order.getUsername());
		String keyIdcard = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Idcard");
		service.account().put(keyIdcard, order.getIdcard());
		String keyComment = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Comment");
		service.account().put(keyComment, order.getComment());
		String keyOpenid = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Openid");
		service.account().put(keyOpenid, order.getOpenid());
		String keyTime = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Time");
		service.account().put(keyTime, order.getTime());
		String keyState = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "State");
		service.account().put(keyState, order.getState());
		String keyTotalcent = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Totalcent");
		service.account().put(keyTotalcent, order.getTotalcent());
		String keyGoodscount = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Goodscount");
		service.account().put(keyGoodscount, order.getGoodscount());
		String keyGoodstypes = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Goodstypes");
		service.account().put(keyGoodstypes, order.getGoodstypes());
		String keyDetail = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Detail");
		service.account().put(keyDetail, order.getDetail());
		for(Cart cart : carts) {
			String cartid = cart.getCartid();
			doneCart(openid, cartid);
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
		String address = service.account().get(keyAddress);//Address());
		String keyMobile = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Mobile");
		String mobile = service.account().get(keyMobile);//Mobile());
		String keyUsername = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Username");
		String username = service.account().get(keyUsername);//Username());
		String keyComment = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Comment");
		String comment = service.account().get(keyComment);//Comment());
		String keyTime = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Time");
		String time = service.account().get(keyTime);//Time());
		String keyState = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "State");
		String state = service.account().get(keyState);//State());
		String keyTotalcent = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Totalcent");
		String totalcent = service.account().get(keyTotalcent);//Totalcent());
		String keyGoodscount = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Goodscount");
		String goodscount = service.account().get(keyGoodscount);//Goodscount());
		String keyGoodstypes = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Goodstypes");
		String goodstypes = service.account().get(keyGoodstypes);//Goodstypes());
		String keyDetail = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Order", openid, orderid, "Detail");
		String detail = service.account().get(keyDetail);//Detail());
		
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
		return orders;
	}
}
