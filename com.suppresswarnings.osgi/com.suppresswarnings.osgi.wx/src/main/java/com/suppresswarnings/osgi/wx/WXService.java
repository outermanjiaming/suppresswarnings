package com.suppresswarnings.osgi.wx;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.alone.Format;
import com.suppresswarnings.osgi.alone.SendMail;
import com.suppresswarnings.osgi.alone.Format.KeyValue;
import com.suppresswarnings.osgi.ner.API;
import com.suppresswarnings.osgi.ner.Item;
import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;

public class WXService implements HTTPService {
	public static final String[] keys = {
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
	public static final String[] values = {
			"gh_a1fe05b98706",
			"ot2GL05lU3rpJnJ7Hf_HTVjrozgk"
	};
	public static final Map<String, String> types = new HashMap<String,String>();
	static {
		types.put("image", "图片");
		types.put("location", "位置");
		types.put("voice", "语音");
		types.put("text", "文字");
		types.put("subscribe", "关注");
	}
	public static final int msgTypeIndex = 3;
	public static final String name = "wx.http";
	public static final String xml  = "<xml><ToUserName><![CDATA[%s]]></ToUserName><FromUserName><![CDATA[gh_a1fe05b98706]]></FromUserName><CreateTime>%s</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[%s]]></Content></xml>";
	public static final String uri = "action={action}&signature={signature}&timestamp={timestamp}&nonce={nonce}&openid={openid}";
	public static final String[] msgFormat = {
		"<xml><ToUserName><![CDATA[{ToUserName}]]></ToUserName><FromUserName><![CDATA[{FromUserName}]]></FromUserName><CreateTime>{CreateTime}</CreateTime><MsgType><![CDATA[{MsgType}]]></MsgType><MediaId><![CDATA[{MediaId}]]></MediaId><Format><![CDATA[{Format}]]></Format><MsgId>{MsgId}</MsgId><Recognition><![CDATA[{Recognition}]]></Recognition></xml>",
		"<xml><ToUserName><![CDATA[{ToUserName}]]></ToUserName><FromUserName><![CDATA[{FromUserName}]]></FromUserName><CreateTime>{CreateTime}</CreateTime><MsgType><![CDATA[{MsgType}]]></MsgType><Content><![CDATA[{Content}]]></Content><MsgId>{MsgId}</MsgId></xml>",
		"<xml><ToUserName><![CDATA[{ToUserName}]]></ToUserName><FromUserName><![CDATA[{FromUserName}]]></FromUserName><CreateTime>{CreateTime}</CreateTime><MsgType><![CDATA[{MsgType}]]></MsgType><PicUrl><![CDATA[{PicUrl}]]></PicUrl><MsgId>{MsgId}</MsgId><MediaId><![CDATA[{MediaId}]]></MediaId></xml>",
		"<xml><ToUserName><![CDATA[{ToUserName}]]></ToUserName><FromUserName><![CDATA[{FromUserName}]]></FromUserName><CreateTime>{CreateTime}</CreateTime><MsgType><![CDATA[{MsgType}]]></MsgType><Location_X>{Location_X}</Location_X><Location_Y>{Location_Y}</Location_Y><Scale>{Scale}</Scale><Label><![CDATA[{Label}]]></Label><MsgId>{MsgId}</MsgId></xml>"
	};
	public static final String[] secret = {
			"lijiaming2018123", 
			"2a6mVPNhf1iNxJMCXoZUomUrS323MVzsSHkpAn4ZwWp", 
			"wx1f95008283948d0b"
	};
	private org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	private Format format = new Format(msgFormat);
	private API api = new API(System.getenv("HB_HOME") + "/quiz.ner");
	
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String start(Parameter parameter) throws Exception {
		String ip = parameter.getParameter(Parameter.COMMON_KEY_CLIENT_IP);
		logger.info("msg from wx: " + parameter.toString());
		String action = parameter.getParameter("action");
		if(("WX").equals(action)){
			logger.info("[wx] IP: "+ip);
		}
		String msgSignature = parameter.getParameter("signature");
		String timestamp = parameter.getParameter("timestamp");
		String nonce = parameter.getParameter("nonce");
		String sha1 = getSHA1(secret[0], timestamp, nonce, "");
		String openid =  parameter.getParameter("openid");
		String echoStr = parameter.getParameter("echostr");
		if(msgSignature == null || !msgSignature.equals(sha1)) {
			logger.error("[wx] wrong signature");
			if(openid != null) return reply(openid, "(fail) I'm glad you're interested in us");
		}
		if(echoStr != null) return echoStr;
		if(openid != null) {
			String sms = parameter.getParameter(Parameter.POST_BODY);
			List<KeyValue> kvs = format.matches(sms);
			KeyValue kv = kvs.get(msgTypeIndex);
			if(!"MsgType".equals(kv.key())) {
				SendMail cn = new SendMail();
				cn.title("[note] wx msg structure not match", kvs.toString());
			}
			if("text".equals(kv.value())) {
				WXtext value = new WXtext();
				value.init(kvs);
				String text = value.Content;
				Item[] items = api.ner(text);
				QuizContext context = new QuizContext();
				for(Item it : items) {
					context.test(it.key());
				}
				return reply(openid, "AI: " + context.output());
			} else if("voice".equals(kv.value())) {
				WXvoice value = new WXvoice();
				value.init(kvs);
				String text = value.Recognition;
				if(text == null || text.length() < 1) {
					return reply(openid, "FAIL: pardon me?");
				}
				Item[] items = api.ner(text);
				QuizContext context = new QuizContext();
				for(Item it : items) {
					context.test(it.key());
				}
				return reply(openid, "AI: " + context.output());
			}
			return reply(openid, "I'm glad you're interested in us, but I can't resolve " + types.get(kv.value()));
		}
		return "success";
	}
	public static String reply(String to, String msg) {
		long time = System.currentTimeMillis()/1000;
		return String.format(xml, to, ""+time, msg);
	}
	public static void main(String[] args) {
		Format format = new Format(msgFormat);
		List<KeyValue> kvs = format.matches("<xml><ToUserName><![CDATA[gh_a1fe05b98706]]></ToUserName><FromUserName><![CDATA[ot2GL05lU3rpJnJ7Hf_HTVjrozgk]]></FromUserName><CreateTime>1516794211</CreateTime><MsgType><![CDATA[location]]></MsgType><Location_X>22.374340</Location_X><Location_Y>113.562973</Location_Y><Scale>16</Scale><Label><![CDATA[魅族科技研发分部(珠海市香洲区)]]></Label><MsgId>6514581531430798690</MsgId></xml>");//"<xml><ToUserName><![CDATA[gh_a1fe05b98706]]></ToUserName><FromUserName><![CDATA[ot2GL05lU3rpJnJ7Hf_HTVjrozgk]]></FromUserName><CreateTime>1516794660</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[http://139.199.104.224/]]></Content><MsgId>6514583459871114725</MsgId></xml>");
		System.out.println(kvs.get(msgTypeIndex));
	}
	
	public String getSHA1(String token, String timestamp, String nonce, String encrypt) {
		if(anyNull(token, timestamp, nonce, encrypt)) {
			return null;
		}
		try {
			String[] array = new String[] { token, timestamp, nonce, encrypt };
			StringBuffer sb = new StringBuffer();
			Arrays.sort(array);
			for (int i = 0; i < 4; i++) {
				sb.append(array[i]);
			}
			String str = sb.toString();
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(str.getBytes());
			byte[] digest = md.digest();
			StringBuffer hexstr = new StringBuffer();
			String shaHex = "";
			for (int i = 0; i < digest.length; i++) {
				shaHex = Integer.toHexString(digest[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexstr.append(0);
				}
				hexstr.append(shaHex);
			}
			return hexstr.toString();
		} catch (Exception e) {
			logger.error("sha-1 error", e);
			return null;
		}
	}
	
	public static boolean anyNull(String...others) {
		if(others == null) return true;
		for(String s : others) if(s == null) return true;
		return false;
	}
}