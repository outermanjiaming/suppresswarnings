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

public class Client {
	public static void main(String[] args) throws Exception {
		Socket sslsocket = new Socket("suppresswarnings.com", 6616);    
	    System.out.println("Client Connected");  
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sslsocket.getOutputStream(),"UTF-8"));    
        out.write("client,40960,Account"+"\n");  
        out.flush();  
        sslsocket.close();
        System.out.println("Msg Sent");
	}
}
