/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.things;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Inherited
@Target({
	ElementType.TYPE, 
	ElementType.FIELD, 
	ElementType.METHOD, 
	ElementType.PARAMETER, 
	ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface SuppressWarnings {

	/**
	 * the command for this method
	 * @return
	 */
	String value();
}
