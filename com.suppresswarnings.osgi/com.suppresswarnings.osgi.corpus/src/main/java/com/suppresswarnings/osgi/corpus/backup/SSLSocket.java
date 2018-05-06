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
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.leveldb.LevelDB;

public class SSLSocket implements Runnable {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	Socket socket;
	String from;
	long capacity;
	WhichDB which;
	Status<SSLSocket> status;
	LevelDB notebook;
	String dbPath;
	public SSLSocket(LevelDB notebook, String dbPath, Socket client, String from, long capacity, WhichDB which){
		this.notebook = notebook;
		this.dbPath = dbPath;
		this.socket = client;
		this.from = from;
		this.capacity = capacity;
		this.which = which;
		this.status = new Status<SSLSocket>(this);
	}
	@Override
	public String toString() {
		return "SSLSocket [socket closed =" + socket.isClosed() + ", from=" + from + ", capacity=" + capacity + ", accept=" + which + ", status=" + status + "]";
	}

	@Override
	public void run() {
		int i = 0;
		int all = 0;
		boolean capable = capacity > 0;
		try {
			this.status.busy();
			//file transfer
			//1.list all file
			//2.for each check last modified decide whether to transfer or not
			// 3.output file name and length
			// 4.keep note of (from-which-filename)=lastmodified in leveldb when done
			logger.info("[SSLSocket] start transfer: " + dbPath);
			File dir = new File(dbPath);
			File[] files = dir.listFiles();
			ByteBuffer buffer = ByteBuffer.allocateDirect(Config.bufferSize);
			all = files.length;
			for(;i<all;i++) {
				if(socket.isClosed()) {
					logger.info("[SSLSocket] socket closed, so we stop here: " + i + " / " + all);
					return;
				}
				File file = files[i];
				String name = file.getName();
				long lastModified = file.lastModified();
				long size = file.length();
				capacity = capacity - size;
				capable = capacity > 0;
				logger.info("[SSLSocket] is agent capable ? " + from + " = " + capable);
				//get which, from, name
				String key = String.join(",", which.name(), from, name);
				String existLastModified = notebook.get(key);
				boolean mustdo = false;
				if(existLastModified == null) {
					mustdo = true;
					notebook.put(key, ""+lastModified);
				} else {
					long existModifiedTime = Long.parseLong(existLastModified);
					mustdo = (existModifiedTime < lastModified);
				}
				logger.info("[SSLSocket] " + key + " lastModified: " + lastModified + " == " + existLastModified + ", must do = " + mustdo);
				if(mustdo) {
					long start = System.currentTimeMillis();
					OutputStream os = socket.getOutputStream();
					//1.which, from, name, i, all, space, 
					String msg = String.join(",", which.name(), from, name, "" + i, "" + all, "" + size);
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));    
			        out.write(msg + "\n");  
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
			        fis.close();
			        srcChannel.close();
			        long time = System.currentTimeMillis() - start;
			        logger.info("[SSLSocket] " + msg + " COST: " + time + "ms == " + TimeUnit.MILLISECONDS.toSeconds(time) + "s");
				}
				logger.info("[SSLSocket] " + key + " finished");
			}
		} catch (Exception e) {
			logger.error("[SSLSocket] run with Exception(" + i + " / " + all + ")", e);
		} finally {
			try {
				socket.close();
			} catch (Exception e) {
				logger.info("[SSLSocket] fail to close socket");
			}
			this.status.rest();
		}
	}
	
	public static void main(String[] args) {
		File file = new File("/Users/lijiaming/Codes/SuppressWarnings/tmp/leveldb/000034.sst");
		System.out.println(file.length()/1024);
		boolean big = file.length() > 100000000;
		System.out.println(big);
	}
}
