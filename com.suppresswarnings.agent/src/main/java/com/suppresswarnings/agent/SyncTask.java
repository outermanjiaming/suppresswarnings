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

import retrofit2.Retrofit;
import rx.Subscriber;

public class SyncTask implements Runnable {
	Retrofit retrofit;
	Agent agent;
	
	public SyncTask(Retrofit retrofit, Agent agent) {
		this.retrofit = retrofit;
		this.agent = agent;
	}
	
	@Override
	public void run() {
		LOG.info(SyncTask.class, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>start to request " + agent.server);
		retrofit.create(CallBy.class)
		.backup(agent.identity, agent.which, "" + agent.capacity)
		.subscribe(new Subscriber<String>() {

			@Override
			public void onCompleted() {
				LOG.info(SyncTask.class, "response finish");
			}

			@Override
			public void onError(Throwable e) {
				LOG.info(SyncTask.class, "fail to request " + e.getMessage());
			}

			@Override
			public void onNext(String t) {
				LOG.info(SyncTask.class, agent.server + " return " + t);
				if("true".equals(t)) {
					try {
						long time = agent.working();
						LOG.info(SyncTask.class, "cost: " + time + "ms");
					} catch (Exception e) {
						LOG.info(SyncTask.class, "Exeption:" + e.getMessage());
					}
				}
			}
		});
	}

}
