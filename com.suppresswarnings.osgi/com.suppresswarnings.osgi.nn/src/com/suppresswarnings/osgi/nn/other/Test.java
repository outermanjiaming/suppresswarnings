package com.suppresswarnings.osgi.nn.other;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class Test {

	public static void main(String[] args) {
		Unsafe safe = Safe.INSTANCE.get();
		long address = 0;
		address = safe.allocateMemory(Integer.MAX_VALUE * 2L);
		safe.putLong(address+20, address);
		System.out.println(address+"+20 == "+safe.getLong(address+20));
	}
}
