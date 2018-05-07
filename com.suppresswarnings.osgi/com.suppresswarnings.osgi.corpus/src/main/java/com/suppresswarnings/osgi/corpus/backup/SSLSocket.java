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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.leveldb.LevelDB;

public class SSLSocket implements Runnable {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	ByteBuffer buffer = ByteBuffer.allocateDirect(Config.bufferSize);
	Socket socket;
	String from;
	long capacity;
	String which;
	Status<SSLSocket> status;
	LevelDB notebook;
	Set<String> blacklist;
	public SSLSocket(LevelDB notebook, String which, Socket client, String from, long capacity){
		this.notebook = notebook;
		this.socket = client;
		this.from = from;
		this.capacity = capacity;
		this.which = which;
		this.status = new Status<SSLSocket>(this);
		blacklist = new HashSet<String>();
	}
	@Override
	public String toString() {
		return "SSLSocket [socket closed =" + socket.isClosed() + ", from=" + from + ", capacity=" + capacity + ", accept=" + which + ", status=" + status + "]";
	}

	@Override
	public void run() {
		try {
			this.status.busy();
			String black = "agent.blacklist.dir";
			notebook.page(black, black, null, 10000, new BiConsumer<String, String>() {
				
				@Override
				public void accept(String t, String dbPath) {
					blacklist.add(dbPath);
				}
			});
			logger.info("[SSLSocket] blacklist: " + blacklist);
			String start = "agent.backup.dir";
			notebook.page(start, start, null, 10000, new BiConsumer<String, String>() {
				
				@Override
				public void accept(String t, String dbPath) {
					logger.info("[SSLSocket] start transfer: " + dbPath);
					File dir = new File(dbPath);
					try {
						sync(dir);
					} catch (Exception e) {
						logger.error("[SSLSocket] transfer Exception", e);
					}
			        logger.info("[SSLSocket] finish transfer: " + dbPath);
				}
			});
			
	        socket.close();
		} catch (Exception e) {
			logger.error("[SSLSocket] sync Exception", e);
		} finally {
			this.status.rest();
		}
	}
	
	public void sync(File root) throws Exception {
		if(blacklist.contains(root.getAbsolutePath())) {
			return;
		}
		File[] files = root.listFiles();
		int i = 0;
		boolean capable = true;
		int all = files.length;
		logger.info("[SSLSocket] sync folder: " + root + ", size: " + all);
		for(;i<all;i++) {
			if(socket.isClosed()) {
				logger.info("[SSLSocket] socket closed, so we stop here: " + i + " / " + all);
				return;
			}
			File file = files[i];
			
			if(file.isDirectory()) {
				logger.info("[SSLSocket] sync directory: " + file);
				sync(file);
			} else {
				logger.info("[SSLSocket] sync file: " + file.getName());
				long lastModified = file.lastModified();
				if(System.currentTimeMillis() - lastModified < 10000) {
					logger.info("[SSLSocket] file is still hot, ignore");
					continue;
				}
				long size = file.length();
				capacity = capacity - size;
				capable = capacity > 0;
				logger.info("[SSLSocket] " + capable + "\tcapable = " + capacity + ", from = " + from);
				//get which, from, name
				String key = String.join(",", which, from, file.getAbsolutePath());
				String existLastModified = notebook.get(key);
				boolean mustdo = false;
				if(existLastModified == null) {
					mustdo = true;
				} else {
					long existModifiedTime = Long.parseLong(existLastModified);
					mustdo = (existModifiedTime < lastModified);
				}
				logger.info("[SSLSocket] " + mustdo + " " + key + "\tlastModified: " +lastModified + " - " + existLastModified);
				if(mustdo) {
					long start = System.currentTimeMillis();
					OutputStream os = socket.getOutputStream();
					//1.which, from, name, i, all, space, 
					String msg = String.join(",", which, from, file.getAbsolutePath(), "" + i, "" + all, "" + size);
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));    
			        out.write(msg + Config.endLine);  
			        out.flush();
			        //2.write bytes
			        WritableByteChannel outChannel = Channels.newChannel(os);
			        FileInputStream fis = new FileInputStream(file);
			        FileChannel srcChannel= fis.getChannel();
			        while (srcChannel.read(buffer) != -1)
			        {
			            buffer.flip();
			            while (buffer.hasRemaining())
			            {
			            	outChannel.write(buffer);
			            }
			            buffer.clear();
			        }
			        os.flush();
			        fis.close();
			        srcChannel.close();
			        long time = System.currentTimeMillis() - start;
			        logger.info("[SSLSocket] " + msg + " COST: " + time + "ms");
			        notebook.put(key, ""+lastModified);
				}
				logger.info("[SSLSocket] " + mustdo + " " + key + " finished");
			}
		}
	}
	
	public static void main(String[] args) {
		File file = new File("/Users/lijiaming/Codes/SuppressWarnings/tmp/leveldb/000034.sst");
		System.out.println(file.length()/1024);
		boolean big = file.length() > 100000000;
		System.out.println(big);
	}
}
