package com.suppresswarnings.things;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCodeUtil {
	public static void showQRCodeAsJFrame(String remoteQRCodeURL) {
		try {
			showQRCodeAsFile(remoteQRCodeURL);
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

	public static void showQRCodeAsFile(String remoteQRCodeURL) {
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
	
	public static void showQRCodeOnScreen(String remoteQRCodeURL) {
		try {
			InputStream inputStream = new URL(remoteQRCodeURL).openStream();
            BufferedImage image = ImageIO.read(inputStream);
            int w = image.getWidth();
            int h = image.getWidth();
            int[] pixels = new int[w * h];
            image.getRGB(0, 0, w, h, pixels, 0, w);
            LuminanceSource source = new RGBLuminanceSource(w, h, pixels);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Map<DecodeHintType,Object> hints = new LinkedHashMap<DecodeHintType,Object>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
            com.google.zxing.Result result = new MultiFormatReader().decode(bitmap, hints);
            String text = result.getText();
            
            int width = 40;
            int height = 40;
            // 用于设置QR二维码参数
            Hashtable<EncodeHintType, Object> qrParam = new Hashtable<EncodeHintType, Object>();
            // 设置QR二维码的纠错级别——这里选择最低L级别
            qrParam.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            qrParam.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, qrParam);
            StringBuilder sb = new StringBuilder();
            for (int rows = 0; rows < bitMatrix.getHeight(); rows++) {
                for (int cols = 0; cols < bitMatrix.getWidth(); cols++) {
                    boolean x = bitMatrix.get(rows, cols);
                    if (!x) {
                        // white
                        sb.append("\033[47m  \033[0m");
                    } else {
                        sb.append("\033[40m  \033[0m");
                    }
                }
                sb.append("\n");
            }
            System.out.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
