/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.osgi.alone;

import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class JavaInterview {

	public static void main(String[] args) throws Exception {
		testNIO();
	}
	public static void testHashcode() {
		String str = "new Object()";
		System.out.println(str.hashCode());
		Object obj = new Object();
		System.out.println(obj.hashCode());
		obj = new Object();
		System.out.println(obj.hashCode());
		obj = new Object();
		System.out.println(obj.hashCode());
	}
	public static void testLinkedHashMap() {
		Map<String,String> map = new LinkedHashMap<String,String>();
		map.put("a", "b");
		map.put("b", "c");
		map.put("c", "d");
		System.out.println(map);
	}
	public static void testUDP2() throws Exception {
		String newData = "New String to write..." + System.currentTimeMillis();
		ByteBuffer buf = ByteBuffer.allocate(36);
		buf.clear();
		buf.put(newData.getBytes());
		buf.flip();
		DatagramChannel channel = DatagramChannel.open();
		int bytesSent = channel.send(buf, new InetSocketAddress("localhost", 9999));
		System.out.println(newData + " == " + bytesSent);
	}
	public static void testUDP() throws Exception {
		DatagramChannel channel = DatagramChannel.open();
		channel.socket().bind(new InetSocketAddress(9999));
		ByteBuffer buf = ByteBuffer.allocate(36);
		buf.clear();
		SocketAddress addr = channel.receive(buf);
		System.out.println(addr.toString());
		System.out.println(new String(buf.array()));
	}
	public static void testNIO() throws Exception {
		DatagramChannel channel = DatagramChannel.open();
		Selector selector = Selector.open();
		channel.configureBlocking(false);
		SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
		Set<SelectionKey> selectedKeys = selector.selectedKeys();
		Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
		while(keyIterator.hasNext()) {
		    key = keyIterator.next();
		    if(key.isAcceptable()) {
		        // a connection was accepted by a ServerSocketChannel.
		    } else if (key.isConnectable()) {
		        // a connection was established with a remote server.
		    } else if (key.isReadable()) {
		        // a channel is ready for reading
		    } else if (key.isWritable()) {
		        // a channel is ready for writing
		    }
		    keyIterator.remove();
		    System.out.println(key);
		}
		
		System.exit(0);
		
		RandomAccessFile aFile = new RandomAccessFile("pom.xml", "rw");
	    FileChannel inChannel = aFile.getChannel();

	    ByteBuffer buf = ByteBuffer.allocate(48);
	    int bytesRead = inChannel.read(buf);
	    while (bytesRead != -1) {
	        buf.flip();

	        while(buf.hasRemaining()){
	            System.out.print((char) buf.get());
	        }

	        buf.clear();
	        bytesRead = inChannel.read(buf);
	    }
	    aFile.close();
	}
	
	public static void testQueue() throws InterruptedException {
		BlockingQueue<String> q = new ArrayBlockingQueue<String>(10, true);
		q.put("a");
		q.put("a");
		q.put("a");
		q.put("a");
		q.put("a");
		
		q.put("b");
		q.put("b");
		q.put("b");
		q.put("b");
		q.put("b");
		
		q.put("c");
		q.put("c");
		q.put("c");
		q.put("c");
		q.put("c");
		
		
		System.out.println(q.size());
	}
	
	public static void testStackOverflow() {
		System.out.println("testStackOverflow");
		try {
			TimeUnit.MILLISECONDS.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		testStackOverflow();
	}
	
	public static void testOutOfMemory() {
		long[][] large = new long[1000][1000];
		System.out.println("testOutOfMemory: " + large);
		try {
			TimeUnit.MINUTES.sleep(3);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void testOutOfMemory2() {
		System.out.println("testOutOfMemory2");
		List<double[]> list = new ArrayList<double[]>();
		while(list != null) {
			list.add(new double[10000000]);
			System.out.println("list: " + list.size());
			if(list.size() > 10) {
				System.gc();
			}
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
}
