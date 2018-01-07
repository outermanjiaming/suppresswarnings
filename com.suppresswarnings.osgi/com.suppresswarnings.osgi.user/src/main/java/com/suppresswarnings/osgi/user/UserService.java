package com.suppresswarnings.osgi.user;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.common.protocol.KEY;
import com.suppresswarnings.osgi.common.proxy.AutowiredConfigFactory;
import com.suppresswarnings.osgi.common.user.AccountService;
import com.suppresswarnings.osgi.common.user.Login;
import com.suppresswarnings.osgi.common.user.Register;
import com.suppresswarnings.osgi.common.user.TokenService;
import com.suppresswarnings.osgi.common.user.User;
import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;

public class UserService implements HTTPService {
	public static final String FAIL = "fail";
	public static final String OK = "ok";
	public static final String name = "user.http";
	private org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	private AccountService accountService;
	private TokenService tokenService;
	
	public void account(AccountService leveldb) {
		logger.info("init accountService: " + leveldb);
		this.accountService = leveldb;
	}
	public void clearAccount(AccountService leveldb){
		logger.info("release accountService: msg:" + leveldb + " here:" + this.accountService);
		this.accountService = null;
	}
	public void token(TokenService leveldb) {
		logger.info("init tokenService: " + leveldb);
		this.tokenService = leveldb;
	}
	public void clearToken(TokenService leveldb){
		logger.info("release tokenService: msg:" + leveldb + " here:" + this.tokenService);
		this.tokenService = null;
	}
	
	public String getName() {
		return name;
	}

	public String start(Parameter parameter) throws Exception {
		String ip = parameter.getParameter(Parameter.COMMON_KEY_CLIENT_IP);
		String action = parameter.getParameter("action");
		logger.info("User ip: "+ ip + ", action: " + action);
		AutowiredConfigFactory factory = new AutowiredConfigFactory();
		if(KEY.Login.name().equals(action)) {
			Login args = (Login) factory.create(parameter, Login.class);
			User user = accountService.login(args);
			if(user == null) {
				return FAIL;
			}
			accountService.lastLogin(user, ip);
			String token = tokenService.create(user);
			return token;
		} else if(KEY.Register.name().equals(action)) {
			Register args = (Register) factory.create(parameter, Register.class);
			User user = accountService.register(args);
			if(user == null) {
				return FAIL;
			}
			String token = tokenService.create(user);
			return token;
		} else if(KEY.Invite.name().equals(action)) {
			String token = parameter.getParameter(KEY.Token.name());
			if(token == null) {
				logger.info("token is null");
				return FAIL;
			}
			String valid = tokenService.valid(token);
			if(valid == null) {
				logger.info("token is invalid");
				return FAIL;
			}
			String uidttl = tokenService.check(token);
			if(uidttl == null) {
				logger.info("token is expired");
				return FAIL;
			}
			String uid = uidttl.split(":")[0];
			User user = User.oldUser(uid);
			String inviteCode = accountService.invite(user);
			if(inviteCode == null) {
				logger.info("fail to invite");
				return FAIL;
			}
			return inviteCode;
		} else if(KEY.Invited.name().equals(action)) {
			String token = parameter.getParameter(KEY.Token.name());
			String invite = parameter.getParameter(KEY.Invite.name());
			if(token == null || invite == null) {
				return FAIL;
			}
			String valid = tokenService.valid(token);
			if(valid == null) {
				return FAIL;
			}
			String uidttl = tokenService.check(token);
			if(uidttl == null) {
				return FAIL;
			}
			String uid = uidttl.split(":")[0];
			User userB = User.oldUser(uid);
			String inviteCode = accountService.invited(invite, userB);
			if(inviteCode == null) {
				return FAIL;
			}
			return inviteCode;
		}
		return OK;
	}

}
