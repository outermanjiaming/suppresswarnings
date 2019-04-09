/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.robot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Random;
import java.util.concurrent.Callable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.LoggerFactory;

public class CallableDownload implements Callable<File> {
	public static final long MB10 = 10 * 1024 * 1024;
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	Random random = new Random();
	
	String url;
	long maxSize;
	String fileName;
	String saveTo;
	/**
	 * .jpg
	 * .txt
	 * .mp4
	 */
	String type;
	boolean delete;
	long now;
	long ttl;
	long deleteAfter;
	int rand;

	public CallableDownload(String url, long maxSize, String saveTo, String filename) {
		this.url = url;
		this.maxSize = maxSize;
		this.saveTo = saveTo;
		this.delete = false;
		this.fileName = filename;
	}
	public CallableDownload(String url, long maxSize, String saveTo, String typeStartWithDot, boolean delete, long ttl) {
		this.url = url;
		this.maxSize = maxSize;
		this.saveTo = saveTo;
		this.type = typeStartWithDot;
		this.delete = delete;
		this.now = System.currentTimeMillis();
		this.ttl = ttl;
		this.delete = delete;
		this.rand = random.nextInt(100000);
		if(delete) {
			this.deleteAfter = now + ttl;
			this.fileName = "DFile" + "_" + rand + "_" + deleteAfter + type;
		} else {
			this.deleteAfter = -1;
			this.fileName = "PFile" + "_" + rand + "_" + now + type;
		}
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public long getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(long maxSize) {
		this.maxSize = maxSize;
	}
	public String getSaveTo() {
		return saveTo;
	}
	public void setSaveTo(String saveTo) {
		this.saveTo = saveTo;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isDelete() {
		return delete;
	}
	public void setDelete(boolean delete) {
		this.delete = delete;
	}
	public long getDeleteAfter() {
		return deleteAfter;
	}
	public void setDeleteAfter(long deleteAfter) {
		this.deleteAfter = deleteAfter;
	}
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public long getNow() {
		return now;
	}
	public void setNow(long now) {
		this.now = now;
	}
	public long getTtl() {
		return ttl;
	}
	public void setTtl(long ttl) {
		this.ttl = ttl;
	}
	public int getRand() {
		return rand;
	}
	public void setRand(int rand) {
		this.rand = rand;
	}
	@Override
	public File call() throws Exception {
		CloseableHttpClient httpClient = HttpClientHolder.getInstance().newHttpClient();
        HttpGet httpGet = new HttpGet(url);
        FileOutputStream out = null;
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if(httpResponse.getStatusLine().getStatusCode() == 200){
            	HttpEntity entity = httpResponse.getEntity();
            	long length = entity.getContentLength();
            	if(length > maxSize) {
            		logger.error("[Download] lijiaming: file is too large: " + length + " > " + maxSize);
            		return null;
            	}
            	if(length <= 0) {
            		logger.error("[Download] lijiaming: file is 0: " + url);
            		return null;
            	}
            	
            	File file = new File(saveTo, fileName);
            	if(!file.exists()){
            		file.createNewFile();
            	}
            	InputStream in = entity.getContent();
            	ReadableByteChannel read = Channels.newChannel(in);
            	ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
            	out = new FileOutputStream(file);
		        FileChannel write = out.getChannel();
		        while (read.read(buffer) != -1)
		        {
		            buffer.flip();
		            while (buffer.hasRemaining())
		            {
		            	write.write(buffer);
		            }
		            buffer.clear();
		        }
		        out.flush();
                logger.info("[Get] http result: " + length + " save to" + file.getAbsolutePath());
                return file;
            } else {
            	logger.error("[Get] fail to request " + url + ", response " + httpResponse.getStatusLine().toString());
            	throw new Exception("Fail to request get");
            }
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
            	
            }
            if(out != null) {
            	try {
					out.close();
				} catch (Exception e) {
				}
            }
        }
	}
	
}
