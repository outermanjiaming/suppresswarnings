package com.suppresswarnings.osgi.data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TextData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6334542727674973374L;
	public static final String extraSpliter = ";";
	//1615a70b02a || 1517640805250
	String createTime;
	//valueofuid
	String byWhom;
	//this text data value for test
	String describe;
	//电影
	String value;
	//collection or question or classify or reply or produce or unknown
	String type;
	//3204906234254
	String uuid;
	//json of extra information about this text data
	String extra = extraSpliter;
	public TextData createTime(String createTime){
		this.createTime = createTime;
		return this;
	}
	public TextData byWhom(String byWhom){
		this.byWhom = byWhom;
		return this;
	}
	public TextData describe(String describe){
		this.describe = describe;
		return this;
	}
	public TextData value(String value){
		this.value = value;
		return this;
	}
	public TextData type(String type){
		this.type = type;
		return this;
	}
	public TextData uuid(String uuid){
		this.uuid = uuid;
		return this;
	}
	public TextData appendExtra(String extra){
		this.extra = this.extra + extra + ";" ;
		return this;
	}
	
	public String createTime(){
		return createTime;
	}
	public String byWhom(){
		return byWhom;
	}
	public String describe(){
		return describe;
	}
	public String value(){
		return value;
	}
	public String type(){
		return type;
	}
	public String uuid(){
		return uuid;
	}
	public String extra(){
		return extra;
	}
	public static String currentTime(){
		long time = System.currentTimeMillis();
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
		Date date = new Date(time);
		System.out.println(format.format(date));
		String current = ""+time;
		System.out.println(current);
		return current;
	}
	public static void main(String[] args) {
		System.out.println(System.currentTimeMillis());
		TextData text = new TextData();
		text.byWhom = "lijiaming";
		long time = System.currentTimeMillis();
		System.out.println(new Date(time));
		text.createTime(Long.toHexString(System.currentTimeMillis()));
		text.describe("收集和电影相关的语料，需求方是自己。");
		text.value("请说一句和电影相关的语句？");
		text.uuid("21478392631");
		text.type("collection");
		System.out.println(text);
	}
	@Override
	public String toString() {
		return "TextData [createTime=" + createTime + ", byWhom=" + byWhom + ", describe=" + describe + ", value="
				+ value + ", type=" + type + ", uuid=" + uuid + ", extra=" + extra + "]";
	}
	
}
