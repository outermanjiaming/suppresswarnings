/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.osgi.corpus.backup;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Status<T> {
	long startFromWhen;
	transient boolean busy;
	int index;
	T that;
	public Status(T the){
		this.startFromWhen = System.currentTimeMillis();
		this.busy = false;
		this.index = 0;
		this.that = the;
	}
	
	public void busy(){
		this.busy = true;
		++ this.index;
	}
	
	public void rest() {
		-- this.index;
		if(this.index <= 0) {
			this.busy = false;
			this.index = 0;
		}
	}
	
	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return "Status<" + that.getClass() + "> [" + busy + "] " + index + ", from " + sdf.format(new Date(startFromWhen));
	}
	
}
