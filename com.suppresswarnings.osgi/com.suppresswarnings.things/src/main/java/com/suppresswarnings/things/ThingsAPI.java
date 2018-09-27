/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.things;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Properties;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class ThingsAPI {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String status = "registered";
		Properties config = new Properties();
		config.load(new FileInputStream("agent.properties"));
		String server = config.getProperty("server.ssl.host", "139.199.104.224");
		String sslPorts = config.getProperty("aiiot.ssl.port", "6617");
		int sslPort = Integer.parseInt(sslPorts);
	    System.setProperty("javax.net.debug", "ssl,handshake");
	    System.setProperty("javax.net.ssl.keyStore", config.getProperty("javax.net.ssl.keyStore"));
        System.setProperty("javax.net.ssl.trustStore", config.getProperty("javax.net.ssl.trustStore"));
        System.setProperty("javax.net.ssl.keyStorePassword", config.getProperty("javax.net.ssl.keyStorePassword"));    
        System.setProperty("javax.net.ssl.trustStorePassword",config.getProperty("javax.net.ssl.trustStorePassword"));
        
		SocketFactory factory = SSLSocketFactory.getDefault();    
		Socket sslsocket = factory.createSocket(server, sslPort);    
	    String knock = String.join(",", "Bulb", "T_Code_123") + "\n";
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sslsocket.getOutputStream(),"UTF-8"));    
        out.write(knock);
        out.flush();
        InputStream is = sslsocket.getInputStream();
        while(!sslsocket.isClosed()) {
		    BufferedReader in = new BufferedReader(new InputStreamReader(is,"UTF-8"));
		    String msg = in.readLine();
		    System.out.println(msg);
	        out.write(status + "\n");
	        out.flush();
	        status = msg;
        }
	}
}
