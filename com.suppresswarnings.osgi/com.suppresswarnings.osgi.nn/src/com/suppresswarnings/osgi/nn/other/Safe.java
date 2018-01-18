package com.suppresswarnings.osgi.nn.other;

import java.lang.reflect.Field;

@SuppressWarnings("restriction")
public enum Safe {
	INSTANCE;
	sun.misc.Unsafe unsafe;
	sun.misc.Unsafe get(){
		if(unsafe == null) {
			try {
				Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
				f.setAccessible(true);
				unsafe = (sun.misc.Unsafe) f.get(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return unsafe;
	}
}
