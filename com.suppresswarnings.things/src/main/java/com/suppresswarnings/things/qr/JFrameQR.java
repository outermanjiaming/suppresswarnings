package com.suppresswarnings.things.qr;

import java.awt.Color;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.suppresswarnings.things.Things;

public class JFrameQR {
	public static void show(String remoteQRCodeURL, String text) {
		try {
			FileQR.show(remoteQRCodeURL, text);
			File file = new File(Things.Const.QRCODE_FILE);
			JFrame frame = new JFrame("微信扫一扫二维码控制该程序");
			int width = 430, height = 430; 
			frame.setBackground(Color.PINK);
			frame.setSize(width, height);
			frame.setLocation(30, 30);
			ImageIcon imageCode = new ImageIcon(file.getAbsolutePath());
			JLabel qrCode = new JLabel(imageCode);
			qrCode.setSize(width, height);
			frame.add(qrCode);
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
