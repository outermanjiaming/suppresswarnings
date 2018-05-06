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

import rx.Subscriber;

public class ServerResult extends Subscriber<String> {
	String result;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "ServerResult [result=" + result + "]";
	}

	@Override
	public void onCompleted() {
		System.out.println("onCompleted");
	}

	@Override
	public void onError(Throwable arg0) {
		System.out.println("onError: " + arg0.getMessage());
	}

	@Override
	public void onNext(String arg0) {
		System.out.println(arg0);
		result = arg0;
	}
	
}
