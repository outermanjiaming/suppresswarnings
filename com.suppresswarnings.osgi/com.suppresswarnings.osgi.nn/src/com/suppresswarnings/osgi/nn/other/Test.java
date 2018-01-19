package com.suppresswarnings.osgi.nn.other;

import java.util.concurrent.atomic.AtomicInteger;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class Test {

	public static void main(String[] args) {
		Unsafe safe = Safe.INSTANCE.get();
		long address = 0;
		address = safe.allocateMemory(Integer.MAX_VALUE * 2L);
		safe.putLong(address+20, address);
		System.out.println(address+"+20 == "+safe.getLong(address+20));
		
		AtomicInteger integer = new AtomicInteger(100);
		for(int i=0;i<10;i++) {
			System.out.println(i+" == "+integer.getAndIncrement());
		}
	}
}
