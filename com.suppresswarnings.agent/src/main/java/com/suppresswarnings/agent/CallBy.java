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
	 @GET("wx.http?action=backup")
	 Observable<String> backup(@Query("from") String from, @Query("which") String which, @Query("capacity") String capacity);
}
