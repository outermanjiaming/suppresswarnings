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

public class Cart {
	public static interface State {
		String Wait = "Wait";
		String Done = "Done";
		String Delete = "Delete";
	}
	public static interface Reason{
		String Public = "Public";
		String VIP = "VIP";
		String Agent = "Agent";
	}
	String agentid;
	String openid;
	String cartid;
	String state;
	String goodsid;
	String count;
	//TODO
	String actualpricecent;
	//TODO
	String actualreason;
	String time;
	Goods goods;
	
	
	public String getActualpricecent() {
		return actualpricecent;
	}
	public void setActualpricecent(String actualpricecent) {
		this.actualpricecent = actualpricecent;
	}
	public String getActualreason() {
		return actualreason;
	}
	public void setActualreason(String actualreason) {
		this.actualreason = actualreason;
	}
	public Goods getGoods() {
		return goods;
	}
	public void setGoods(Goods goods) {
		this.goods = goods;
	}
	public String getAgentid() {
		return agentid;
	}
	public void setAgentid(String agentid) {
		this.agentid = agentid;
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getCartid() {
		return cartid;
	}
	public void setCartid(String cartid) {
		this.cartid = cartid;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getGoodsid() {
		return goodsid;
	}
	public void setGoodsid(String goodsid) {
		this.goodsid = goodsid;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public boolean goodsExist(String goodsid) {
		if(goodsid == null || this.goodsid == null) return false;
		return this.goodsid.equals(goodsid);
	}
	public void addOne() {
		int cnt = Integer.parseInt(this.count);
		cnt = cnt + 1;
		this.count = "" + cnt;
	}
	@Override
	public String toString() {
		return "Cart [agentid=" + agentid + ", openid=" + openid + ", cartid=" + cartid + ", state=" + state
				+ ", goodsid=" + goodsid + ", count=" + count + ", time=" + time + "]";
	}
	
}
