package com.suppresswarnings.things.qr;

import java.util.Hashtable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class ScreenQR {
	
	public static void show(String remoteQRCodeURL, String text) {
		try {
            System.out.println(text);
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
