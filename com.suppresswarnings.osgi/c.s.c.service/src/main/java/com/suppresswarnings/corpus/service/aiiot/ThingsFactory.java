/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.aiiot;

import java.net.Socket;

public interface ThingsFactory {
	public Things newThings(String type, String code, Socket socket);
}
