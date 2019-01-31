/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.common;

import java.util.Observable;
import java.util.Observer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Interview {

	class Candidate implements Observer {
		String name;
		int age;

		@Override
		public void update(Observable o, Object arg) {
			System.out.println(name + " got a notify " + arg + " > " + o);
		}
	}

	class Manager extends Observable {
		String department;
		String name;

		public void interview(Candidate can) {
			System.out.println("Hello " + can.name + ", I am " + name + " of " + department
					+ ", let's start. your age is " + can.age);
		}

		public void start() {
			setChanged();
			notifyObservers("Please on");
		}
	}

	public void start() {
		Candidate me = new Candidate();
		me.age = 19;
		me.name = "Li Jiaming";

		Candidate me2 = new Candidate();
		me2.age = 29;
		me2.name = "LI";

		Manager you = new Manager();
		you.department = "Java";
		you.name = "John";

		you.addObserver(me);
		you.addObserver(me2);
		you.interview(me);
		you.start();
	}

	public static void main(String[] args) {
		double x = Math.pow(2, Double.NaN);
		System.out.println(x);
		System.out.println("A000001"==new String("A000001"));
		System.out.println(null == null);
		Interview interview = new Interview();
		interview.start();
		int a1 = 128;
		int a2 = 128;
		System.out.println(a1 == a2);
		int b1 = 127;
		int b2 = 127;
		int b3 = 1 + b1;
		int b4 = a1 - 1;
		System.out.println(b1 == b2);
		System.out.println(b3 == a1);
		System.out.println(b4 == b1);
		
		List<Integer> list = new ArrayList<>();
		list.add(1);
		list.add(3);
		list.add(2);
		int[] array = new int[]{1,3,2};
		Collections.sort(list);
		Arrays.sort(array);
		System.out.println(list);
		System.out.println(Arrays.toString(array));
		ThreadLocal<Object> local = new ThreadLocal<>();
		AtomicReferenceArray<Integer> array2 = new AtomicReferenceArray<>(10);
		array2.set(0, 10);
		CopyOnWriteArrayList<Integer> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
		copyOnWriteArrayList.add(10);
		copyOnWriteArrayList.get(0);
		ConcurrentSkipListMap<String, String> skipListMap = new ConcurrentSkipListMap<>();
		skipListMap.put("a", "ab");
		ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<>();
		concurrentHashMap.put("a", "ab");
		ReentrantLock lock;
		
	}
}
