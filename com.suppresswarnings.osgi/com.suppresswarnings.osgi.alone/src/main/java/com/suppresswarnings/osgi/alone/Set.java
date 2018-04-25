/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.osgi.alone;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Set {
	/**
	 * 0 no need change type
	 * 1 primitive to String
	 * 2 String to primitive
	 * 3 List<T>
	 * @return
	 */
	int type() default 0;
	String value();
	Class<?> clazz() default Object.class;
}
