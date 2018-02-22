package com.suppresswarnings.osgi.wx.data;

import java.util.Random;
import java.util.function.BiConsumer;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.alone.Version;
import com.suppresswarnings.osgi.data.Const;
import com.suppresswarnings.osgi.data.DataService;
import com.suppresswarnings.osgi.leveldb.LevelDB;
import com.suppresswarnings.osgi.leveldb.LevelDBImpl;

/**
 * collect data and store it
 * @author lijiaming
 *
 */
public class DataDB implements DataService {
	static final String version = Version.V1;
	static final String dbname = "/data";
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	Random random = new Random();
	LevelDBImpl levelDB;
	
	public DataDB() {
		this.levelDB = new LevelDBImpl(dbname);
	}
	
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
	public int save(String key, String value) {
		return levelDB.put(key, value);
	}
	@Override
	public void listSome(String start, long limit, BiConsumer<String, String> consumer) {
		levelDB.list(start, limit, consumer);
	}
	@Override
	public int unknown(String uid, String value) {
		String time = "" + System.currentTimeMillis();
		String hash = "" + value.hashCode();
		String key = String.join(Const.delimiter, version, Const.data, Const.TextDataType.unknown, uid, time, hash);
		return save(key, value);
	}
	@Override
	public int answer(String uid, String question, String value) {
		// TODO Auto-generated method stub
		logger.info("[Data] answer not implemented");
		return 0;
	}
	@Override
	public int classify(String uid, String classify, String value) {
		// TODO Auto-generated method stub
		logger.info("[Data] classify not implemented");
		return 0;
	}

	@Override
	public LevelDB leveldb() {
		return levelDB;
	}

}
