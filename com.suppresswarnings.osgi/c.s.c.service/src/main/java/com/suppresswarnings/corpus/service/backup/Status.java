/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.backup;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Status<T> {
	long startFromWhen;
	transient boolean busy;
	T that;
	public Status(T the){
		this.startFromWhen = System.currentTimeMillis();
		this.busy = false;
		this.that = the;
	}
	
	public void busy(){
		this.startFromWhen = System.currentTimeMillis();
		this.busy = true;
	}
	
	public void rest() {
		this.busy = false;
	}
	
	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return "Status<" + that.getClass().getSimpleName() + "> [" + busy + "] from " + sdf.format(new Date(startFromWhen));
	}
	
}
