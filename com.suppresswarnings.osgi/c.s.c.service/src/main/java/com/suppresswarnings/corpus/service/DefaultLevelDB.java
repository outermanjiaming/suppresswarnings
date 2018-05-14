/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.leveldb.LevelDB;

public class DefaultLevelDB implements LevelDB {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	String dbName;
	public DefaultLevelDB(String name) {
		this.dbName = name;
		logger.error("[DefaultLevelDB] new");
	}
	@Override
	public byte[] read(String key) {
		logger.error("[DefaultLevelDB] read");
		return null;
	}
	
	@Override
	public int put(String key, byte[] value) {
		logger.error("[DefaultLevelDB] put");
		return 0;
	}
	
	@Override
	public int put(String key, String value) {
		logger.error("[DefaultLevelDB] put");
		return 0;
	}
	
	@Override
	public String page(String head, String start, AtomicBoolean stop, long limit, BiConsumer<String, String> consumer) {
		logger.error("[DefaultLevelDB] page");
		return null;
	}
	
	@Override
	public void list(String start, long limit, BiConsumer<String, String> consumer) {
		logger.error("[DefaultLevelDB] list");
	}
	
	@Override
	public boolean inited() {
		logger.error("[DefaultLevelDB] inited");
		return false;
	}
	
	@Override
	public int init(String dbname, boolean create_if_missing) {
		logger.error("[DefaultLevelDB] init");
		return 0;
	}
	
	@Override
	public String getDBname() {
		return dbName;
	}
	
	@Override
	public String get(String key) {
		logger.error("[DefaultLevelDB] get");
		return null;
	}
	
	@Override
	public int destroy(String dbname) {
		logger.error("[DefaultLevelDB] destroy");
		return 0;
	}
	
	@Override
	public int del(String key) {
		logger.error("[DefaultLevelDB] del");
		return 0;
	}
	
	@Override
	public void close() {
		logger.error("[DefaultLevelDB] close");
	}

}
