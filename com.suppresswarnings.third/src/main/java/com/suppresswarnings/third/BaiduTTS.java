package com.suppresswarnings.third;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.suppresswarnings.corpus.common.Baidu;
import com.suppresswarnings.corpus.common.Speaker;

public class BaiduTTS {

	public static void addListener(String path) throws Exception {

        Baidu baidu = new Baidu();
        WatchService  service = FileSystems.getDefault().newWatchService();
        
        Paths.get(path).register(service, StandardWatchEventKinds.ENTRY_CREATE);
        
        ExecutorService fixedThreadPool = Executors.newCachedThreadPool();
        
        fixedThreadPool.execute(() -> {
        	try {
                while(true){
                    WatchKey watchKey = service.take();
                    List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                    for(WatchEvent<?> event : watchEvents){
                    	try {
                    		String filename = event.context().toString();
                        	if(!filename.endsWith(".mp3")) continue;
                            if(StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
                            	System.out.println("baidu tts " + event.context());
                            	String words = filename.substring(0, filename.length() - 4);
                            	String mp3 = baidu.speak(words);
                            	Speaker.speakMp3(mp3);
                            }
						} catch (Exception e) {
							System.out.println("异常：" + e.getMessage());
						}
                    	
                    }
                    watchKey.reset();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    service.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    

    public static void main(String[] args) throws Exception {
    	BaiduTTS.addListener("/Users/lijiaming/listen");
    }
}