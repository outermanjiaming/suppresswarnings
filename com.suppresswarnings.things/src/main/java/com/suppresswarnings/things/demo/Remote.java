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
   
   
 */
package com.suppresswarnings.things.demo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.suppresswarnings.things.SuppressWarnings;
import com.suppresswarnings.things.Things;
import com.suppresswarnings.things.ThingsManager;

public class Remote implements Things {

	@SuppressWarnings("打开记事本")
	public String openTxt(String input) {
		try {
			File tmp = new File("/Users/lijiaming/tmp.txt");
			Files.write(Paths.get(tmp.getAbsolutePath()), Arrays.asList("亲爱的", "我爱你"));
	        Runtime.getRuntime().exec("open " + tmp.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
			return ERROR;
		}
		
		return SUCCESS;
	}
	
	@SuppressWarnings("说我爱你")
	public String love(String input) {
		try {
			JFrame frame = new JFrame("新消息");
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setSize(215, 160);
			frame.setLocation(400, 300);
			JPanel jp1 = new JPanel();
			JPanel jp2 = new JPanel();
			jp2.setLayout(new FlowLayout());
			jp1.add(new JButton("<html>=================<br/><br/>====== 我爱你 ======<br/><br/>=================</html>"));
			jp2.add(new JTextField(15));
			jp2.add(new JButton("发送"));

			frame.setLayout(new BorderLayout());
			frame.add(jp1, BorderLayout.NORTH);
			frame.add(jp2, BorderLayout.SOUTH);
			frame.pack();
			frame.setVisible(true);
	        
		} catch (Exception e) {
			e.printStackTrace();
			return ERROR;
		}
		
		return SUCCESS;
	}
	
	@Override
	public String description() {
		return "遥控电脑";
	}

	public static void main(String[] args) {
		Remote object = new Remote();
		ThingsManager.connect(object);
	}

	@Override
	public String exception(String error) {
		return "";
	}
}
