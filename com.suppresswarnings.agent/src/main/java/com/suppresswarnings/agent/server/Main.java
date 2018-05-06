package com.suppresswarnings.agent.server;
/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class Main {
    public static void main(String args[]) throws Exception {
    	String keyStore = args[0];
    	String trustStore = args[1];
    	String keyStorePassword = args[2];
    	String trustStorePassword = args[3];
    	System.setProperty("javax.net.debug", "ssl,handshake");    
        System.setProperty("javax.net.ssl.keyStore", keyStore);
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);    
        System.setProperty("javax.net.ssl.trustStorePassword",trustStorePassword);
        long start = System.currentTimeMillis();
	    ServerSocketFactory factory =  SSLServerSocketFactory.getDefault();  
	    SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(6022);   
	    serverSocket.setNeedClientAuth(true);
	    long time = System.currentTimeMillis() - start;
	    System.out.println("Server Open: " + time + "ms");
	    int index = 0;
	    while(true) {
	    	index ++;
	    	try {
	    		start = System.currentTimeMillis();
	    		Socket socket = serverSocket.accept();
	    		time = System.currentTimeMillis() - start;
			    System.out.println(socket.toString() + ": " + time);
			    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));  
			    System.out.println(index + "\tServerLog: " + in.readLine());
			} catch (Exception e) {
				System.out.println(index + "\tException: " + e.getMessage());
			}
	    }
	}  
}
