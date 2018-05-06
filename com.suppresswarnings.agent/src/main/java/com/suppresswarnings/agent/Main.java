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

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Subscriber;

public class Main {
	public static void main(String[] args) {
		String which = "Data";
		
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
		retrofit.create(CallBy.class)
		.backup("raspberrypi-1", "Data", "102400")
		.subscribe(new Subscriber<String>() {

			@Override
			public void onCompleted() {
				System.out.println("[backup] response finish");
			}

			@Override
			public void onError(Throwable e) {
				System.out.println("[backup] fail to request " + e.getMessage());
			}

			@Override
			public void onNext(String t) {
				System.out.println("[backup] return " + t);
				if("true".equals(t)) {
					try {
						Agent agent = new Agent(which);
						long time = agent.working();
						System.out.println("[backup] cost: " + time + "ms");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
}
