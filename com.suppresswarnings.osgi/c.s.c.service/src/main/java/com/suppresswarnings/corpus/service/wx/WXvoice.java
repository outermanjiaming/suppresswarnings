package com.suppresswarnings.corpus.service.wx;

import java.util.List;

import com.suppresswarnings.corpus.common.Format.KeyValue;


public class WXvoice extends WXmsg {
	public String MediaID;
	public String Format;
	public String MsgID;
	public String Recognition;
	@Override
	public void set(List<KeyValue> kvs) {
		this.MediaID = kvs.get(4).value();
		this.Format = kvs.get(5).value();
		this.MsgID = kvs.get(6).value();
		this.Recognition = kvs.get(7).value();
	}

}
