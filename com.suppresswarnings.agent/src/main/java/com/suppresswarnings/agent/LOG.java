/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.agent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LOG {
	public static void info(Class<?> clazz, String msg) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		System.out.println(sdf.format(new Date()) +" ["+clazz.getSimpleName()+"] " + msg);
	}
}
