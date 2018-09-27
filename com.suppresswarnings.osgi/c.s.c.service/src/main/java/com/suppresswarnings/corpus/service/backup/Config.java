/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.backup;

import java.util.concurrent.TimeUnit;

public interface Config {
	String endLine = "\n";
	String closeGate = "CLOSE";
	String defaultServerSslPort = "6616";
	String keyServerSslPort = "server.ssl.port";
	
	String defaultAIIoTSslPort = "6617";
	String keyAIIoTSslPort = "aiiot.ssl.port";
	String serverConfigFilePath = "/root/osgi/conf/server.properties";
	long timeWaitUntilAcceptedMillis = TimeUnit.SECONDS.toMillis(30);
	String notebook = "/notebook";
	int bufferSize = 4096;
}
