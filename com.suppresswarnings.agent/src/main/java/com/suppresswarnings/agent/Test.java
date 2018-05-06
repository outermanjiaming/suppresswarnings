/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.agent;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suppresswarnings.osgi.leveldb.LevelDB;
import com.suppresswarnings.osgi.leveldb.LevelDBImpl;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

public class Test {
	public static final String BASE_URL = "http://139.199.104.224/"; 
	public static void main(String[] args) {
		LevelDB leveldb = new LevelDBImpl("leveldb");
		NumberFormat nf = new DecimalFormat("##########");
		Random random = new Random();
		for(int i =1;i< 1000000;i++) {
			leveldb.put("Key-" + nf.format(random.nextInt()), "Value-" + i);
		}
		leveldb.list("Key", 100000, new BiConsumer<String, String>() {
			
			@Override
			public void accept(String t, String u) {
				System.out.println(t+ " == " +u);
			}
		});
		leveldb.close();
		ServerResult src = new ServerResult();
		src.setResult("key=\"value\"");
		Gson gson = new GsonBuilder().create();
		String sss = gson.toJson(src);
		System.out.println(sss);
		ServerResult des = gson.fromJson(sss, ServerResult.class);
		System.out.println(des.getResult());
		OkHttpClient.Builder builder = new OkHttpClient
				.Builder()
				.connectTimeout(3000, TimeUnit.MILLISECONDS)
				.writeTimeout(5000,TimeUnit.MILLISECONDS)
				.readTimeout(10000,TimeUnit.MILLISECONDS);
		Retrofit retrofit = new Retrofit
				.Builder()
				.client(builder.build())
				.baseUrl("http://139.199.104.224/")
				.addConverterFactory(new ToStringConverter())
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.build();
		retrofit.create(CallBy.class).sayhi("raspberrypi", "LiJiaming", "Who Are You?\n").subscribe(new ServerResult());
	}
}
