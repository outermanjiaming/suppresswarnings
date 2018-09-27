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
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.CheckUtil;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.backup.Config;

public class AIIoT implements ThingsFactory, Closeable {
	public org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	public Map<String, Things> things = new ConcurrentHashMap<String, Things>();
	public Map<String, Class<?>> types = new HashMap<>();
	public Map<String, List<String>> typesCMD = new HashMap<>();
	public String typesName = null;
	
	int sslPort;
	AtomicBoolean on = new AtomicBoolean(true);
	SSLServerSocket serverSocket;
	
	public String remoteCall(String openid, String code, String input, String origin, Context<CorpusService> context) {
		Things thing = things.get(code);
		if(thing == null) {
			logger.info("[AIIoT] remoteCall but thing is null for code: " + code);
			return null;
		}
		if(thing.isClosed()) {
			logger.info("[AIIoT] thing is closed: " + thing.toString());
			return null;
		}
		logger.info("[AIIoT] remoteCall("+openid+","+code+", " +input+ ", " +context.state().name()+ ")");
		Method[] methods = thing.getClass().getDeclaredMethods();
        for(Method method : methods) {
        	CMD annotation = method.getAnnotation(CMD.class);
        	logger.info("[AIIoT] method: " + method + ", cmd: " + annotation);
        	if(annotation == null) continue;
        	if(annotation.value().equals(input)) {
        		try {
        			String cmd = (String) method.invoke(thing, origin);
					return thing.execute(cmd);
				} catch (Exception e) {
					logger.error("[AIIoT] invoke Exception: " + method.getName(), e);
				}
        	}
        }
		return null;
	}
	public String registerCMD(CorpusService service) {
		if(types.isEmpty()) {
			registerThings();
		}
		types.forEach((type, klass) ->{
			List<String> cmds = new ArrayList<>();
			logger.info("[AIIoT] register cmd for this type: " + type);
			Method[] methods = klass.getDeclaredMethods();
	        for(Method method : methods) {
	        	CMD annotation = method.getAnnotation(CMD.class);
	        	if(annotation == null) continue;
	        	
	        	String cmd = annotation.value();
	        	cmds.add(cmd);
				String aid = service.questionToAid.get(cmd);
				logger.info("[AIIoT] register cmd: " + cmd + ", aid " + aid);
				if(aid != null) {
					HashSet<String> similars = service.aidToSimilars.get(aid);
					if(similars == null || similars.size() == 0) {
						logger.info("[AIIoT] registerCMD find no similars: " + cmd);
					} else {
						for(String similar : similars) {
							String key = CheckUtil.cleanStr(similar);
							service.aidToCommand.put(key, cmd);
							logger.info("[AIIoT] register " + key + " to " + cmd);
						}
					}
				} else {
					logger.info("[AIIoT] registerCMD find no such cmd: " + cmd);
				}
	        }
	        typesCMD.put(type, cmds);
		});
		typesName = types.keySet().toString();
		return typesName;
	}
	
	//TODO lijiaming: important
	public int registerThings() {
		types.put(Bulb.class.getSimpleName(), Bulb.class);
		//put more
		
		
		return types.size();
	}
	
	public void working() throws Exception {
		logger.info("[AIIoT] start working, types: " + registerThings());
		
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
				//1.read from,capacity,WhichDB
				String knock = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8")).readLine();
				logger.info("[AIIoT] knock knock: " + knock);
				String[] args = knock.split(",");
				String type = args[0];
				String code = args[1];
				//TODO lijiaming check code
				Things thing = newThings(type, code, socket);
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


	@Override
	public Things newThings(String type, String code, Socket socket) {
		if(socket == null) return null;
		if(socket.isClosed()) {
			logger.info("[AIIoT] socket is closed");
			return null;
		}
		if(Bulb.class.getSimpleName().equals(type)) {
			return new Bulb(code, socket);
		}
		return null;
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
	
	
	public static void main(String[] args) {
		Bulb thing = new Bulb("T_Code_123", null);
		Method[] methods = thing.getClass().getDeclaredMethods();
        for(Method method : methods) {
        	CMD annotation = method.getAnnotation(CMD.class);
        	System.out.println(method + " == " + annotation);
        	if(annotation == null) continue;
        }
	}
}
