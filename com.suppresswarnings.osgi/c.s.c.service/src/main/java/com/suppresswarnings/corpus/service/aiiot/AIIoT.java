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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.backup.Config;
import com.suppresswarnings.corpus.service.wx.QRCodeTicket;

public class AIIoT implements Closeable {
	public org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	public Map<String, Things> things = new ConcurrentHashMap<String, Things>();
	Gson gson = new Gson();
	CorpusService service;
	int sslPort;
	AtomicBoolean on = new AtomicBoolean(true);
	SSLServerSocket serverSocket;
	ScheduledExecutorService scheduledExecutorService;
	public AIIoT(){}
	public AIIoT(CorpusService service) {
		this.service = service;
		this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	}
	public String ping(String code) {
		Things things = service.aiiot.things.get(code);
		if(things == null) return "null";
		if(things.isClosed()) return "closed";
		long diff = things.ping();
		logger.info(code + " has diff " + diff);
		return "success";
	}
	public String remoteCall(String openid, String code, String cmd, String input, Context<?> context) {
		Things thing = service.aiiot.things.get(code);
		logger.info("[AIIoT] remoteCall "+ thing);
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
		logger.info("[AIIoT] remoteCall("+openid+", "+thing+", " +input+ ", " +context+ ")");
		return thing.execute(cmd, input);
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
	    scheduledExecutorService.scheduleWithFixedDelay(() -> {
	    	List<String> remove = new ArrayList<>();
	    	things.forEach((code, thing) ->{
				try {
					long diff = thing.diff();
					if(diff > TimeUnit.SECONDS.toMillis(30)) {
						logger.info("[AIIoT] checking " + thing + " closed: " + thing.close());
						if(thing.close()) {
							logger.info("[AIIoT] going to remove " + thing);
							remove.add(code);
						}
					}
				} catch (Exception e) {
					logger.error("[AIIoT] checking things client socket Exception");
				}
			});
	    	remove.forEach(code -> things.remove(code));
	    }, 2, 10, TimeUnit.SECONDS);
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
				String qrScene = code;
				String accessToken = service.accessToken("things qrcode");
				service.account().put(String.join(Const.delimiter, Const.Version.V1, "QRCode", qrScene), qrScene);
				service.setGlobalCommand(qrScene, "智能家居设备", code, "" + System.currentTimeMillis());
				String json = service.qrCode(accessToken, 10000, "QR_STR_SCENE", qrScene);
				QRCodeTicket qrTicket = gson.fromJson(json, QRCodeTicket.class);
				String commands = args[2];
				service.account().put(String.join(Const.delimiter, Const.Version.V1, "AIIoT", "Info", code), description);
				service.account().put(String.join(Const.delimiter, Const.Version.V1, "AIIoT", "CMD", code), commands);
				Things exist = things.remove(code);
				if(exist != null) {
					exist.close();
					logger.info("[AIIoT] close previos things");
				}
				checkCMD(code, commands.split(";"));
				//TODO lijiaming check code
				Things thing = newThings(description, code, socket);
				if(thing != null) {
					things.put(code, thing);
					logger.info("[AIIoT] new things register: " + thing.toString());
					thing.execute("suppresswarnings.showqrcode", "http://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + qrTicket.getTicket());
				} else {
					logger.info("[AIIoT] unknown things: " + knock);
				}
			} catch (Exception e) {
				logger.error("[AIIoT] Exception", e);
			}
	    }
	}

	public void checkCMD(String code, String[] commands){
		String keyType = String.join(Const.delimiter, Const.Version.V1, "AIIoT", "Creator", code);
		String openid = service.account().get(keyType);
		for(String cmd : commands) {
			String keyCMD = String.join(Const.delimiter, Const.Version.V1, openid, "AIIoT", cmd);
			String codes = service.account().get(keyCMD);
			if(codes != null) {
				logger.info("[AIIoT] " +openid+ " already registered cmd: " + cmd + " for " + code);
			} else {
				service.account().put(keyCMD, code);
			}
		}
	}
	public Things newThings(String desc, String code, Socket socket) {
		if(socket == null) {
			logger.info("[AIIoT] socket is null");
		}
		if(socket.isClosed()) {
			logger.info("[AIIoT] socket is closed");
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
