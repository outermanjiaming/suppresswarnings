/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.data;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.Provider;
import com.suppresswarnings.osgi.leveldb.LevelDB;
import com.suppresswarnings.osgi.leveldb.LevelDBImpl;

public class DataProvider implements Provider<LevelDB> {
	static final String dbname = "/data";
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	
	LevelDB levelDB;
	public void activate() {
		if(this.levelDB == null) {
			this.levelDB = new LevelDBImpl(dbname);
			logger.info("[data] create.");
		}
		logger.info("[data] activate.");
	}

	public void deactivate() {
		if(this.levelDB != null) {
			this.levelDB.close();
			logger.info("[data] close.");
		}
		this.levelDB = null;
		logger.info("[data] deactivate.");
	}

	public void modified() {
		logger.info("[data] modified.");
	}
	
	@Override
	public String identity() {
		return "Data";
	}

	@Override
	public String description() {
		return "data leveldb";
	}

	@Override
	public LevelDB instance() {
		return levelDB;
	}

}
