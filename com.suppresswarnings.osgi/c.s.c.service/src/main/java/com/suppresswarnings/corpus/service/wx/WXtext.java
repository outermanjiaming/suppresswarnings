package com.suppresswarnings.corpus.service.wx;

import java.util.List;

import com.suppresswarnings.corpus.common.KeyValue;


public class WXtext extends WXmsg {
	public String Content;
	public String MsgId;
	@Override
	public void set(List<KeyValue> kvs) {
		this.Content = kvs.get(4).value();
		this.MsgId = kvs.get(5).value();
	}
}
