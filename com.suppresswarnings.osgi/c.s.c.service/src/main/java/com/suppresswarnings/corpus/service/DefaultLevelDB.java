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

import com.suppresswarnings.osgi.leveldb.LevelDB;

public class DefaultLevelDB implements LevelDB {
	String dbName;
	public DefaultLevelDB(String name) {
		this.dbName = name;
	}
	@Override
	public byte[] read(String key) {
		return null;
	}
	
	@Override
	public int put(String key, byte[] value) {
		return 0;
	}
	
	@Override
	public int put(String key, String value) {
		return 0;
	}
	
	@Override
	public String page(String head, String start, AtomicBoolean stop, long limit, BiConsumer<String, String> consumer) {
		return null;
	}
	
	@Override
	public void list(String start, long limit, BiConsumer<String, String> consumer) {
		
	}
	
	@Override
	public boolean inited() {
		return false;
	}
	
	@Override
	public int init(String dbname, boolean create_if_missing) {
		return 0;
	}
	
	@Override
	public String getDBname() {
		return dbName;
	}
	
	@Override
	public String get(String key) {
		return null;
	}
	
	@Override
	public int destroy(String dbname) {
		return 0;
	}
	
	@Override
	public int del(String key) {
		return 0;
	}
	
	@Override
	public void close() {
	}

}
