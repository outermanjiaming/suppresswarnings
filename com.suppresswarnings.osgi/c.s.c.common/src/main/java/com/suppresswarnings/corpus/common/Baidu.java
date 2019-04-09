/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.common;

import java.io.File;

import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import com.baidu.aip.util.Util;

public class Baidu {
	public org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	AipSpeech client;
	public Baidu(){
		client = new AipSpeech(APP_ID, API_KEY, SECRET_KEY);
		logger.info("baidu client: " + APP_ID + ", " + API_KEY + ", " + SECRET_KEY);
	    System.out.println("baidu client: " + APP_ID + ", " + API_KEY + ", " + SECRET_KEY);
		client.setConnectionTimeoutInMillis(2000);
	    client.setSocketTimeoutInMillis(60000);
	}
	public static final String APP_ID = System.getProperty("baidu.app.id");
    public static final String API_KEY = System.getProperty("baidu.api.key");
    public static final String SECRET_KEY = System.getProperty("baidu.secret.key");
    public static String STORE_PATH = System.getProperty("mp3.store.path");
	public static void main(String[] args) {
		Baidu baidu = new Baidu();
		baidu.speak(args[0]);
	}
	
	public String listen(byte[] data) {
		try {
			JSONObject res = client.asr(data, "pcm", 16000, null);
			if(res.getInt("err_no") == 0) {
				return (String) res.getJSONArray("result").get(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	public String speak(String words) {
		if(STORE_PATH == null) {
			logger.info("-Dmp3.store.path is not set");
			STORE_PATH = "./";
		}
		File mp3 = new File(STORE_PATH, words + ".mp3");
		String path = mp3.getAbsolutePath();
		if(mp3.exists()) {
			logger.info("Already exists");
			return path;
		}
		
        TtsResponse res = client.synthesis(words, "zh", 1, null);
        logger.info("call baidu...");
        byte[] data = res.getData();
        logger.info("call baidu data " + data);
        if (data != null) {
            try {
                Util.writeBytesToFileSystem(data, path);
                return path;
            } catch (Exception e) {
                logger.error("fail to call baidu", e);
            }
        }
        return "null";
	}

}
