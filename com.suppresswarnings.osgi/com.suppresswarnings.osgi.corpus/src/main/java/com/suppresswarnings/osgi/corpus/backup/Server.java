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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.leveldb.LevelDB;
import com.suppresswarnings.osgi.leveldb.LevelDBImpl;

public class Server implements Closeable {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	SSLServerSocket serverSocket;
	LevelDB notebook;
	List<SSLSocket> sslSockets;
	Properties config;
	Status<Server> status;
	ScheduledExecutorService schedule;
	ExecutorService agentPool;
	Runnable accept;
	int sslPort;
	public LevelDB leveldb(){
		return notebook;
	}
	public Server(String account, String data, String token) {
		sslSockets = new ArrayList<SSLSocket>();
		notebook = new LevelDBImpl(Config.notebook);
	}
	/**
	 * when working, be prepared and do reports regularly
	 * 1.load server.properties
	 * 2.create server socket
	 * 3.new status
	 * 4.new schedule
	 */
	public void working() throws Exception {
		logger.info("[Server] start working");
		config = new Properties();
		config.load(new FileInputStream(Config.serverConfigFilePath));
		logger.info("[Server] load config: " + config.size());
		String sslPorts = config.getProperty(Config.keyServerSslPort, Config.defaultServerSslPort);
		sslPort = Integer.parseInt(sslPorts);
	    logger.info("[Server] sslPorts: " + sslPort);
	    //System.setProperty("javax.net.debug", "ssl,handshake");
	    System.setProperty("javax.net.ssl.keyStore", config.getProperty("javax.net.ssl.keyStore"));
        System.setProperty("javax.net.ssl.trustStore", config.getProperty("javax.net.ssl.trustStore"));
        System.setProperty("javax.net.ssl.keyStorePassword", config.getProperty("javax.net.ssl.keyStorePassword"));    
        System.setProperty("javax.net.ssl.trustStorePassword",config.getProperty("javax.net.ssl.trustStorePassword"));
        logger.info("[Server] getDefault:" + sslPorts);
	    status = new Status<Server>(this);
	    schedule = new ScheduledThreadPoolExecutor(7, new ThreadFactory() {
			int thread = 0;
			@Override
			public Thread newThread(Runnable r) {
				++ thread;
				return new Thread(r, "Server-schedule-" + thread);
			}
		}, new ThreadPoolExecutor.AbortPolicy());
	    logger.info("[Server] schedule created");
	    agentPool = Executors.newFixedThreadPool(6, new ThreadFactory() {
			int thread = 0;
			@Override
			public Thread newThread(Runnable r) {
				++ thread;
				return new Thread(r, "Server-agent-" + thread);
			}
		});
	    logger.info("[Server] agent pool created");
	    accept = new Runnable() {
			
			@Override
			public void run() {
				try {
					if(status.busy) {
						logger.info("[Server accept] gate opened already");
						return;
					}
					status.busy();
					long start = System.currentTimeMillis();
					ServerSocketFactory factory = SSLServerSocketFactory.getDefault();  
				    logger.info("[Server accept] createServerSocket:" + sslPorts);
					serverSocket = (SSLServerSocket) factory.createServerSocket(sslPort);   
				    serverSocket.setNeedClientAuth(true);
				    long time = System.currentTimeMillis() - start;
				    logger.info("[Server accept] server socket prepared: " + time + "ms");
					Socket socket = serverSocket.accept();
					logger.info("[Server accept] accept socket: " + socket);
					serverSocket.close();
					serverSocket = null;
					status.rest();
					//1.read from,capacity,WhichDB
					String knock = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8")).readLine();
					logger.info("[Server accept] knock knock: " + knock);
					if(knock == null || knock.trim().isEmpty()) {
						logger.info("[Server accept] please knock first(with from,1000,Data)");
						return;
					}
					//2.if close me, return
					if(Config.closeGate.equals(knock)) {
						logger.info("[Server accept] close gate");
						return;
					}
					//TODO knock with identity
					String[] identity = knock.split(",");
					if(identity.length != 3) {
						logger.info("[Server accept] wrong knock");
						return;
					}
					SSLSocket agent = new SSLSocket(notebook, identity[2], socket, identity[0], Long.valueOf(identity[1]));
					agentPool.execute(agent);
					logger.info("[Server] new agent executed: " + agent.toString());
				} catch (Exception e) {
					logger.error("[Server] working on accepting new agents error: ", e);
				}
			}
		};
	    //TODO won't accept until agent asked for, schedule.execute(accept);
	    logger.info("[Server] working accept created and executed");
	    schedule.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				try {
					logger.info("[Server] schedule: " + status.toString());
				} catch (Exception e) {
					logger.error("[Server] schedule Exception", e);
				}
			}
		}, 10, 360, TimeUnit.SECONDS);
	    logger.info("[Server] schedule running");
	}
	
	public SSLSocket getAgentAlive(String from) {
		for(SSLSocket exist : sslSockets) {
			if(from.equals(exist.from) && exist.status.busy && exist.socket != null && !exist.socket.isClosed()) {
				logger.info("[Server] get agent exist = " + exist.toString());
				return exist;
			}
		}
		return null;
	}
	public boolean newAgent(String from, long capacity) {
		SSLSocket exist = getAgentAlive(from);
		if(exist != null) {
			logger.info("[Server] new agent already exist");
			return false;
		}
		
		if(status.busy) {
			logger.info("[Server] new agent ready to accept");
		} else {
			schedule.execute(accept);
			schedule.schedule(new Runnable() {
				
				@Override
				public void run() {
					try {
						SSLSocket exist = getAgentAlive(from);
						if(exist != null) {
							logger.info("[Server] close gate no need to do: still none");
							return;
						}
						if(!status.busy) {
							logger.info("[Server] close gate no need to do: already accepted");
							return;
						}
						logger.info("[Server] close gate connect to server and close it");
						SocketFactory factory = SSLSocketFactory.getDefault();
					    Socket close = factory.createSocket("127.0.0.1", sslPort);
					    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(close.getOutputStream(),"UTF-8"));    
				        out.write(Config.closeGate);
				        out.write(Config.endLine);
				        out.flush();
				        close.close();
				        logger.info("[Server] close gate by itself");
					} catch (Exception e) {
						logger.info("[Server] close gate stop server socket from waiting");
					}
				}
			}, Config.timeWaitUntilAcceptedMillis, TimeUnit.MILLISECONDS);
		}
		return true;
	}
	@Override
	public void close() {
		try {
			if(schedule != null) schedule.shutdownNow();
			if(serverSocket != null) serverSocket.close();
			if(notebook != null) notebook.close();
			if(agentPool != null) agentPool.shutdownNow();
			if(sslSockets != null) sslSockets.clear();
		} catch (Exception e) {
			logger.error("[Server] close Exception", e);
		}
	}
}
