/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.token;
import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.Provider;
import com.suppresswarnings.osgi.leveldb.LevelDB;
import com.suppresswarnings.osgi.leveldb.LevelDBImpl;

public class TokenProvider implements Provider<LevelDB> {
	static final String dbname = "/token";
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	
	LevelDB levelDB;
	public void activate() {
		if(this.levelDB == null) {
			this.levelDB = new LevelDBImpl(dbname);
			logger.info("[token] create.");
		}
		logger.info("[token] activate.");
	}

	public void deactivate() {
		if(this.levelDB != null) {
			this.levelDB.close();
			logger.info("[token] close.");
		}
		this.levelDB = null;
		logger.info("[token] deactivate.");
	}

	public void modified() {
		logger.info("[token] modified.");
	}
	
	@Override
	public String identity() {
		return "Token";
	}

	@Override
	public String description() {
		return "token leveldb";
	}

	@Override
	public LevelDB instance() {
		return levelDB;
	}

}
