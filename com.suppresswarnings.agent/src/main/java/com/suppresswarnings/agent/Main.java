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

import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
/**
 * agent Entrance
 * @author lijiaming
 *
 */
public class Main {
	public static void main(String[] args) {
		Properties config = new Properties();
		try {
			config.load(new FileInputStream("agent.properties"));
			Agent agent = new Agent(config);
			OkHttpClient.Builder builder = new OkHttpClient
					.Builder()
					.connectTimeout(3000, TimeUnit.MILLISECONDS)
					.writeTimeout(5000,TimeUnit.MILLISECONDS)
					.readTimeout(10000,TimeUnit.MILLISECONDS);
			Retrofit retrofit = new Retrofit
					.Builder()
					.client(builder.build())
					.baseUrl("http://" + agent.server)
					.addConverterFactory(new ToStringConverter())
					.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
					.build();
			SyncTask synctask = new SyncTask(retrofit, agent);
			ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
			LOG.info(Main.class, "sync task scheduled");
			Integer minutes = Integer.parseInt(config.getProperty("agent.schedule.minutes", "120"));
			ses.scheduleWithFixedDelay(synctask, TimeUnit.SECONDS.toMillis(10), TimeUnit.MINUTES.toMillis(minutes), TimeUnit.MILLISECONDS);
			LOG.info(Main.class, "sync task would execute every hour");
		} catch (Exception e) {
			LOG.info(Main.class, "fail to start agent: " + e.getMessage());
		}
	}
}
