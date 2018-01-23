package com.suppresswarnings.osgi.user.account;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.user.KEY;
import com.suppresswarnings.osgi.user.KeyCreator;
import com.suppresswarnings.osgi.user.Step;
import com.suppresswarnings.osgi.user.Version;
import com.suppresswarnings.osgi.user.AccountService;
import com.suppresswarnings.osgi.user.Login;
import com.suppresswarnings.osgi.user.Register;
import com.suppresswarnings.osgi.user.User;
import com.suppresswarnings.osgi.leveldb.LevelDBImpl;
/**
 * user operations on account
 * @author lijiaming
 *
 */
public class AccountDB implements AccountService {
	static final String version = Version.V1;
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	static final String dbname = "/account";
	LevelDBImpl levelDB;

	public AccountDB() {
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
	public User login(Login args) {
		if(args.username() == null || args.passcode() == null) {
			return null;
		}
		String checkExist  = uidByUsername(args.username());
		String exist = levelDB.get(checkExist);
		if(exist == null) {
			return null;
		}
		String checkPasswd = uidByUsernamePasscode(args.username(), args.passcode());
		String uid = levelDB.get(checkPasswd);
		if(uid == null) {
			return null;
		}
		User user = User.oldUser(uid);
		user.set(KEY.Account, args.username());
		user.set(KEY.Passwd, "******");
		logger.info("[account] login " + String.valueOf(user));
		return user;
	}

	@Override
	public void lastLogin(User user, String additional) {
		String kLastLoginCount = lastloginCountByUid(user.uid);
		String lastCount = levelDB.get(kLastLoginCount);
		if(lastCount == null) {
			String kLastLogin = lastloginByUidCount(user.uid, "0");
			String loginMsg = additional + ":"+System.currentTimeMillis();
			levelDB.put(kLastLogin, loginMsg);
			user.set(KEY.LastLogin, loginMsg);
		} else {
			String kLastLogin = lastloginByUidCount(user.uid, lastCount);
			String lastLogin = levelDB.get(kLastLogin);
			user.set(KEY.LastLogin, lastLogin);
			//this time
			long count = Long.valueOf(lastCount) + 1;
			lastCount = "" + count;
			String kThisLogin = lastloginByUidCount(user.uid, lastCount);
			String loginMsg = additional + ":"+System.currentTimeMillis();
			levelDB.put(kThisLogin, loginMsg);
			levelDB.put(kLastLoginCount, lastCount);
		}
		logger.info("[account] update " + String.valueOf(user));
	}

	@Override
	public User register(Register args) {
		if(args.username() == null || args.passcode() == null || !args.passcode().equals(args.confirm())) {
			return null;
		}
		String uidByUsername  = uidByUsername(args.username());
		String exist = levelDB.get(uidByUsername);
		if(exist != null) {
			logger.info("account exists already.");
			return null;
		}
		//TODO encrypt
		User user = User.newUser();
		user.set(KEY.Account, args.username());
		user.set(KEY.Passwd, "******");
		
		//1.set uid in account:passwd
		levelDB.put(uidByUsernamePasscode(args.username(), args.passcode()), user.uid);
		//2.set passwd in uid ( get passwd by uid <- get uid by token no matter if it expired)
		levelDB.put(passcodeByUID(user.uid), args.passcode());
		//3.set exsit check in account:""
		levelDB.put(uidByUsername, user.uid);
		logger.info("[account] register " + String.valueOf(user));
		return user;
	}

	//A --invite--> B
	@Override
	public String invite(final User userA) {
		String invite = null;
		String limitByUid = limitByUid(userA.uid);
		String limit = levelDB.get(limitByUid);
		if(limit == null) {
			logger.info("[invite] unlimited: " + userA.uid);
			//try 10 times
			invite = invite(userA, 10);
		} else {
			int left = Integer.valueOf(limit);
			if(left > 0) {
				//try 10 times
				invite = invite(userA, 10);
				left --;
				levelDB.put(limitByUid, ""+left);
			} else {
				//sorry you can't invite any more for now
				logger.info("[invite] limited: " + userA.uid);
			}
		}
		return invite;
	}

	private String invite(final User userA, int count) {
		String invite = userA.inviteCode();
		do{
			String keyInvite = uidByInvite(invite);
			String uidA = levelDB.get(keyInvite);
			if(uidA == null) {
				String keyInviteUid = inviteByUid(userA.uid);
				levelDB.put(keyInvite, userA.uid);
				levelDB.put(keyInviteUid, invite);
				userA.set(KEY.Invite, invite);
				logger.info("[invite] success: " + invite + " count: " + count);
				break;
			} else {
				//TODO send message to that uid, congratulations
				logger.info("[invite] try again: " + invite + " count: " + count);
				invite = userA.inviteCode();
			}
		} while(count-- > 0);
		return invite;
	}

	//B <--invited by-- A
	@Override
	public String invited(String invite, User userB) {
		String keyInvite = uidByInvite(invite);
		String uidA = levelDB.get(keyInvite);
		if(uidA == null) {
			logger.info("[invited] invite code not exist: " + invite);
			return null;
		} else if(uidA.startsWith(Step.Done.name())) {
			logger.info("[invited] invite code was done: " + invite);
			return null;
		}
		String keyInvited = invitedByUid(userB.uid);
		String keyInviteUid = inviteByUid(uidA);
		levelDB.put(keyInvited, uidA);
		levelDB.put(keyInviteUid, userB.uid);
		levelDB.put(keyInvite, Step.Done.name() + uidA);
		return invite;
	}

	public String uidByInvite(String invite) {
		return KeyCreator.key(version, KEY.Exist.name(), KEY.Invite, invite);
	}
	public String inviteByUid(String uid) {
		return KeyCreator.key(version, uid, KEY.Invite);
	}
	public String invitedByUid(String uid) {
		return KeyCreator.key(version, uid, KEY.Invited);
	}
	public String limitByUid(String uid) {
		return KeyCreator.key(version, KEY.Invite.name(), KEY.Limit, uid);
	}
	
	public String passcodeByUID(String uid) {
		return KeyCreator.key(version, KEY.UID.name(), KEY.Passwd, uid);
	}

	public String uidByUsername(String username) {
		return KeyCreator.key(version, KEY.Exist.name(), KEY.Account, username);
	}

	public String uidByUsernamePasscode(String username, String passcode) {
		return KeyCreator.key(version, username, KEY.Passwd, passcode);
	}

	private String lastloginCountByUid(String uid) {
		return KeyCreator.counter(version, uid, KEY.LastLogin);
	}
	
	public String lastloginByUidCount(String uid, String lastCount) {
		return KeyCreator.key(version, uid, KEY.LastLogin, lastCount);
	}

}
