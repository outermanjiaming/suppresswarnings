/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.main;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.suppresswarnings.corpus.common.Format;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.service.http.CallableDownload;
import com.suppresswarnings.corpus.service.http.CallableGet;
import com.suppresswarnings.osgi.leveldb.LevelDB;
import com.suppresswarnings.osgi.leveldb.LevelDBImpl;

public class AACrawler {

	public static void list(LevelDB levelDB ){
		levelDB.list("0", Integer.MAX_VALUE, (x,y)->{
			System.out.println(x+" == " + y);
		});
		System.exit(0);
	}
	public static void main(String[] args) {
		LevelDB levelDB = new LevelDBImpl("aa");
		list(levelDB);
		ConcurrentHashMap<String, String> all = new ConcurrentHashMap<>();
		AtomicInteger integer = new AtomicInteger(0); 
		CallableGet get = new CallableGet("https://www.aa.co.nz/RoadCodeQuizController/getSet", "");
		Format format = new Format(new String[]{"qImg/{IMAGE}?m"});
		while(true) {
			try {
				String json = get.call();
				Gson gson = new Gson();
				JsonArray list = gson.fromJson(json, JsonArray.class);
				list.forEach(one -> {
					AA aa = gson.fromJson(one, AA.class);
					List<KeyValue> kValues = format.matches(aa.getImage());
					String image = kValues.get(0).value();
					
					String key = aa.getQuestion().trim() + "-" + aa.getCorrectAnswer() + "-" + aa.getRoadCodePage() + "-" + image;
					
					all.put(key, one.toString());
					levelDB.put(key, one.toString());
					
					if(levelDB.get("001.Image."+image) == null) {
						CallableDownload download = new CallableDownload("https://www.aa.co.nz/assets/motoring/rcq/qImg/" +image , CallableDownload.MB10, "/Users/lijiaming/company/AA/qImg", image);
						try {
							File file = download.call();
							levelDB.put("001.Image."+image, file.getAbsolutePath());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					System.out.println(all.size() + " <-> " + integer.incrementAndGet() + "\t" + key);
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
	}
}
