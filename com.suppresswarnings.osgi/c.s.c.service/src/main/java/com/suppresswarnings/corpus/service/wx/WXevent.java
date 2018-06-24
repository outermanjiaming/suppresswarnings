/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.wx;

import java.util.List;

import com.suppresswarnings.corpus.common.KeyValue;

public class WXevent extends WXmsg {
	public String event;
	public String eventKey;
	public String tiket;

	@Override
	public void set(List<KeyValue> kvs) {
		this.event = get(4, kvs);
		this.eventKey = get(5, kvs);
		this.tiket = get(6, kvs);
	}

}
