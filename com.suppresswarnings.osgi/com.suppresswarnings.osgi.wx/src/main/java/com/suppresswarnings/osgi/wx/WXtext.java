package com.suppresswarnings.osgi.wx;

import java.util.List;

import com.suppresswarnings.osgi.alone.Format.KeyValue;

public class WXtext extends WXmsg {
	public String Content;
	public String MsgId;
	@Override
	public void set(List<KeyValue> kvs) {
		this.Content = kvs.get(4).value();
		this.MsgId = kvs.get(5).value();
	}
}
