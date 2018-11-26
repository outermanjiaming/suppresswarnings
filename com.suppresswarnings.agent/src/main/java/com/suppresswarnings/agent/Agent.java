/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.agent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Properties;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class Agent {
	String folder;
	String identity;
	String server;
	int port;
	String which;
	long capacity;
	File parent;
	public Agent(Properties config) {
		System.setProperty("javax.net.ssl.keyStore", config.getProperty("javax.net.ssl.keyStore"));
        System.setProperty("javax.net.ssl.trustStore", config.getProperty("javax.net.ssl.trustStore"));
        System.setProperty("javax.net.ssl.keyStorePassword", config.getProperty("javax.net.ssl.keyStorePassword"));    
        System.setProperty("javax.net.ssl.trustStorePassword",config.getProperty("javax.net.ssl.trustStorePassword"));
        
		this.which = config.getProperty("agent.backup.which", "backup");
		this.folder = config.getProperty("agent.backup.folder", ".");
        this.identity = config.getProperty("agent.backup.identity", "identity");
        this.server = config.getProperty("server.ssl.ip", "139.199.104.224");
        String port = config.getProperty("server.ssl.port", "6616");
        this.port = Integer.valueOf(port);
        this.parent = new File(folder, which);
        if(!parent.exists()) {
        	parent.mkdirs();
        }
        this.capacity = parent.getUsableSpace();
	}
	
	public long working() throws Exception {
		long start = System.currentTimeMillis();
        SocketFactory factory = SSLSocketFactory.getDefault();    
	    Socket sslsocket = factory.createSocket(server, port);    
	    LOG.info(Agent.class, "Client Connected");
	    String knock = String.join(",", this.identity, "" + this.capacity, this.which) + "\n";
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sslsocket.getOutputStream(),"UTF-8"));    
        out.write(knock);
        out.flush();
        LOG.info(Agent.class, "Msg Sent = " + knock);
        InputStream is = sslsocket.getInputStream();
        //String msg = String.join(Const.delimiter, which.name(), from, name, "" + i, "" + all, "" + size);
        ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
        while(!sslsocket.isClosed()) {
	        BufferedReader in = new BufferedReader(new InputStreamReader(is,"UTF-8"));
	        String msg = in.readLine();
	        LOG.info(Agent.class, "===================== receive Log: " + msg);
	        if(msg == null || "CLOSE".equals(msg)) {
	        	LOG.info(Agent.class, "file none");
	        	break;
	        }
	        String[] argv = msg.split(",");
	        long length = Long.valueOf(argv[5]);
	        String name = argv[2];
	        File backup = new File(parent, name);
	        if(!backup.exists()) {
	        	backup.getParentFile().mkdirs();
	        	backup.createNewFile();
	        }
	        if(length <= 0) {
	        	LOG.info(Agent.class, "zero file");
	        	continue;
	        }
	        ReadableByteChannel channel = Channels.newChannel(is);
	        FileOutputStream fos = new FileOutputStream(backup);
	        FileChannel saveTo = fos.getChannel();
	        long read = 0;
	        while(channel.read(buffer) != -1) {
	        	LOG.info(Agent.class, "data size: "+buffer.position());
	        	read += buffer.position();
	        	buffer.flip();
	        	while(buffer.hasRemaining()) {
	        		saveTo.write(buffer);
	        	}
	        	buffer.clear();
	        	this.capacity = this.capacity - read;
	        	if(read >= length) {
	        		break;
	        	}
	        }
	        fos.close();
	        saveTo.close();
        }
	    sslsocket.close();
		return System.currentTimeMillis() - start;
	}
	
	public static void main(String[] args) {
		File parent = new File("/Users/lijiaming/Codes/SuppressWarnings/tmp/");
		File son = new File(parent, "bak/son/file.log");
		try {
			System.out.println(son.getParentFile().mkdirs());
			boolean b = son.createNewFile();
			System.out.println(b);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
