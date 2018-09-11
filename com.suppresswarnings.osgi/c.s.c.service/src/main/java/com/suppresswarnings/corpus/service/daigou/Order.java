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

import java.util.List;

public class Order {
	public static interface State{
		String Create = "Create";
		String Paying = "Paying";
		String Paid = "Paid";
		String Closed = "Closed";
		String Deleted = "Deleted";
	}
	String orderid;
	String openid;
	String username;
	String idcard;
	String mobile;
	String address;
	String comment;
	String totalcent;
	String goodscount;
	String goodstypes;
	String time;
	String state;
	String detail;
	
	List<Cart> carts;
	
	public String getIdcard() {
		return idcard;
	}
	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public String getOrderid() {
		return orderid;
	}
	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getTotalcent() {
		return totalcent;
	}
	public void setTotalcent(String totalcent) {
		this.totalcent = totalcent;
	}
	public String getGoodscount() {
		return goodscount;
	}
	public void setGoodscount(String goodscount) {
		this.goodscount = goodscount;
	}
	public String getGoodstypes() {
		return goodstypes;
	}
	public void setGoodstypes(String goodstypes) {
		this.goodstypes = goodstypes;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public List<Cart> getCarts() {
		return carts;
	}
	public void setCarts(List<Cart> carts) {
		this.carts = carts;
	}
	
}
