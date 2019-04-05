package com.suppresswarnings.things;

import javax.net.SocketFactory;

import com.suppresswarnings.things.security.a;
import com.suppresswarnings.things.security.b;
import com.suppresswarnings.things.security.c;

@SuppressWarnings("用于创建和素朴网联的Socket连接")
public interface Config{
	SocketFactory factory();
	static byte[] code() {
		byte[][] bbs = {
				a.b1,a.b2,a.b3,a.b4,a.b5,a.b6,a.b7,a.b8,
				a.b9,a.b10,
				b.b11,b.b12,b.b13,b.b14,b.b15,b.b16,b.b17,
				b.b18,c.b19,c.b20,
				c.b21,c.b22,c.b23,c.b24,c.b25,c.b26,c.b27,
				b.b28,b.b29
		};
		int size = 0;
		for(byte[] bs : bbs) {
			size += bs.length;
		}
		byte[] b = new byte[size];
		int index = 0;
		for(byte[] bs : bbs) {
			System.arraycopy(bs, 0, b, index, bs.length);
			index += bs.length;
		}
		return b;
	}
}