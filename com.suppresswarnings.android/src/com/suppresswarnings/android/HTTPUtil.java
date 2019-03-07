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
}
