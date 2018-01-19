package com.suppresswarnings.osgi.user.token;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.common.protocol.KEY;
import com.suppresswarnings.osgi.common.protocol.KeyCreator;
import com.suppresswarnings.osgi.common.protocol.Version;
import com.suppresswarnings.osgi.common.user.PersistUtil;
import com.suppresswarnings.osgi.common.user.TokenService;
import com.suppresswarnings.osgi.common.user.User;

public class TokenDB implements TokenService {
	static final long VALID_MILLIS = TimeUnit.HOURS.toMillis(2);
	static final String HEAD_TOKEN = "0000000";
	static final String version = Version.V1;
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	static final int MAX_TOKEN = 10000000;
	static final String dbname = "/token";
	
	ConcurrentHashMap<String, String> tokenMap;

	public TokenDB() {
		File map = new File(dbname);
		if(map.exists()) {
			@SuppressWarnings("unchecked")
			ConcurrentHashMap<String, String> temp = (ConcurrentHashMap<String, String>) PersistUtil.deserialize(map.getAbsolutePath());
			Iterator<Map.Entry<String, String>> itr = temp.entrySet().iterator();
			logger.info("[token] clean token map: " + temp.size());
			while(itr.hasNext()) {
				Map.Entry<String, String> e = itr.next();
				String token = e.getKey();
				long time = createTime(token);
				long ttl = time + VALID_MILLIS - System.currentTimeMillis();
				if(ttl < 0) {
					logger.info("token expire: " + e.toString());
					itr.remove();
				}
			}
			this.tokenMap = temp;
			logger.info("[token] after token map cleaned: " + this.tokenMap.size());
		} else {
			this.tokenMap = new ConcurrentHashMap<String, String>();
			logger.info("[token] token map created");
		}
	}

	public void activate() {
		if(this.tokenMap == null) {
			this.tokenMap = new ConcurrentHashMap<String, String>();
			logger.info("[token] token map created");
			logger.info(this.getClass() + " create.");
		}
		logger.info(this.getClass() + " activate.");
	}

	public void deactivate() {
		if(this.tokenMap != null) {
			File map = new File(dbname);
			if(!map.exists()) {
				try {
					map.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			PersistUtil.serialize(this.tokenMap, map.getAbsolutePath());
			logger.info(this.getClass() + " close and token map saved: " + map.getAbsolutePath());
		}
		this.tokenMap = null;
		logger.info(this.getClass() + " deactivate.");
	}

	public void modified() {
		logger.info(this.getClass() + " modified.");
	}

	@Override
	public String create(User user) {
		String uid_username_token =  KeyCreator.key(version, user.uid, KEY.Token, user.get(KEY.Account));
		String token = tokenMap.get(uid_username_token);
		if(valid(token) == null) {
			token = randomToken();
			tokenMap.put(uid_username_token, token);
			logger.info("[token] new random token.");
		} else {
			logger.info("[token] old token.");
		}
		
		String token_uid =  KeyCreator.key(version, KEY.Token.name(), KEY.UID, token);
		tokenMap.put(token_uid, user.uid);
		user.set(KEY.Token, token);
		return token;
	}

	@Override
	public String check(String token) {
		String token_uid = KeyCreator.key(version, KEY.Token.name(), KEY.UID, token);
		String uid = tokenMap.get(token_uid);
		if(uid == null) {
			return null;
		}
		long time = createTime(token);
		long ttl = time + VALID_MILLIS - System.currentTimeMillis();
		if(ttl > 0) {
			return uid + ":" + ttl;
		}
		logger.info("remove expired token: " + tokenMap.remove(token_uid));
		return null;
	}

	@Override
	public String valid(String token) {
		if(token == null || token.length() < HEAD_TOKEN.length() + 5) {
			return null;
		}
		long time = createTime(token);
		long valid = time + VALID_MILLIS - System.currentTimeMillis();
		if(valid < 0) {
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss.SS");
		return format.format(new Date(valid));
	}
	
	public long createTime(String token) {
		String hex = token.substring(HEAD_TOKEN.length());
		long time = Long.parseLong(hex, 16);
		return time;
	}
	
	public static String randomToken(){
		Random random = new Random();
		DecimalFormat format = new DecimalFormat(HEAD_TOKEN);
		return format.format(random.nextInt(999999)) + Long.toHexString(System.currentTimeMillis());
	}
   
}
