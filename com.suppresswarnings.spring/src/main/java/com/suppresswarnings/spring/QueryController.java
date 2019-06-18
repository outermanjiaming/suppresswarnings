package com.suppresswarnings.spring;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;

import com.tencentcloudapi.aai.v20180522.AaiClient;

import com.tencentcloudapi.aai.v20180522.models.ChatRequest;
import com.tencentcloudapi.aai.v20180522.models.ChatResponse;


@Controller
public class QueryController {
	
	@Autowired Environment ev;
	long minutes = TimeUnit.MINUTES.toMillis(2);
	long update = System.currentTimeMillis();
	
	ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
	
	ConcurrentHashMap<String, File> files = new ConcurrentHashMap<>();
	//TODO the id & key was updated
	Credential cred = new Credential("KIDA72i8JGoReHomeHfmeGgqlRIuJUQAhwxi", "f5FAtNightGuJOKExOZUVGn7CetsbjcJlx");
    HttpProfile httpProfile = new HttpProfile();
    ClientProfile clientProfile = new ClientProfile();
    AaiClient client = null;
	
	
	@GetMapping("/index")
    public String index() {
		System.out.println("/index -> index.html");
		System.out.println(ev.getProperty("server.port", "8009"));
		
		try{
			httpProfile.setEndpoint("aai.tencentcloudapi.com");
			clientProfile.setHttpProfile(httpProfile);
			client = new AaiClient(cred, "ap-beijing", clientProfile);
            
            String params = "{\"Text\":\"吃饭了吗\",\"User\":\"{\\\"id\\\":\\\"10010\\\",\\\"gender\\\":\\\"0\\\"}\",\"ProjectId\":1255895122}";
            ChatRequest req = ChatRequest.fromJsonString(params, ChatRequest.class);
            ChatResponse resp = client.Chat(req);
            
            System.out.println(resp.getAnswer());
            System.setOut(new PrintStream("/Users/lijiaming/qa_lijiaming_"+System.currentTimeMillis()+".log"));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
		
        return "index.html";
    }
	
	@RequestMapping("chat")
    @ResponseBody
    public String chat(@RequestParam("input") String input){
		
		try {
			ChatRequest req = new ChatRequest();
			req.setUser("{\"id\":\"test\",\"gender\":\"male\"}");
			req.setText(input);
			req.setProjectId(1255895122);
			ChatResponse resp = client.Chat(req);
			String reply = resp.getAnswer();
			System.out.println(input);
			System.out.println(reply);
			System.out.println();
			return reply;
		} catch (Exception e) {
			e.printStackTrace();
			return "无语😓";
		}
		
	}
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
        File dest = uploadFile(fileName, email);
        if(!dest.getParentFile().exists()){
            dest.getParentFile().mkdir();
        }
        try {
            file.transferTo(dest); 
            System.out.println(dest.getAbsolutePath());
            
            return "true == " + dest.exists();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return "false";
        } catch (IOException e) {
            e.printStackTrace();
            return "false";
        }
    }
    
    public File uploadFile(String filename, String email) {
    	long time = System.currentTimeMillis();
    	Random rand = new Random();
    	SimpleDateFormat folder = new SimpleDateFormat("yyyy-MM-dd");
    	SimpleDateFormat name = new SimpleDateFormat("HH-mm-ss");
    	String path = ev.getProperty("upload.path", "/Users/lijiaming/worldpop") ;
    	File upload = new File(path, "upload/" + folder.format(time) + "/" + name.format(time) + "#" + email + "#" + filename);
    	checkFolder(upload);
    	if(upload.exists()) {
    		try {
				TimeUnit.MILLISECONDS.sleep(rand.nextInt(3000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		uploadFile(filename, email);
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
    
    public void downloaded(File downloaded, String msg) {
    	try {
    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(downloaded),"UTF-8"));    
            out.write(msg); 
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    }
    private void checkFolder(File file) {
    	if(file.exists()) return;
    	if(file.getParentFile().exists()) return;
    	file.getParentFile().mkdirs();
    }
    public File downloadFile(File file, String email) {
    	long time = System.currentTimeMillis();
    	Random rand = new Random();
    	SimpleDateFormat folder = new SimpleDateFormat("yyyy-MM-dd");
    	SimpleDateFormat name = new SimpleDateFormat("HH-mm-ss");
    	String path = ev.getProperty("upload.path", "/Users/lijiaming/worldpop") ;
    	File downloaded = new File(path, "download/" + folder.format(time) + "/" + name.format(time) + "#" + email + "#" + file.getName());
    	checkFolder(downloaded);
    	if(downloaded.exists()) {
    		try {
				TimeUnit.MILLISECONDS.sleep(rand.nextInt(3000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		return downloadFile(file, email);
    	} else {
    		try {
    			downloaded.createNewFile();
				return downloaded;
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	return null;
    }
    
    @RequestMapping("download")
    public String downLoad(HttpServletResponse response, @RequestParam("index") Integer index,  @RequestParam("email") String email){
        File file = files.get(""+index);
        String filename = file.getName();
        if(email== null || email.length() < 1) return "false";
        System.out.println(email);
        if(file.exists()){
        	
        	File downloaded = downloadFile(file, email);
            response.setContentType("application/force-download");
            response.setHeader("Content-Disposition", "attachment;fileName=" + filename);
            try {
				OutputStream os = response.getOutputStream();
				WritableByteChannel outChannel = Channels.newChannel(os);
				FileInputStream fis = new FileInputStream(file);
				FileChannel srcChannel= fis.getChannel();
				while (srcChannel.read(buffer) != -1)
				{
				    buffer.flip();
				    while (buffer.hasRemaining())
				    {
				    	outChannel.write(buffer);
				    }
				    buffer.clear();
				}
				os.flush();
				
				downloaded(downloaded, email);
				
				fis.close();
				srcChannel.close();
				return "true";
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            System.out.println("----------file download " + filename);
            
        }
        System.out.println("File not exist! " + filename);
        return "false";
    }
    
    private void list(){
    	String path = ev.getProperty("upload.path", "/Users/lijiaming/worldpop") ;
    	File file = new File(path + "/" + "files");
    	File[] fs = file.listFiles((f,n) -> !n.endsWith(".desc"));
    	int i=0; 
    	System.out.println("list file of " + file.getAbsolutePath());
    	for(File f : fs) {
    		files.put(""+i, f);
    		i++;
    	}
    }
    
    @RequestMapping("listFiles")
    @ResponseBody
    public List<Map<String, String>> listFiles(){
    	if(files.isEmpty() || (System.currentTimeMillis() - update > minutes)) {
    		update = System.currentTimeMillis();
    		String path = ev.getProperty("upload.path", "/Users/lijiaming/worldpop") ;
        	File upload = new File(path, "/upload");
        	upload.mkdirs();
        	File download = new File(path, "/download");
        	download.mkdirs();
    		list();
    	}
    	List<Map<String, String>> result = new ArrayList<>();
    	files.forEach((index, file) ->{
    		Map<String, String> map = new HashMap<>();
    		map.put("name", file.getName());
			try {
				List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath() + ".desc"));
				map.put("desc", String.join("<br/>", lines));
			} catch (Exception e) {
				e.printStackTrace();
				map.put("desc", "Download it and see the details");
			}
			result.add(map);
    	});
    	
    	String path = ev.getProperty("upload.path", "/Users/lijiaming/worldpop") ;
    	File terms = new File(path, "/terms");
    	File[] tfs = terms.listFiles();
    	if(tfs != null && tfs.length > 0) {
    		File file = tfs[0];
    		Map<String, String> map = new HashMap<>();
    		map.put("name", file.getName());
			try {
				List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));
				map.put("desc", String.join("<br/>", lines));
			} catch (Exception e) {
				e.printStackTrace();
				map.put("desc", "Please see worldpop.co.nz/termsConditions.php");
			}
			result.add(map);
    	}
    	
    	return result;
    }
    
    
}
