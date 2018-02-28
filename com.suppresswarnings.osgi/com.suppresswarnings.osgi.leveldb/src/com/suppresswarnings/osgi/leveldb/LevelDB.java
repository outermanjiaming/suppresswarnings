package com.suppresswarnings.osgi.leveldb;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public interface LevelDB {
	public int NO = 0;
	public int OK = 1;
	public int FAIL = 400;
	public int EXCEPTION = 500;
	public int init(String dbname, boolean create_if_missing);
	public void close();
	public int destroy(String dbname);
	public int put(String key, String value);
	public int put(String key, byte[] value);
	public int del(String key);
	public String get(String key);
	public byte[] read(String key);
	public void list(String start, long limit, BiConsumer<String, String> consumer);
	public String page(String head, String start, AtomicBoolean stop, long limit, BiConsumer<String, String> consumer);
}
