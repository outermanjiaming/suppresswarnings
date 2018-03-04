package com.suppresswarnings.osgi.raspberrypi;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.data.DataService;
import com.suppresswarnings.osgi.leveldb.LevelDB;
import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;
import com.suppresswarnings.osgi.user.AccountService;
import com.suppresswarnings.osgi.user.TokenService;

public class PiService implements HTTPService {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	AccountService accountService;
	TokenService tokenService;
	DataService dataService;
	LevelDB leveldb;
	
	@Override
	public String getName() {
		return "pi.http";
	}

	public void activate() {
		logger.info("[Pi] activate.");
	}

	public void deactivate() {
		logger.info("[Pi] deactivate.");
	}

	public void modified() {
		logger.info("[Pi] modified.");
	}
	
	public void data(DataService service) {
		logger.info("[Pi] init dataService: " + service);
		this.dataService = service;
	}
	public void clearData(DataService service) {
		logger.info("[Pi] release dataService: msg:" + leveldb + " here:" + this.dataService);
		this.dataService = null;
	}
	public void account(AccountService service) {
		logger.info("[Pi] init accountService: " + service);
		this.accountService = service;
	}
	public void clearAccount(AccountService service){
		logger.info("[Pi] release accountService: msg:" + leveldb + " here:" + this.accountService);
		this.accountService = null;
	}
	public void token(TokenService service) {
		logger.info("[Pi] init tokenService: " + service);
		this.tokenService = service;
	}
	public void clearToken(TokenService service){
		logger.info("[Pi] release tokenService: msg:" + leveldb + " here:" + this.tokenService);
		this.tokenService = null;
	}

	public int saveToData(String key, String value) {
		return dataService.leveldb().put(key, value);
	}
	public int saveToToken(String key, String value) {
		return tokenService.leveldb().put(key, value);
	}
	public int saveToAccount(String key, String value) {
		return accountService.leveldb().put(key, value);
	}
	public String getFromAccount(String key) {
		return accountService.leveldb().get(key);
	}
	public String getFromData(String key) {
		return dataService.leveldb().get(key);
	}
	public String getFromToken(String key) {
		return tokenService.leveldb().get(key);
	}
	public static final String[] STRING = {"raspberrypi", "001.Token.RaspberryPi.%s", "001.IP.RaspberryPi.%s.%s", "001.Limit.Token.RaspberryPi.%s", "001.Expire.Token.RaspberryPi.%s"};
	public static final String[] ARGS = {"action","ip","var"};
	public static final String[] RESULT = {"success", "fail"};
	public static final int MaxSize = 240;
	
	@Override
	public String start(Parameter arg0) throws Exception {
		logger.info("[Pi] request: " + arg0.toString());
		String action = arg0.getParameter(ARGS[0]);
		if(!STRING[0].equals(action)) {
			return RESULT[1];
		}
		String ipconfig = arg0.getParameter(ARGS[1]);
		if(ipconfig == null || ipconfig.length() > MaxSize) {
			return RESULT[1];
		}
		String variable = arg0.getParameter(ARGS[2]);
		if(variable == null || variable.length() > MaxSize) {
			return RESULT[1];
		}
		String requestip= arg0.getParameter(Parameter.COMMON_KEY_CLIENT_IP);
		if(requestip == null || requestip.length() > 16) {
			return RESULT[1];
		}
		String last = String.format(STRING[1], variable);
		String value = "" + System.currentTimeMillis();
		saveToToken(last, value);
		String entry = String.format(STRING[2], variable, value);
		saveToToken(entry, ipconfig + "from" + requestip);
		return RESULT[0];
	}
}
