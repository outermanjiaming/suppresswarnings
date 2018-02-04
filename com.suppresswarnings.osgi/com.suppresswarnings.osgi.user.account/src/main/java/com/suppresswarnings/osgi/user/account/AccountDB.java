package com.suppresswarnings.osgi.user.account;

import org.slf4j.LoggerFactory;
import com.suppresswarnings.osgi.user.KEY;
import com.suppresswarnings.osgi.user.Step;
import com.suppresswarnings.osgi.user.Version;
import com.suppresswarnings.osgi.user.AccountService;
import com.suppresswarnings.osgi.user.Counter;
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
	static final String delimiter = ";";
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	static final String dbname = "/account";
	LevelDBImpl levelDB;
	Counter counter;

	public AccountDB() {
		this.levelDB = new LevelDBImpl(dbname);
		this.counter = Counter.getInstance();
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
		String uidByAccount  = String.join(delimiter, version, KEY.Account.name(), KEY.UID.name(), args.username());
		String existUid = levelDB.get(uidByAccount);
		if(existUid == null) {
			return null;
		}
		String uidByUsernamePasscode = String.join(delimiter, version, KEY.Account.name(), KEY.Passwd.name(), args.username(), args.passcode());
		String uid = levelDB.get(uidByUsernamePasscode);
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
		String kLastLoginCount = String.join(delimiter, version, user.uid, KEY.LastLogin.name());
		String lastCount = levelDB.get(kLastLoginCount);
		if(lastCount == null) {
			String kLastLogin = String.join(delimiter, version, user.uid, KEY.LastLogin.name(), "0");
			String loginMsg = additional + ":"+System.currentTimeMillis();
			levelDB.put(kLastLogin, loginMsg);
			user.set(KEY.LastLogin, loginMsg);
		} else {
			String kLastLogin = String.join(delimiter, version, user.uid, KEY.LastLogin.name(), lastCount);
			String lastLogin = levelDB.get(kLastLogin);
			user.set(KEY.LastLogin, lastLogin);
			//this time
			long count = Long.valueOf(lastCount) + 1;
			lastCount = "" + count;
			String kThisLogin = String.join(delimiter, version, user.uid, KEY.LastLogin.name(), lastCount);
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
		String uidByUsername  = String.join(delimiter, version, KEY.Account.name(), KEY.UID.name(), args.username());
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
		String uidByUsernamePasscode = String.join(delimiter, version, KEY.Account.name(), KEY.Passwd.name(), KEY.UID.name(), args.username(), args.passcode());
		levelDB.put(uidByUsernamePasscode, user.uid);
		//2.set passwd in uid ( get passwd by uid <- get uid by token no matter if it expired)
		String passcodeByUID = String.join(delimiter, version, user.uid, KEY.Passwd.name());
		levelDB.put(passcodeByUID, args.passcode());
		//3.set exsit check in account:""
		levelDB.put(uidByUsername, user.uid);
		logger.info("[account] register " + String.valueOf(user));
		return user;
	}

	//A --invite--> B
	@Override
	public String invite(final User userA) {
		String invite = null;
		String limitByUid = String.join(delimiter, version, userA.uid, KEY.Invite.name(), KEY.Limit.name());
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
			String uidByInvite = String.join(delimiter, version, KEY.Invite.name(), KEY.UID.name(), invite);
			String uidA = levelDB.get(uidByInvite);
			if(uidA == null) {
				String inviteByUid = String.join(delimiter, version, userA.uid, KEY.Invite.name());
				levelDB.put(uidByInvite, userA.uid);
				levelDB.put(inviteByUid, invite);
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
		String uidByInvite = String.join(delimiter, version, KEY.Invite.name(), KEY.UID.name(), invite);
		String uidA = levelDB.get(uidByInvite);
		if(uidA == null) {
			logger.info("[invited] invite code not exist: " + invite);
			return null;
		} else if(uidA.startsWith(Step.Done.name())) {
			logger.info("[invited] invite code was done: " + invite);
			return null;
		}
		String invitedByUid = String.join(delimiter, version, userB.uid, KEY.Invited.name(), KEY.UID.name());
		String inviteByUid = String.join(delimiter, version, uidA, KEY.Invite.name(), KEY.UID.name());
		//means uidB invited by uidA
		levelDB.put(invitedByUid, uidA);
		//means uidA invite uidB
		levelDB.put(inviteByUid, userB.uid);
		//means invite code was done
		levelDB.put(uidByInvite, Step.Done.name() + uidA);
		return invite;
	}
	
}
