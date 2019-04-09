/**
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
	            connection.setConnectTimeout(1500);
	            connection.setReadTimeout(3000);
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
			String debug = System.getProperty("debug");
			if("true".equals(debug)) System.setProperty("javax.net.debug", "ssl,handshake");
			Config prepare = new UnsafeClassLoader(Thread.currentThread().getContextClassLoader()).load(Config.code(), "prepare");
			this.sslsocket = prepare.factory().createSocket(Things.Const.HOST, Things.Const.PORT);
			String knock = String.join(",", things.description(), code, String.join(";", commands)) + "\n";
			System.out.println("knock ======== " + knock);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sslsocket.getOutputStream(), Things.Const.UTF8));    
			out.write(knock);
			out.flush();
			InputStream is = sslsocket.getInputStream();
			System.out.println("Connected!");
			
			service.scheduleWithFixedDelay(() -> {
				try {
					String ret = doGet(String.format(Things.Const.PING_FORAMT, code));
					System.out.println(start + " ping = " + ret);
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
			}, 1, 3, TimeUnit.SECONDS);
			
		    try {
		    	while(run.get() && sslsocket.isConnected() && !sslsocket.isClosed()) {
					BufferedReader in = new BufferedReader(new InputStreamReader(is, Things.Const.UTF8));
					String msg = in.readLine();
					System.out.println("msg ========= " + msg);
					if(msg == null) {
						run.set(false);
						continue;
					}
					String command = msg.trim();
					String[] callInput = command.split(";");
					if(callInput.length == 2) {
						String call = callInput[0];
						String input = callInput[1];
						String ret = execute(call, input);
						System.out.println("ret ========= " + ret);
						out.write(ret + "\n");
						out.flush();
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
