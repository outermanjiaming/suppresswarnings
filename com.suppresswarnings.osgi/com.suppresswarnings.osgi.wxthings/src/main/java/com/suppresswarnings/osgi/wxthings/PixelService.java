package com.suppresswarnings.osgi.wxthings;

import javax.imageio.ImageIO; 
import javax.swing.ImageIcon;
 
import java.awt.*; 
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays; 

public class  PixelService { 
       
    public static void write(int[] data, int width, int height) throws Exception {
    	BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        int index = 0;
        for(int i=0;i<bufferedImage.getHeight();i++) {
    		for(int j=0;j<bufferedImage.getWidth();j++) {
    			Color c = new Color(data[index],data[index+1],data[index+2],data[index+3]);
    			bufferedImage.setRGB(j, i, c.getRGB());
    			index += 4;
    		}
    	}
    	File file = new File("/Users/lijiaming/company/mine/data"+System.currentTimeMillis()+".png");
        ImageIO.write(bufferedImage, "png",  file);
    }
    
    public static int[] read(String input) throws Exception {
    	
    	BufferedImage bufferedImage = ImageIO.read(new File(input));
    	int[] data = new int[bufferedImage.getWidth()*bufferedImage.getHeight()*4];
    	int index = 0;
    	System.out.print('[');
    	for(int i=0;i<bufferedImage.getWidth();i++) {
    		for(int j=0;j<bufferedImage.getHeight();j++) {
    			int pixel = bufferedImage.getRGB(i, j);
    			Color c = new Color(pixel, true);
    			data[index] = c.getRed();
    			data[index+1] = c.getGreen();
    			data[index+2] = c.getBlue();
    			data[index+3] = c.getAlpha();
    			index += 4;
    		}
    	}
    	System.out.println();
    	return data;
    }
    
    public static int[] hex2data(String input) throws Exception{
    	byte[] bytes = Files.readAllBytes(Paths.get(input));
    	char[] chars = new String(bytes).toCharArray();
    	int index = 0;
    	int w = Integer.parseInt(chars[index]+""+chars[index+1]+""+chars[index+2]+""+chars[index+3], 16);
    	index += 4;
    	int h = Integer.parseInt(chars[index]+""+chars[index+1]+""+chars[index+2]+""+chars[index+3], 16);
    	index += 4;
    	int size = (chars.length - 8) / 2;
    	int[] data = new int[size];
    	for(int i=index,pointer = 0;i<chars.length;i+=2,pointer++) {
    		int d = Integer.parseInt(chars[index]+""+chars[index+1], 16);
    		data[pointer] = d;
    	}
    	return data;
    }
    
    String hex = "01f401f4333234ff9e9e9e00363632ff333237ff7d7771ff3939390019151aff5c5e5fff4d4d4bffb19890ffc9cacdffd0faf7ff545655";
    
    public static String byteToHex(byte[] bytes){
        String strHex = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++) {
            strHex = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString().trim();
    }
    public static void main(String[] args) throws Exception { 
    	//write(data, 16, 16);
    	int x = -16777216;
    	System.out.println(Integer.toHexString(x));
    	System.out.println(Integer.toBinaryString(x));
    	System.out.println((x >> 24) & 0xff);
    	System.out.println(Integer.parseInt("9c", 16));
    	
    	String input = "/Users/lijiaming/moneytree/data3.hex";
    	byte[] bytes = Files.readAllBytes(Paths.get(input));
    	char[] chars = byteToHex(bytes).toCharArray();
    	int index = 0;
    	int w = Integer.parseInt(chars[index]+""+chars[index+1]+""+chars[index+2]+""+chars[index+3], 16);
    	index += 4;
    	int h = Integer.parseInt(chars[index]+""+chars[index+1]+""+chars[index+2]+""+chars[index+3], 16);
    	index += 4;
    	int size = (chars.length - 8) / 2;
    	int[] data = new int[size];
    	for(int i=index,pointer = 0;i<chars.length;i+=2,pointer++) {
    		int d = Integer.parseInt(chars[i]+""+chars[i+1], 16);
    		data[pointer] = d;
    	}
    	write(data, w, h);
    } 
}