package com.suppresswarnings.corpus.service.wx;

import java.util.List;

import com.suppresswarnings.corpus.common.KeyValue;


public class WXtext extends WXmsg {
	public String Content;
	public String MsgId;
	@Override
	public void set(List<KeyValue> kvs) {
		this.Content = get(4, kvs);
		this.MsgId = get(5, kvs);
	}
}
