package com.suppresswarnings.things.qr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import com.suppresswarnings.things.Things;

public class FileQR {

	public static void show(String remoteQRCodeURL, String text) {
		try {
			InputStream in = new URL(remoteQRCodeURL).openStream();
			File file = new File(Things.Const.QRCODE_FILE);
			if(file.exists()) file.delete();
			file.createNewFile();
			ReadableByteChannel ch = Channels.newChannel(in);
			FileOutputStream fos = new FileOutputStream(file);
			FileChannel fch = fos.getChannel();
			fch.transferFrom(ch, 0, 50000);
			fos.close();
			System.out.println("微信扫一扫二维码控制该程序\n" + file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
