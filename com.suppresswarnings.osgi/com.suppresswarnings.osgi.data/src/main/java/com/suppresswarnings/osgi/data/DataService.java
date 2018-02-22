package com.suppresswarnings.osgi.data;

import java.util.function.BiConsumer;

import com.suppresswarnings.osgi.leveldb.LevelDB;

public interface DataService {
	public int save(String key, String value);
	public int unknown(String uid, String value);
	public int answer(String uid, String question, String value);
	public int classify(String uid, String classify, String value);
	public void listSome(String start, long limit, BiConsumer<String, String> consumer);
	public LevelDB leveldb();
}
