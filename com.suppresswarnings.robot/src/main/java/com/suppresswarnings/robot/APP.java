package com.suppresswarnings.robot;

import java.io.File;
import java.io.IOException;

import com.suppresswarnings.corpus.common.Speaker;
import com.suppresswarnings.things.SuppressWarnings;
import com.suppresswarnings.things.Things;
import com.suppresswarnings.things.ThingsManager;

@SuppressWarnings("Connect with others, speak words from server")
public class APP implements Things {
	String format = "/Users/lijiaming/listen/%s.mp3";
	String mp3Format = "/Users/lijiaming/mp3/%s.mp3";
	
	@SuppressWarnings("exam")
	public String exam(String quiz) {
		File file = new File(String.format(format, quiz));
		String mp3 = String.format(mp3Format, quiz);
		System.out.println(file.getAbsolutePath());
		File mp3File = new File(mp3);
		if(mp3File.exists()) {
			Speaker.speakMp3(mp3);
		} else {
			try {
				if(file.exists()) file.delete();
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return SUCCESS;
	}
	
	public static void main(String[] args) throws IOException {
		ThingsManager.connect(new APP());
	}

	@Override
	public String description() {
		return "Connect with others and speaking words";
	}

	@Override
	public String code() {
		return "Robot_AIIoT_0001";
	}

	@Override
	public String exception(String error) {
		System.err.println(error);
		return FAIL;
	}
}