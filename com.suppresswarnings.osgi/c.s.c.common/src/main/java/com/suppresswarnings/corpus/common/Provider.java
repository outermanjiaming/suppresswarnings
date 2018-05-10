/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.common;

public interface Provider<T> {
	public String identity();
	public String description();
	public T instance();
}
