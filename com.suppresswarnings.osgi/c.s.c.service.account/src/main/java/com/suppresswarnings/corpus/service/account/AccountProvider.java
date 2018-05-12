/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */

package com.suppresswarnings.corpus.service.account;
import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.Provider;
import com.suppresswarnings.osgi.leveldb.LevelDB;
import com.suppresswarnings.osgi.leveldb.LevelDBImpl;

public class AccountProvider implements Provider<LevelDB> {
	static final String dbname = "/account";
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	
	LevelDB levelDB;
	public void activate() {
		if(this.levelDB == null) {
			this.levelDB = new LevelDBImpl(dbname);
			logger.info("[account] create.");
		}
		logger.info("[account] activate.");
	}

	public void deactivate() {
		if(this.levelDB != null) {
			this.levelDB.close();
			logger.info("[account] close.");
		}
		this.levelDB = null;
		logger.info("[account] deactivate.");
	}

	public void modified() {
		logger.info("[account] modified.");
	}
	
	@Override
	public String identity() {
		return "Account";
	}

	@Override
	public String description() {
		return "account leveldb";
	}

	@Override
	public LevelDB instance() {
		return levelDB;
	}

}
