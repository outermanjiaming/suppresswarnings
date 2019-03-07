package com.suppresswarnings.android;

import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;

public class CMDService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		
		return null;
	}
	AtomicBoolean on = new AtomicBoolean(false);
	String[] commands = {
    		"input keyevent 3", 
    		"input keyevent 4",
    		"am start -n cn.weli.story/cn.etouch.ecalendar.MainActivity",
    		"input keyevent 4",
    		"input touchscreen swipe 400 400 400 1290 1000",
    		"input touchscreen swipe 400 790 400 530 1000",
    		"input tap 400 400",
    		"input touchscreen swipe 400 1290 400 400 1000",
    		"input touchscreen swipe 400 400 400 1290 1000",
    		"input touchscreen swipe 400 1290 400 400 1000",
    		"input touchscreen swipe 400 400 400 1290 1000",
    		"input touchscreen swipe 400 1290 400 400 1000",
    		"input touchscreen swipe 400 400 400 1290 1000",
    		"input touchscreen swipe 400 1290 400 400 1000",
    		"input touchscreen swipe 400 400 400 1290 1000",
    		"input keyevent 4"
    };
	boolean started = false;
	TimerTask task;
	Timer timer;
	AtomicReference<String[]> acommands = new AtomicReference<String[]>();
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String cmds = intent.getStringExtra("commands");
		if(cmds != null && cmds.length() > 2) {
			acommands.set(cmds.split("\n"));
		} else {
			acommands.set(commands);
		}
		if(!started) {
			started = true;
			timer.schedule(task, 10);
		} else {
			started = false;
		}
		
		return super.onStartCommand(intent, flags, startId);
	}

	public boolean openActivity(String who, String where) {
    	try {
    		Intent intent = new Intent(Intent.ACTION_MAIN);
			ComponentName cmp = new ComponentName(who, where);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setComponent(cmp);
			startActivity(intent);
			return true;
    	} catch(Exception e) {
    		return false;
    	}
    }

	@Override
	public void onCreate() {
		super.onCreate();
		acommands.set(commands);
		timer  = new Timer();
		task = new TimerTask() {

			@Override
			public void run() {
				try {
					
					TimeUnit unit = TimeUnit.SECONDS;
					while(started) {
						OutputStream os = Runtime.getRuntime().exec("su").getOutputStream();
						String[] cmds = acommands.get();
						for(String cmd: cmds) {
			        		unit.sleep(1);
			        		if(cmd.toLowerCase().contains("sleep")) {
			        			unit.sleep(1);
			        			continue;
			        		}
							os.write((cmd+"\n").getBytes());
			        		os.flush();
			        		unit.sleep(1);
						}
						os.write(("exit\n").getBytes());
		        		os.flush();
		        		try {
			                if (os != null) {
			                    os.close();
			                }
			            } catch (Exception e) {
			            }
		        		Intent intent = new Intent(CMDService.this, MainActivity.class);
		        	    startActivity(intent);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

}
