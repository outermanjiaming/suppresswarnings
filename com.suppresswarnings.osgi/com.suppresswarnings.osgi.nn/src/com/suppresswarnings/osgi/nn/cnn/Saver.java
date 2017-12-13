package com.suppresswarnings.osgi.nn.cnn;

import java.util.concurrent.locks.ReentrantLock;

import com.suppresswarnings.osgi.nn.Network;
import com.suppresswarnings.osgi.nn.Util;

public class Saver implements Runnable {
	Network network;
	String serializeTo;
	String job;
	int count=0;
	double last;
	ReentrantLock lock;
	public Saver() {
	}
	public Saver(String job, Network network, String serializeTo) {
		this.job = job;
		this.network = network;
		this.serializeTo = serializeTo;
		this.last = network.last();
	}
	public Saver(ReentrantLock lock, String job, Network network, String serializeTo) {
		this(job, network, serializeTo);
		this.lock = lock;
	}
	@Override
	public void run() {
		if(lock == null) {

			System.out.println("[U] " + network.last() + ", last:" + last);
			if(network.last() < last) {
				Util.serialize(network, serializeTo);
				last = network.last();
				System.out.println(count + "\t["+job+"] scheduleAtFixedRate to serialize to " + serializeTo);
			} else {
				Util.serialize(network, serializeTo+".last");
				System.out.println(count + "\t["+job+" .last] scheduleAtFixedRate to serialize to " + serializeTo);
			}
			count ++;
		
		} else {
			lock.lock();
			try {
				System.out.println("[S] " + network.last() + ", last:" + last);
				if(network.last() < last) {
					Util.serialize(network, serializeTo);
					last = network.last();
					System.out.println(count + "\t["+job+"] scheduleAtFixedRate to serialize to " + serializeTo);
				} else {
					Util.serialize(network, serializeTo+".last");
					System.out.println(count + "\t["+job+" .last] scheduleAtFixedRate to serialize to " + serializeTo);
				}
				count ++;
			} finally {
				lock.unlock();
			}
		}
	}


}
