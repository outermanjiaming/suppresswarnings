/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.sdk;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.suppresswarnings.corpus.service.sdk.WXPayConstants.SignType;

public class WXPayUtil {

    private static final String SYMBOLS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final Random RANDOM = new SecureRandom();

    /**
     * @param args
     * @throws Exception
     */
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
//		String xxx = "<xml><ToUserName><![CDATA[gh_b3ac48e83f7d]]></ToUserName>\n" + 
//				"<FromUserName><![CDATA[oDqlM1T1RcLQuv48d0iVr0r1VpLQ]]></FromUserName>\n" + 
//				"<CreateTime>1562864790</CreateTime>\n" + 
//				"<MsgType><![CDATA[event]]></MsgType>\n" + 
//				"<Event><![CDATA[ShakearoundUserShake]]></Event><ChosenBeacon><Uuid><![CDATA[FDA50693-A4E2-4FB1-AFCF-C6EB07647825]]></Uuid><Major>10197</Major><Minor>49838</Minor><Distance>1.5933141040212173</Distance><Rssi>-67</Rssi></ChosenBeacon><AroundBeacons></AroundBeacons><ChosenPageId>6750529</ChosenPageId></xml>";
//		
    	String xxx = "<xml><ToUserName><![CDATA[gh_b3ac48e83f7d]]></ToUserName>\n" + 
    			"<FromUserName><![CDATA[oDqlM1T1RcLQuv48d0iVr0r1VpLQ]]></FromUserName>\n" + 
    			"<CreateTime>1563090095</CreateTime>\n" + 
    			"<MsgType><![CDATA[event]]></MsgType>\n" + 
    			"<Event><![CDATA[user_get_card]]></Event>\n" + 
    			"<CardId><![CDATA[pDqlM1YsEzf5pU_DZzo8v63You64]]></CardId>\n" + 
    			"<IsGiveByFriend>0</IsGiveByFriend>\n" + 
    			"<UserCardCode><![CDATA[401838674686]]></UserCardCode>\n" + 
    			"<FriendUserName><![CDATA[]]></FriendUserName>\n" + 
    			"<OuterId>0</OuterId>\n" + 
    			"<OldUserCardCode><![CDATA[]]></OldUserCardCode>\n" + 
    			"<IsRestoreMemberCard>0</IsRestoreMemberCard>\n" + 
    			"<IsRecommendByFriend>0</IsRecommendByFriend>\n" + 
    			"<SourceScene><![CDATA[SOURCE_SCENE_APPMSG_JSAPI]]></SourceScene>\n" + 
    			"<UnionId><![CDATA[oAAAAAOtb28yY2_C54hSN7gj6YpA]]></UnionId>\n" + 
    			"</xml>";
    	String yyy = "<xml><ToUserName><![CDATA[gh_b3ac48e83f7d]]></ToUserName>\n" + 
    			"<FromUserName><![CDATA[oDqlM1TyKpSulfMC2OsZPwhi-9Wk]]></FromUserName>\n" + 
    			"<CreateTime>156309≈0444</CreateTime>\n" + 
    			"<MsgType><![CDATA[event]]></MsgType>\n" + 
    			"<Event><![CDATA[user_get_card]]></Event>\n" + 
    			"<CardId><![CDATA[pDqlM1YsEzf5pU_DZzo8v63You64]]></CardId>\n" + 
    			"<IsGiveByFriend>1</IsGiveByFriend>\n" + 
    			"<UserCardCode><![CDATA[732947834955]]></UserCardCode>\n" + 
    			"<FriendUserName><![CDATA[oDqlM1T1RcLQuv48d0iVr0r1VpLQ]]></FriendUserName>\n" + 
    			"<OuterId>0</OuterId>\n" + 
    			"<OldUserCardCode><![CDATA[401838674686]]></OldUserCardCode>\n" + 
    			"<OutetStr><![CDATA[]]></OutetStr>\n" + 
    			"</xml>";
    	String zzz = "<xml><ToUserName><![CDATA[gh_b3ac48e83f7d]]></ToUserName>\n" + 
    			"<FromUserName><![CDATA[oDqlM1eskciYMfB7xSLAPzo04oIU]]></FromUserName>\n" + 
    			"<CreateTime>1563090686</CreateTime>\n" + 
    			"<MsgType><![CDATA[event]]></MsgType>\n" + 
    			"<Event><![CDATA[user_get_card]]></Event>\n" + 
    			"<CardId><![CDATA[pDqlM1YsEzf5pU_DZzo8v63You64]]></CardId>\n" + 
    			"<IsGiveByFriend>1</IsGiveByFriend>\n" + 
    			"<UserCardCode><![CDATA[687337417790]]></UserCardCode>\n" + 
    			"<FriendUserName><![CDATA[oDqlM1TyKpSulfMC2OsZPwhi-9Wk]]></FriendUserName>\n" + 
    			"<OuterId>0</OuterId>\n" + 
    			"<OldUserCardCode><![CDATA[732947834955]]></OldUserCardCode>\n" + 
    			"<OutetStr><![CDATA[]]></OutetStr>\n" + 
    			"</xml>";
		Map<String, String> map = xmlToMap(zzz);
		map.forEach((x, y) -> System.out.println(x + " = " + y));
	}
    /**
     * XML格式字符串转换为Map
     *
     * @param strXML XML字符串
     * @return XML数据转换后的Map
     * @throws Exception
     */
    public static Map<String, String> xmlToMap(String strXML) throws Exception {
        try {
            Map<String, String> data = new HashMap<String, String>();
            DocumentBuilder documentBuilder = WXPayXmlUtil.newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(stream);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            for (int idx = 0; idx < nodeList.getLength(); ++idx) {
                Node node = nodeList.item(idx);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                    NodeList nodes = element.getChildNodes();
                    if(nodes.getLength() > 1) {
                    	for (int x = 0; x < nodes.getLength(); ++x) {
                            Node n = nodes.item(x);
                            org.w3c.dom.Element e = (org.w3c.dom.Element) n;
                            data.put(element.getNodeName() + ':' + e.getNodeName(), e.getTextContent());
                    	}
                    }
                    data.put(element.getNodeName(), element.getTextContent());
                }
            }
            try {
                stream.close();
            } catch (Exception ex) {
                // do nothing
            }
            return data;
        } catch (Exception ex) {
            WXPayUtil.getLogger().warn("Invalid XML, can not convert to map. Error message: {}. XML content: {}", ex.getMessage(), strXML);
            throw ex;
        }

    }

    /**
     * 将Map转换为XML格式的字符串
     *
     * @param data Map类型数据
     * @return XML格式的字符串
     * @throws Exception
     */
    public static String mapToXml(Map<String, String> data) throws Exception {
        org.w3c.dom.Document document = WXPayXmlUtil.newDocument();
        org.w3c.dom.Element root = document.createElement("xml");
        document.appendChild(root);
        for (String key: data.keySet()) {
            String value = data.get(key);
            if (value == null) {
                value = "";
            }
            value = value.trim();
            org.w3c.dom.Element filed = document.createElement(key);
            filed.appendChild(document.createTextNode(value));
            root.appendChild(filed);
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        DOMSource source = new DOMSource(document);
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        String output = writer.getBuffer().toString(); //.replaceAll("\n|\r", "");
        try {
            writer.close();
        }
        catch (Exception ex) {
        }
        return output;
    }


    /**
     * 生成带有 sign 的 XML 格式字符串
     *
     * @param data Map类型数据
     * @param key API密钥
     * @return 含有sign字段的XML
     */
    public static String generateSignedXml(final Map<String, String> data, String key) throws Exception {
        return generateSignedXml(data, key, SignType.MD5);
    }

    /**
     * 生成带有 sign 的 XML 格式字符串
     *
     * @param data Map类型数据
     * @param key API密钥
     * @param signType 签名类型
     * @return 含有sign字段的XML
     */
    public static String generateSignedXml(final Map<String, String> data, String key, SignType signType) throws Exception {
        String sign = generateSignature(data, key, signType);
        data.put(WXPayConstants.FIELD_SIGN, sign);
        return mapToXml(data);
    }


    /**
     * 判断签名是否正确
     *
     * @param xmlStr XML格式数据
     * @param key API密钥
     * @return 签名是否正确
     * @throws Exception
     */
    public static boolean isSignatureValid(String xmlStr, String key) throws Exception {
        Map<String, String> data = xmlToMap(xmlStr);
        if (!data.containsKey(WXPayConstants.FIELD_SIGN) ) {
            return false;
        }
        String sign = data.get(WXPayConstants.FIELD_SIGN);
        return generateSignature(data, key).equals(sign);
    }

    /**
     * 判断签名是否正确，必须包含sign字段，否则返回false。使用MD5签名。
     *
     * @param data Map类型数据
     * @param key API密钥
     * @return 签名是否正确
     * @throws Exception
     */
    public static boolean isSignatureValid(Map<String, String> data, String key) throws Exception {
        return isSignatureValid(data, key, SignType.MD5);
    }

    /**
     * 判断签名是否正确，必须包含sign字段，否则返回false。
     *
     * @param data Map类型数据
     * @param key API密钥
     * @param signType 签名方式
     * @return 签名是否正确
     * @throws Exception
     */
    public static boolean isSignatureValid(Map<String, String> data, String key, SignType signType) throws Exception {
        if (!data.containsKey(WXPayConstants.FIELD_SIGN) ) {
            return false;
        }
        String sign = data.get(WXPayConstants.FIELD_SIGN);
        return generateSignature(data, key, signType).equals(sign);
    }

    /**
     * 生成签名
     *
     * @param data 待签名数据
     * @param key API密钥
     * @return 签名
     */
    public static String generateSignature(final Map<String, String> data, String key) throws Exception {
        return generateSignature(data, key, SignType.MD5);
    }

    /**
     * 生成签名. 注意，若含有sign_type字段，必须和signType参数保持一致。
     *
     * @param data 待签名数据
     * @param key API密钥
     * @param signType 签名方式
     * @return 签名
     */
    public static String generateSignature(final Map<String, String> data, String key, SignType signType) throws Exception {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals(WXPayConstants.FIELD_SIGN)) {
                continue;
            }
            if (data.get(k).trim().length() > 0) // 参数值为空，则不参与签名
                sb.append(k).append("=").append(data.get(k).trim()).append("&");
        }
        sb.append("key=").append(key);
        if (SignType.MD5.equals(signType)) {
            return MD5(sb.toString()).toUpperCase();
        }
        else if (SignType.HMACSHA256.equals(signType)) {
            return HMACSHA256(sb.toString(), key);
        }
        else {
            throw new Exception(String.format("Invalid sign_type: %s", signType));
        }
    }


    /**
     * 获取随机字符串 Nonce Str
     *
     * @return String 随机字符串
     */
    public static String generateNonceStr() {
        char[] nonceChars = new char[32];
        for (int index = 0; index < nonceChars.length; ++index) {
            nonceChars[index] = SYMBOLS.charAt(RANDOM.nextInt(SYMBOLS.length()));
        }
        return new String(nonceChars);
    }


    /**
     * 生成 MD5
     *
     * @param data 待处理数据
     * @return MD5结果
     */
    public static String MD5(String data) throws Exception {
        java.security.MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString().toUpperCase();
    }

    /**
     * 生成 HMACSHA256
     * @param data 待处理数据
     * @param key 密钥
     * @return 加密结果
     * @throws Exception
     */
    public static String HMACSHA256(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] array = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString().toUpperCase();
    }

    /**
     * 日志
     * @return
     */
    public static Logger getLogger() {
        Logger logger = LoggerFactory.getLogger("SYSTEM");
        return logger;
    }

    /**
     * 获取当前时间戳，单位秒
     * @return
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis()/1000;
    }

    /**
     * 获取当前时间戳，单位毫秒
     * @return
     */
    public static long getCurrentTimestampMs() {
        return System.currentTimeMillis();
    }
	public static String sign(Map<String, String> data, String key, SignType type) {
		try {
			return generateSignature(data, key, type);
		} catch (Exception e) {
			getLogger().error("[WXPayUtil] sign error: ", e);
			return null;
		}
		
	}

}
