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


import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface CallBy {
	 @GET("pi.http?action=raspberrypi")
	 Observable<String> sayhi(@Query("passwd") String action, @Query("var") String var, @Query("ip") String ip);
}
