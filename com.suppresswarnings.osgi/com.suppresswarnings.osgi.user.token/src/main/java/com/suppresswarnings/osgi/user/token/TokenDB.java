package com.suppresswarnings.osgi.user.token;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.common.protocol.KEY;
import com.suppresswarnings.osgi.common.protocol.KeyCreator;
import com.suppresswarnings.osgi.common.protocol.Version;
import com.suppresswarnings.osgi.common.user.TokenService;
import com.suppresswarnings.osgi.common.user.User;
import com.suppresswarnings.osgi.leveldb.LevelDBImpl;

public class TokenDB implements TokenService {
	static final long VALID_MILLIS = TimeUnit.HOURS.toMillis(2);
	static final String HEAD_TOKEN = "0000000";
	static final String version = Version.V1;
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	static final String dbname = "/token";
	
	LevelDBImpl levelDB;

	public TokenDB() {
		this.levelDB = new LevelDBImpl(dbname);
	}

	public void activate() {
		if(this.levelDB == null) {
			this.levelDB = new LevelDBImpl(dbname);
			logger.info(this.getClass() + " create.");
		}
		logger.info(this.getClass() + " activate.");
	}

	public void deactivate() {
		if(this.levelDB != null) {
			this.levelDB.close();
			logger.info(this.getClass() + " close.");
		}
		this.levelDB = null;
		logger.info(this.getClass() + " deactivate.");
	}

	public void modified() {
		logger.info(this.getClass() + " modified.");
	}

	@Override
	public String create(User user) {
		String uid_username_token =  KeyCreator.key(version, user.uid, KEY.Token, user.get(KEY.Account));
		String token = levelDB.get(uid_username_token);
		if(valid(token) == null) {
			token = randomToken();
			levelDB.put(uid_username_token, token);
			logger.info("[token] new random token.");
		} else {
			logger.info("[token] old token.");
		}
		
		String token_uid =  KeyCreator.key(version, KEY.Token.name(), KEY.UID, token);
		levelDB.put(token_uid, user.uid);
		user.set(KEY.Token, token);
		return token;
	}

	@Override
	public String check(String token) {
		String token_uid = KeyCreator.key(version, KEY.Token.name(), KEY.UID, token);
		String uid = levelDB.get(token_uid);
		if(uid == null) {
			return null;
		}
		long ttl = createTime(token) + VALID_MILLIS - System.currentTimeMillis();
		if(ttl > 0) {
			return uid + ":" + ttl;
		}
		return null;
	}

	@Override
	public String valid(String token) {
		if(token == null || token.length() < HEAD_TOKEN.length() + 10) {
			return null;
		}
		long ttl = createTime(token) + VALID_MILLIS - System.currentTimeMillis();
		if(ttl < 0) {
			return null;
		}
		return "TTL:" + ttl;
	}

	public static String randomToken(){
		Random random = new Random();
		DecimalFormat format = new DecimalFormat(HEAD_TOKEN);
		return format.format(random.nextInt(999999)) + Long.toHexString(System.currentTimeMillis());
	}
	
	public static long createTime(String token) {
		String hex = token.substring(HEAD_TOKEN.length());
		long time = Long.parseLong(hex, 16);
		return time;
	}
}
