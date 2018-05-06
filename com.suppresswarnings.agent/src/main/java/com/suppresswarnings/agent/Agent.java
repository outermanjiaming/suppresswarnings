/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.agent;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class Agent {

	static String keystorePath = "/Users/lijiaming/Codes/SuppressWarnings/keystore/.client.jks";
    static String keystorePassword = "86543210";  
    public static void main(String args[]) throws Exception{  
//    	System.setProperty("javax.net.debug", "ssl,handshake");    
        System.setProperty("javax.net.ssl.keyStore", keystorePath);
        System.setProperty("javax.net.ssl.trustStore", keystorePath);
        System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);    
        System.setProperty("javax.net.ssl.trustStorePassword",keystorePassword);  
	    SocketFactory factory = SSLSocketFactory.getDefault();    
	    Socket sslsocket = factory.createSocket("139.199.104.224", 6616);    
	    System.out.println("Client Connected");  
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sslsocket.getOutputStream(),"UTF-8"));    
        out.write("client,40960,Account"+"\n");  
        out.flush();  
        System.out.println("Msg Sent");
    }
}
