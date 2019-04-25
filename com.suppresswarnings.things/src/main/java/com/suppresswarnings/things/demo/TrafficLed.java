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
   
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.things.demo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import com.suppresswarnings.things.SuppressWarnings;
import com.suppresswarnings.things.Things;
import com.suppresswarnings.things.ThingsManager;
import com.suppresswarnings.things.qr.ScreenQR;

public class TrafficLed implements Things {

	public TrafficLed() {
		try {
			prepare();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("红灯亮")
	public String red(String input) {
		try {
			int ret = exec("1", "/sys/class/gpio/gpio12/value");
			ret |= exec("0", "/sys/class/gpio/gpio16/value");
			if (ret == 0)
				return "on";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "off";
	}

	@SuppressWarnings("绿灯亮")
	public String green(String input) {
		try {
			int ret = exec("1", "/sys/class/gpio/gpio16/value");
			ret |= exec("0", "/sys/class/gpio/gpio12/value");
			if (ret == 0)
				return "on";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "off";
	}
	
	@SuppressWarnings("黄灯亮")
	public String yellow(String input) {
		try {
			int ret = exec("1", "/sys/class/gpio/gpio16/value");
			ret |= exec("1", "/sys/class/gpio/gpio12/value");
			if (ret == 0)
				return "on";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "off";
	}
	
	@SuppressWarnings("关灯")
	public String off(String input) {
		try {
			int ret = exec("0", "/sys/class/gpio/gpio12/value");
			ret |= exec("0", "/sys/class/gpio/gpio16/value");
			if (ret == 0)
				return "off";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "on";
	}

	public int exec(String state, String light) throws Exception {
		System.out.println("==================== try my method =================");
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(light))));
		writer.write(state);
		writer.flush();
		writer.close();
		System.out.println("====================================================");
		return 0;
	}
	
	public void write(String filename, String value) throws Exception{
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filename))));
		writer.write(value);
		writer.flush();
		writer.close();
	}
	
	public void prepare() throws Exception{
		File redlight = new File("/sys/class/gpio/gpio12/value");
		if(!redlight.exists()) {
			write("/sys/class/gpio/export", "12");
			write("/sys/class/gpio/gpio12/direction", "out");
		}
		
		File greenlight = new File("/sys/class/gpio/gpio16/value");
		if(!greenlight.exists()) {
			write("/sys/class/gpio/export", "16");
			write("/sys/class/gpio/gpio16/direction", "out");
		}
	}

	@Override
	public String description() {
		return "树莓派红绿灯";
	}
	
	@Override
	public void showQRCode(String remoteQRCodeURL, String text) {
		ScreenQR.show(remoteQRCodeURL, text);
	}

	public static void main(String[] args) throws Exception {
		ThingsManager.connect(new TrafficLed());
	}
	@Override
	public String exception(String error) {
		return "";
	}
	
}