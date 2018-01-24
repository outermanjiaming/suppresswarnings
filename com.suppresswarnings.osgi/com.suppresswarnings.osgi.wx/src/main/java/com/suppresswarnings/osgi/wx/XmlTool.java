package com.suppresswarnings.osgi.wx;

import java.util.List;

import cc.u2me.smskeywords.SMSDigger;
import cc.u2me.smskeywords.SMSDigger.KeyValue;

public class XmlTool {
	
	public static void main(String[] args) throws Exception {
		String text = "<xml>"
				+ "<ToUserName><![CDATA[{ToUserName}]]></ToUserName>"
				+ "<FromUserName><![CDATA[{FromUserName}]]></FromUserName>"
				+ "<CreateTime>{CreateTime}</CreateTime>"
				+ "<MsgType><![CDATA[{MsgType}]]></MsgType>"
				+ "<Content><![CDATA[{Content}]]></Content>"
				+ "<MsgId>{MsgId}</MsgId>"
				+ "</xml>";
		String image = "<xml><ToUserName><![CDATA[{ToUserName}]]></ToUserName><FromUserName><![CDATA[{FromUserName}]]></FromUserName><CreateTime>{CreateTime}</CreateTime><MsgType><![CDATA[{MsgType}]]></MsgType><PicUrl><![CDATA[{PicUrl}]]></PicUrl><MediaId><![CDATA[{MediaId}]]></MediaId><MsgId>{MsgId}</MsgId></xml>";
		SMSDigger.compile(text);
		SMSDigger.compile(image);
		String xmltext = "<xml>"
				+ "<ToUserName><![CDATA[gh_a1fe05b98706]]></ToUserName>\n"
				+ "<FromUserName>< ![CDATA[ot2GL05lU3rpJnJ7Hf_HTVjrozgk]] ></FromUserName>\n"
				+ "<CreateTime>1516776235</CreateTime>\n"
				+ "<MsgType>< ![CDATA[text]] ></MsgType>\n"
				+ "<Content><![CDATA[淋雨了]]></Content>\n"
				+ "<MsgId>6514504325098680211</MsgId>\n"
				+ "</xml>";
		String test = "<xml>  <ToUserName>< ![CDATA[toUser] ]></ToUserName>  <FromUserName>< ![CDATA[fromUser] ]></FromUserName>  <CreateTime>1348831860</CreateTime>  <MsgType>< ![CDATA[text] ]></MsgType>  <Content>< ![CDATA[this is a test] ]></Content>  <MsgId>1234567890123456</MsgId>  </xml>";
		List<KeyValue> list = SMSDigger.matches(xmltext);
		System.out.println(list);
		System.out.println(SMSDigger.matches(test));
		System.out.println(SMSDigger.matches("<xml> <ToUserName>< ![CDATA[toUser] ]></ToUserName> <FromUserName>< ![CDATA[fromUser] ]></FromUserName> <CreateTime>1348831860</CreateTime> <MsgType>< ![CDATA[image] ]></MsgType> <PicUrl>< ![CDATA[this is a url] ]></PicUrl> <MediaId>< ![CDATA[media_id] ]></MediaId> <MsgId>1234567890123456</MsgId> </xml>"));
	}
	
}
