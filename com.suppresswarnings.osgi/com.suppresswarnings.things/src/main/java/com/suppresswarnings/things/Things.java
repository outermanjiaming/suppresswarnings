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

public interface Things {
	String SUCCESS = "success";
	String FAIL = "fail";
	String ERROR = "error";
	/**
	 * description of this thing
	 * @return
	 */
	public String description();
	/**
	 * the unique code of this thing
	 * get it from 公众号: 素朴网联
	 * 在公众号输入"我要物联网",即可返回code
	 * @return
	 */
	public String code();
}
