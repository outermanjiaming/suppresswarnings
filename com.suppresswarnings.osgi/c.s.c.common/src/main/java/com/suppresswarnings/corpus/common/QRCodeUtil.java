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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCodeUtil {
	public static boolean createQrCode(OutputStream outputStream, String content, int qrCodeSize, String imageFormat) {
		try {
			Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
			hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);
			int matrixWidth = byteMatrix.getWidth();
			BufferedImage image = new BufferedImage(matrixWidth - 2, matrixWidth - 2, BufferedImage.TYPE_INT_RGB);
			image.createGraphics();
			Graphics2D graphics = (Graphics2D) image.getGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, matrixWidth, matrixWidth);
			graphics.setColor(Color.BLACK);
			for (int i = 0; i < matrixWidth; i++) {
				for (int j = 0; j < matrixWidth; j++) {
					if (byteMatrix.get(i, j)) {
						graphics.fillRect(i - 1, j - 1, 1, 1);
					}
				}
			}
			return ImageIO.write(image, imageFormat, outputStream);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static String readQrCode(InputStream inputStream) {
		try {
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
			Result result = new MultiFormatReader().decode(bitmap, hints);
			return result.getText();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) throws Exception {
//		createQrCode(new FileOutputStream(new File("/Users/lijiaming/Downloads/showqrcode10010.jpg")), "http://weixin.qq.com/q/020-m2xCeFdcl15sUUhr1y", 1, "JPEG");
		String text = readQrCode(new FileInputStream(new File("/Users/lijiaming/code/suppresswarnings/com.suppresswarnings.osgi/com.suppresswarnings.things/trafficled.jpeg")));
		System.out.println(text);
	}

}