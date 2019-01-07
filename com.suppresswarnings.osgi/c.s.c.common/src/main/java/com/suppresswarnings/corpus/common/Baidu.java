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

import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import com.baidu.aip.util.Util;

public class Baidu {
	AipSpeech client;
	public Baidu(){
		client = new AipSpeech(APP_ID, API_KEY, SECRET_KEY);
	    client.setConnectionTimeoutInMillis(2000);
	    client.setSocketTimeoutInMillis(60000);
	}
	public static final String APP_ID = System.getProperty("baidu.app.id");
    public static final String API_KEY = System.getProperty("baidu.api.key");
    public static final String SECRET_KEY = System.getProperty("baidu.secret.key");
    public static final String STORE_PATH = System.getProperty("mp3.store.path");
	public static void main(String[] args) {
		Baidu baidu = new Baidu();
		baidu.speak("这是做什么的东西？");
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
	
	public void speak(String words) {
		if(STORE_PATH == null) {
			System.out.println("-Dmp3.store.path is not set");
			System.exit(0);
		}
        TtsResponse res = client.synthesis(words, "zh", 1, null);
        byte[] data = res.getData();
        if (data != null) {
            try {
                Util.writeBytesToFileSystem(data, new File(STORE_PATH, words + ".mp3").getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}

}
