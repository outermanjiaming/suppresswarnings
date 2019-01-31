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

public class Goods {
	//openid.time.random
	String goodsid;
	String pricecent;
	//TODO
	String pricesecret;
	String priceagent;
	String pricevip;
	String usewhichprice;
	//TODO
	String outurl;
	
	String image;
	String title;
	String extra;
	
	String quota;
	String listimages;
	String time;
	String state;
	
	
	public String getUsewhichprice() {
		return usewhichprice;
	}
	public void setUsewhichprice(String usewhichprice) {
		this.usewhichprice = usewhichprice;
	}
	public String getPricesecret() {
		return pricesecret;
	}
	public void setPricesecret(String pricesecret) {
		this.pricesecret = pricesecret;
	}
	public String getPriceagent() {
		return priceagent;
	}
	public void setPriceagent(String priceagent) {
		this.priceagent = priceagent;
	}
	public String getPricevip() {
		return pricevip;
	}
	public void setPricevip(String pricevip) {
		this.pricevip = pricevip;
	}
	public String getOuturl() {
		return outurl;
	}
	public void setOuturl(String outurl) {
		this.outurl = outurl;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getGoodsid() {
		return goodsid;
	}
	public void setGoodsid(String goodsid) {
		this.goodsid = goodsid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getExtra() {
		return extra == null ? "" : extra;
	}
	public void setExtra(String extra) {
		this.extra = extra;
	}
	public String getPricecent() {
		return pricecent;
	}
	public void setPricecent(String pricecent) {
		this.pricecent = pricecent;
	}
	public String getQuota() {
		return quota == null ? "" : quota;
	}
	public void setQuota(String quota) {
		this.quota = quota;
	}
	public String getListimages() {
		return listimages;
	}
	public void setListimages(String listimages) {
		this.listimages = listimages;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
	@Override
	public String toString() {
		return "[" + pricecent + "分]「" + title + "」[" + goodsid + "]";
	}
	
	public void setState(String state) {
		this.state = state;
	}
	public String getState() {
		return state == null ? "" : state;
	}
	public boolean isDeleted() {
		return "DELETE".equals(state);
	}
	
}
