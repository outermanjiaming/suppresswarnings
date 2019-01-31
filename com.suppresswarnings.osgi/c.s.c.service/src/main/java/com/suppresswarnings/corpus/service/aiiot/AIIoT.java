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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.backup.Config;

public class AIIoT implements Closeable {
	public org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	public Map<String, Things> things = new ConcurrentHashMap<String, Things>();
	CorpusService service;
	int sslPort;
	AtomicBoolean on = new AtomicBoolean(true);
	SSLServerSocket serverSocket;
	public AIIoT(){}
	public AIIoT(CorpusService service) {
		this.service = service;
	}
	
	public String remoteCall(String openid, String code, String input, String origin, Context<CorpusService> context) {
		Things thing = things.get(code);
		if(thing == null) {
			logger.info("[AIIoT] remoteCall but thing is null for code: " + code);
			context.output("该设备不存在或离线");
			return null;
		}
		if(thing.isClosed()) {
			logger.info("[AIIoT] thing is closed: " + thing.toString());
			context.output("该设备离线");
			return null;
		}
		logger.info("[AIIoT] remoteCall("+openid+", "+code+", " +input+ ", " +context.state().name()+ ")");
		return thing.execute(input);
	}
	
	public void working() throws Exception {
		Properties config = new Properties();
		config.load(new FileInputStream(Config.serverConfigFilePath));
		logger.info("[AIIoT] load config: " + config.size());
		String sslPorts = config.getProperty(Config.keyAIIoTSslPort, Config.defaultAIIoTSslPort);
		sslPort = Integer.parseInt(sslPorts);
	    logger.info("[AIIoT] sslPorts: " + sslPort);
	    //System.setProperty("javax.net.debug", "ssl,handshake");
	    System.setProperty("javax.net.ssl.keyStore", config.getProperty("javax.net.ssl.keyStore"));
        System.setProperty("javax.net.ssl.trustStore", config.getProperty("javax.net.ssl.trustStore"));
        System.setProperty("javax.net.ssl.keyStorePassword", config.getProperty("javax.net.ssl.keyStorePassword"));    
        System.setProperty("javax.net.ssl.trustStorePassword",config.getProperty("javax.net.ssl.trustStorePassword"));
        
		long start = System.currentTimeMillis();
		ServerSocketFactory factory = SSLServerSocketFactory.getDefault();  
	    logger.info("[AIIoT] createServerSocket:" + sslPort);
		serverSocket = (SSLServerSocket) factory.createServerSocket(sslPort);   
	    serverSocket.setNeedClientAuth(true);
	    long time = System.currentTimeMillis() - start;
	    logger.info("[AIIoT] server socket prepared: " + time + "ms");
	    
	    while(on.get()) {
	    	if(serverSocket.isClosed()) {
	    		on.set(false);
	    		logger.error("[AIIoT] server socket is closed");
	    	}
			try {
				Socket socket = serverSocket.accept();
				logger.info("[AIIoT] accept socket: " + socket);
				String knock = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8")).readLine();
				logger.info("[AIIoT] knock knock: " + knock);
				String[] args = knock.split(",");
				String description = args[0];
				String code = args[1];
				String commands = args[2];
				service.account().put(String.join(Const.delimiter, Const.Version.V1, "AIIoT", "Info", code), description);
				service.account().put(String.join(Const.delimiter, Const.Version.V1, "AIIoT", "CMD", code), commands);
				Things exist = things.get(code);
				if(exist != null) {
					exist.close();
					logger.info("[AIIoT] close previos things");
				}
				//TODO lijiaming check code
				Things thing = newThings(description, code, socket);
				if(thing != null) {
					things.put(code, thing);
					logger.info("[AIIoT] new things register: " + thing.toString());
				} else {
					logger.info("[AIIoT] unknown things: " + knock);
				}
			} catch (Exception e) {
				logger.error("[AIIoT] Exception", e);
			}
	    }
	}

	public Things newThings(String desc, String code, Socket socket) {
		if(socket == null) return null;
		if(socket.isClosed()) {
			logger.info("[AIIoT] socket is closed");
			return null;
		}
		//check code 
		Things things = new Things(desc, code, socket);
		return things;
	}

	@Override
	public void close() {
		on.set(false);
		things.forEach((code, thing) ->{
			try {
				logger.info("[AIIoT] close " + thing.toString() + " closed: " + thing.close());
			} catch (Exception e) {
				logger.error("[AIIoT] close things client socket Exception");
			}
		});
		try {
			if(serverSocket != null) serverSocket.close();
		} catch (Exception e) {
			logger.error("[AIIoT] close serverSocket Exception", e);
		}
	}
	
}
