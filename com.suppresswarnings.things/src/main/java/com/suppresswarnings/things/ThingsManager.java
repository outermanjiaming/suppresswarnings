/**
 * 
 * Copyright 2019 SuppressWarnings.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 *       素朴网联
 *       
 *       #  #  $
 *       #     #
 *       #  #  #
 * 
 *   SuppressWarnings
 * 
 */
package com.suppresswarnings.things;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.suppresswarnings.things.security.UnsafeClassLoader;

@SuppressWarnings("物联网的总入口，通过ThingsManager.connect(Things)进行连接。")
public class ThingsManager {
	static final AtomicBoolean run = new AtomicBoolean(true);
	static class Executor {
		long start = System.currentTimeMillis();
		Map<String, Method> cmds;
		Things things;
		Socket sslsocket;
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		String doGet(String httpurl) {
	        HttpURLConnection connection = null;
	        InputStream is = null;
	        BufferedReader br = null;
	        String result = null;
	        try {
	            URL url = new URL(httpurl);
	            connection = (HttpURLConnection) url.openConnection();
	            connection.setRequestMethod("GET");
	            connection.setConnectTimeout(30000);
	            connection.setReadTimeout(30000);
	            connection.connect();
	            if (connection.getResponseCode() == 200) {
	                is = connection.getInputStream();
	                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	                StringBuffer sbf = new StringBuffer();
	                String temp = null;
	                while ((temp = br.readLine()) != null) {
	                    sbf.append(temp);
	                    sbf.append("\r\n");
	                }
	                result = sbf.toString();
	            }
	        } catch (MalformedURLException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (null != br) {
	                try {
	                    br.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }

	            if (null != is) {
	                try {
	                    is.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }

	            connection.disconnect();
	        }

	        return result;
	    }
		
		String execute(String call, String msg) {
			Method method = cmds.get(call);
			
			if(method == null) {
				return "notfound";
			} else {
				try {
					Object object = method.invoke(things, msg);
					return String.valueOf(object);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			return "error";
		}
		
		void start(Things things) throws Exception {
			this.things = things;
			this.cmds = new HashMap<>();
			Method[] methods = things.getClass().getDeclaredMethods();
			List<String> commands = new ArrayList<>();
			for(Method method : methods) {
				SuppressWarnings cmd = method.getDeclaredAnnotation(SuppressWarnings.class);
				if(cmd == null) continue;
				String command = cmd.value();
				commands.add(command);
				cmds.put(command, method);
			}
			
			String code = System.getProperty(Things.Config.CODE, things.code()); 
			String debug = System.getProperty(Things.Const.DEBUG_SWITCH);
			if("true".equals(debug)) System.setProperty("javax.net.debug", "ssl,handshake");
			Config prepare = new UnsafeClassLoader(Thread.currentThread().getContextClassLoader()).load(Config.code(), "prepare");
			this.sslsocket = prepare.factory().createSocket(Things.Const.HOST, Things.Const.PORT);
			String knock = String.join(",", things.description(), code, String.join(";", commands)) + "\n";
			if("true".equals(debug)) System.out.println("knock at " + knock);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sslsocket.getOutputStream(), Things.Const.UTF8));    
			out.write(knock);
			out.flush();
			InputStream is = sslsocket.getInputStream();
			System.out.println("Connected!");
			
			service.scheduleWithFixedDelay(() -> {
				try {
					String ret = doGet(String.format(Things.Const.PING_FORAMT, code));
					if("true".equals(debug)) System.out.println(start + " -> " + System.currentTimeMillis() + " ping = " + ret);
					if(ret == null || "closed".equals(ret.trim())) {
						System.out.println("restart");
						things.exception("restart");
						service.shutdown();
						System.exit(-1);
					}
				} catch (Exception e) {
					e.printStackTrace();
					things.exception("ping = " + e.getMessage());
				}
			}, 5, 15, TimeUnit.SECONDS);
			
		    try {
		    	while(run.get() && sslsocket.isConnected() && !sslsocket.isClosed()) {
					BufferedReader in = new BufferedReader(new InputStreamReader(is, Things.Const.UTF8));
					String msg = in.readLine();
					if("true".equals(debug)) System.out.println("msg read " + msg);
					if(msg == null) {
						run.set(false);
						continue;
					}
					String command = msg.trim();
					String[] callInput = command.split(";");
					if(callInput.length == 2) {
						String call = callInput[0];
						String input = callInput[1];
						if(Things.Const.SHOW_QRCODE.equals(call)) {
							System.out.println("微信扫一扫二维码控制该程序");
							String[] url_text = input.split("#");
							if(url_text.length < 2) url_text = new String[] {input, input};
							things.showQRCode(url_text[0], url_text[1]);
							out.write("ok\n");
							out.flush();
						} else if(Things.Const.PING_PONG.equals(call)) {
							out.write("pong\n");
							out.flush();
						} else {
							String ret = execute(call, input);
							if("true".equals(debug)) System.out.println("return " + ret);
							out.write(ret + "\n");
							out.flush();
						}
					} else {
						execute(callInput[0], "我什么也没有说");
						out.write("WRONG_FORMAT\n");
						out.flush();
					}
		    	}
			} catch (Exception e) {
				run.set(false);
				out.close();
				is.close();
				sslsocket.close();
				e.printStackTrace();
				things.exception("网络异常，请重启！" + e.getMessage());
				System.out.println(e.getMessage() + "网络异常，请重启！");
			}
			service.shutdown();
			System.out.println("Executor stops");
		}
	}
	
	public static void connect(Things things) {
		String error = "";
		try {
			Executor executor = new Executor();
			executor.start(things);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			error += e.getMessage();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			error += e.getMessage();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			error += e.getMessage();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			error += e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			error += e.getMessage();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			error += e.getMessage();
		} catch (InstantiationException e) {
			e.printStackTrace();
			error += e.getMessage();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			error += e.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
		}
		things.exception("Exit, " + error);
	}
}
