package com.suppresswarnings.osgi.leveldb;

import java.util.function.BiConsumer;

import org.slf4j.LoggerFactory;

import com.leveldb.common.Iterator;
import com.leveldb.common.Slice;
import com.leveldb.common.Status;
import com.leveldb.common.db.DB;
import com.leveldb.common.options.Options;
import com.leveldb.common.options.ReadOptions;
import com.leveldb.common.options.WriteOptions;

public class LevelDBImpl implements LevelDB {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	String dbname_;
	DB db_;

	@Override
	public int put(String key, String value) {
		WriteOptions woption = new WriteOptions();
		Status s = db_.Put(woption, new Slice(key), new Slice(value));
		if(s.ok()) return OK;
		return 0;
	}

	@Override
	public int put(String key, byte[] value) {
		WriteOptions woption = new WriteOptions();
		Status s = db_.Put(woption, new Slice(key), new Slice(value));
		if(s.ok()) return OK;
		return 0;
	}

	@Override
	public String get(String key) {
		Status status = new Status();
		ReadOptions roption = new ReadOptions();
		Slice ret = db_.Get(roption, new Slice(key), status);
		if(status.ok()) return ret.toString();
		return null;
	}

	@Override
	public byte[] read(String key) {
		Status status = new Status();
		ReadOptions roption = new ReadOptions();
		Slice ret = db_.Get(roption, new Slice(key), status);
		if(status.ok()) return ret.data();
		return null;
	}
	
	@Override
	public int init(String dbname, boolean create_if_missing) {
		Options options = new Options();
		options.create_if_missing = create_if_missing;
		dbname_ = dbname;
		db_ = DB.Open(options, dbname_);
		if(db_ == null) return OK;
		return 0;
	}

	@Override
	public void close() {
		if(db_ != null) db_.Close();
		
	}

	@Override
	public int destroy(String dbname) {
		if(dbname == null || dbname.length() < 1) {
			logger.warn("[leveldb] give the name of db: " + dbname);
			return NO;
		}
		if(dbname.equals(dbname_)) {
			logger.warn("[leveldb] closing the db: " + dbname_);
			close();
			logger.warn("[leveldb] destroy the db: " + dbname_);
			Status status = DB.DestroyDB(dbname_, new Options());
			if(status.ok()) return OK;
		} else {
			logger.warn("[leveldb] no right to destroy the db: " + dbname);
		}
		return NO;
	}
	
	@Override
	public void list(String start, long limit, BiConsumer<String, String> consumer) {
		Iterator itr = db_.NewIterator(new ReadOptions());
		long count = 0;
		for(itr.Seek(new Slice(start));itr.Valid() && count < limit; itr.Next()) {
			consumer.accept(itr.key().toString(), itr.value().toString());
			count++;
		}
	}

	@Override
	public String toString() {
		return "LevelDBImpl [dbname_=" + dbname_ + ", db_=" + db_ + "]";
	}

	
}
