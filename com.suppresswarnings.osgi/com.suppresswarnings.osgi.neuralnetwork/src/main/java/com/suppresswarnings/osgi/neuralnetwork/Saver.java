package com.suppresswarnings.osgi.neuralnetwork;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

public class Saver implements Runnable {
	Serializable network;
	String serializeTo;
	String job;
	int count=0;
	ReentrantLock lock;
	Predicate<Serializable> tester;
	public Saver() {
	}
	public Saver(String job, Serializable network, String serializeTo, Predicate<Serializable> tester) {
		this.job = job;
		this.network = network;
		this.serializeTo = serializeTo;
		this.tester = tester;
	}
	public Saver(ReentrantLock lock, String job, Serializable network, String serializeTo, Predicate<Serializable> tester) {
		this(job, network, serializeTo, tester);
		this.lock = lock;
	}
	@Override
	public void run() {
		if(lock == null) {
			System.out.println("[U] " + network.toString());
			if(tester.test(network)) {
				Util.serialize(network, serializeTo);
				System.out.println(count + "\t["+job+"] scheduleAtFixedRate to serialize to " + serializeTo);
			} else {
				Util.serialize(network, serializeTo+".last");
				System.out.println(count + "\t["+job+" .last] scheduleAtFixedRate to serialize to " + serializeTo);
			}
			count ++;
		
		} else {
			lock.lock();
			try {
				System.out.println("[S] " + network.toString());
				if(tester.test(network)) {
					Util.serialize(network, serializeTo);
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
