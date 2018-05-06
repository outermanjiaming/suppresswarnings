/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.agent.client;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class Main {

	public static void main(String[] args) throws Exception {
    	String keyStore = args[0];
    	String trustStore = args[1];
    	String keyStorePassword = args[2];
    	String trustStorePassword = args[3];
    	String server = args[4];
    	int port = Integer.valueOf(args[5]);
    	System.setProperty("javax.net.debug", "ssl,handshake");    
        System.setProperty("javax.net.ssl.keyStore", keyStore);
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);    
        System.setProperty("javax.net.ssl.trustStorePassword",trustStorePassword);
        SocketFactory factory = SSLSocketFactory.getDefault();    
	    Socket sslsocket = factory.createSocket(server, port);    
	    System.out.println("Client Connected");  
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sslsocket.getOutputStream(),"UTF-8"));    
        out.write("client,40960,Account"+"\n");  
        out.flush();
        System.out.println("Msg Sent");
	}
}
