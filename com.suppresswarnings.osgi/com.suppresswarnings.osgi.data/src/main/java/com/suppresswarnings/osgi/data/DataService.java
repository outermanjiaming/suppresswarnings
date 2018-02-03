package com.suppresswarnings.osgi.data;

import java.util.Spliterator;
import java.util.concurrent.Future;

public interface DataService {
	//create a textdata into database, return a callback function which you can get a reply from
	public Future<String> create(String uuid, String attribute, String value);
	public Future<String> complement(TextData data);
	public Spliterator<TextData> getAllMyData(String uid, String...strings);
	public Spliterator<TextData> getDataByQuestion(String question, String...strings);
	public Spliterator<TextData> getDataByClassify(String classify, String...strings);
	public Spliterator<TextData> getDataByPeriod(String start, String end);
}
