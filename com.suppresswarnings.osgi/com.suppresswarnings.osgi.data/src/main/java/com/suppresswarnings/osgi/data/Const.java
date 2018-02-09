package com.suppresswarnings.osgi.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface Const {
	String delimiter = ".";
	String data = "Data";
	String count= "Count";
	String user = "User";
	/**
	 * collection or question or classify or reply or produce or unknown
	 * @author lijiaming
	 *
	 */
	public interface TextDataType {
		String collection = "collection";
		String question   = "question";
		String classify   = "classify";
		String reply      = "reply";
		String produce    = "produce";
		String unknown    = "unknown";
	}
	
	public interface InteractionTTL {
		long userReply = TimeUnit.MINUTES.toMillis(20);
		long setThePaper = TimeUnit.MINUTES.toMillis(120);
	}
	
	public interface CounterName {
		String data = "Data";
		String type = "Type";
	}
	
	public interface WXmsg {
		int msgTypeIndex = 3;
		String openid = "gh_a1fe05b98706";
		String uri = "action={action}&signature={signature}&timestamp={timestamp}&nonce={nonce}&openid={openid}";
		String xml  = "<xml><ToUserName><![CDATA[%s]]></ToUserName><FromUserName><![CDATA[%s]]></FromUserName><CreateTime>%s</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[%s]]></Content></xml>";
		String[] keys = {
				"ToUserName",
				"FromUserName",
				"CreateTime", 
				"MsgType",
				"MediaId",
				"Content",
				"MsgId",
				"PicUrl",
				"Location_X",
				"Location_Y", 
				"Scale", 
				"Label", 
				"Recognition"
		};
		
		String[] values = {
				"gh_a1fe05b98706",
				"ot2GL05lU3rpJnJ7Hf_HTVjrozgk"
		};
		
		String[] msgFormat = {
				"<xml><ToUserName><![CDATA[{ToUserName}]]></ToUserName><FromUserName><![CDATA[{FromUserName}]]></FromUserName><CreateTime>{CreateTime}</CreateTime><MsgType><![CDATA[{MsgType}]]></MsgType><MediaId><![CDATA[{MediaId}]]></MediaId><Format><![CDATA[{Format}]]></Format><MsgId>{MsgId}</MsgId><Recognition><![CDATA[{Recognition}]]></Recognition></xml>",
				"<xml><ToUserName><![CDATA[{ToUserName}]]></ToUserName><FromUserName><![CDATA[{FromUserName}]]></FromUserName><CreateTime>{CreateTime}</CreateTime><MsgType><![CDATA[{MsgType}]]></MsgType><Content><![CDATA[{Content}]]></Content><MsgId>{MsgId}</MsgId></xml>",
				"<xml><ToUserName><![CDATA[{ToUserName}]]></ToUserName><FromUserName><![CDATA[{FromUserName}]]></FromUserName><CreateTime>{CreateTime}</CreateTime><MsgType><![CDATA[{MsgType}]]></MsgType><PicUrl><![CDATA[{PicUrl}]]></PicUrl><MsgId>{MsgId}</MsgId><MediaId><![CDATA[{MediaId}]]></MediaId></xml>",
				"<xml><ToUserName><![CDATA[{ToUserName}]]></ToUserName><FromUserName><![CDATA[{FromUserName}]]></FromUserName><CreateTime>{CreateTime}</CreateTime><MsgType><![CDATA[{MsgType}]]></MsgType><Location_X>{Location_X}</Location_X><Location_Y>{Location_Y}</Location_Y><Scale>{Scale}</Scale><Label><![CDATA[{Label}]]></Label><MsgId>{MsgId}</MsgId></xml>"
		};
		
		String[] secret = {
				"lijiaming2018123", 
				"2a6mVPNhf1iNxJMCXoZUomUrS323MVzsSHkpAn4ZwWp", 
				"wx1f95008283948d0b"
		};
		
		Map<String, String> types = new HashMap<String,String>(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 4208011498374070759L;

			{
				put("image",     "图片");
				put("location",  "位置");
				put("voice",     "语音");
				put("text",      "文字");
				put("subscribe", "关注");
			}
		};

		String[] reply = {
				"签名不太对，不给处理。",
				"格式不太对，不能处理。",
				"类型不太对，不会处理。"
		};
	}
}
