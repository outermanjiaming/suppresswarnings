package com.suppresswarnings.android;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class HTTPUtil {

	public static String checkValid(String mac, String token) throws Exception {
		URL url = new URL("http://suppresswarnings.com/wx.http?action=validate&identity="+mac+"&token="+token);
        URLConnection connection = url.openConnection();
        InputStream in = connection.getInputStream();
        InputStreamReader isr = new InputStreamReader(in,"utf-8");
        BufferedReader br = new BufferedReader(isr);
        String line;
        StringBuilder sb = new StringBuilder();
        while((line = br.readLine()) != null)
        {
            sb.append(line);
        }
        br.close();
        isr.close();
        in.close();
        return sb.toString();
	}
	
	public static String translate(String cmd) {
		String[] action_input = cmd.split(",");
		String action = action_input[0];
		String input = null;
		if(action_input.length > 1) input = action_input[1];
		return translate(action, input);
	}
	
	public static String translate(String action, String input) {
		ActionType ac = ActionType.valueOf(action.toUpperCase());
		if(ac != null) return ac.action() + ac.input(input);
		return null;
	}
}
