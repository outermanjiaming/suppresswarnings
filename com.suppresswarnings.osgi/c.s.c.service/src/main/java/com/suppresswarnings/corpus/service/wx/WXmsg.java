package com.suppresswarnings.corpus.service.wx;

import java.util.List;

import com.suppresswarnings.corpus.common.KeyValue;


public abstract class WXmsg {
	public String ToUserName;
	public String FromUserName;
	public String CreateTime;
	public String MsgType;
	public void init(List<KeyValue> kvs) {
		this.ToUserName = kvs.get(0).value();
		this.FromUserName = kvs.get(1).value();
		this.CreateTime = kvs.get(2).value();
		this.MsgType = kvs.get(3).value();
		set(kvs);
	}
	public abstract void set(List<KeyValue> kvs);
	public String get(int index, List<KeyValue> kvs) {
		if(kvs == null || kvs.size() < index + 1) {
			return null;
		}
		return kvs.get(index).value();
	}
}
