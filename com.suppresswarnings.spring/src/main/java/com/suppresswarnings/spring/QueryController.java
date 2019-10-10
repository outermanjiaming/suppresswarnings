package com.suppresswarnings.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.concurrent.TimeUnit;


@Controller
public class QueryController {
	
	@Autowired Environment ev;
	long minutes = TimeUnit.MINUTES.toMillis(2);
	long update = System.currentTimeMillis();
	
	ByteBuffer buffer = ByteBuffer.allocateDirect(2048);

	/**
     * 实现文件上传
     * */
    @RequestMapping("fileUpload")
    @ResponseBody 
    public String fileUpload(@RequestParam("fileName") MultipartFile file,  @RequestParam("email") String email){
        if(file.isEmpty()){
            return "false";
        }
        String fileName = System.currentTimeMillis() + "." + file.getOriginalFilename();
        int size = (int) file.getSize();
        System.out.println(fileName + "-->" + size);
		String path = ev.getProperty("upload.path", "/Users/lijiaming/worldpop");
        File dest = uploadFile(path, fileName, email);
        if(!dest.getParentFile().exists()){
            dest.getParentFile().mkdir();
        }
        try {
            file.transferTo(dest);
            String input = dest.getAbsolutePath();
            if("transform".equals(email)) {
				System.out.println("transform:" + input);
				String transform = writepng(input);
            	return transform.substring(path.length());
			}
            return input.substring(path.length());
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return "false";
        } catch (IOException e) {
            e.printStackTrace();
            return "false";
        }
    }
    
    public File uploadFile(String path, String filename, String email) {
    	long time = System.currentTimeMillis();
    	Random rand = new Random();
    	SimpleDateFormat folder = new SimpleDateFormat("yyyyMMdd");
    	SimpleDateFormat name = new SimpleDateFormat("HHmmss");

    	File upload = new File(path, "upload/" + folder.format(time) + "/" + name.format(time) + "." + email + "." + filename);
    	checkFolder(upload);
    	if(upload.exists()) {
    		try {
				TimeUnit.MILLISECONDS.sleep(rand.nextInt(300));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		uploadFile(path, filename, email);
    	} else {
    		try {
    			upload.createNewFile();
				return upload;
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	return null;
    }


    private void checkFolder(File file) {
    	if(file.exists()) return;
    	if(file.getParentFile().exists()) return;
    	file.getParentFile().mkdirs();
    }

	public static String byteToHex(byte[] bytes){
		String strHex = "";
		StringBuilder sb = new StringBuilder("");
		for (int n = 0; n < bytes.length; n++) {
			strHex = Integer.toHexString(bytes[n] & 0xFF);
			sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
		}
		return sb.toString().trim();
	}

    private String writepng(String input) throws IOException {
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
		String path = input+ ".png";
		BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
		index = 0;
		for(int i=0;i<bufferedImage.getHeight();i++) {
			for(int j=0;j<bufferedImage.getWidth();j++) {
				Color c = new Color(data[index],data[index+1],data[index+2],data[index+3]);
				bufferedImage.setRGB(j, i, c.getRGB());
				index += 4;
			}
		}
		File file = new File(path);
		ImageIO.write(bufferedImage, "png",  file);
		return path;
	}
}
