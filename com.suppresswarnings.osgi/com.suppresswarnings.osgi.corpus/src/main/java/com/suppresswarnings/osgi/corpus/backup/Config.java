/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.osgi.corpus.backup;

import java.util.concurrent.TimeUnit;

public interface Config {
	String closeGate = "CLOSE\n";
	String defaultServerSslPort = "6616";
	String keyServerSslPort = "server.ssl.port";
	String serverConfigFilePath = "/root/osgi/conf/server.properties";
	long timeWaitUntilAcceptedMillis = TimeUnit.SECONDS.toMillis(30);
	String notebook = "/notebook";
	int bufferSize = 4096;
}
