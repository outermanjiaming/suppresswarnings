package com.suppresswarnings.osgi.nn.impl;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.suppresswarnings.osgi.nn.PointMatrix;
import com.suppresswarnings.osgi.nn.Util;
import com.suppresswarnings.osgi.nn.cnn.Clock;
import com.suppresswarnings.osgi.nn.cnn.Data;
import com.suppresswarnings.osgi.nn.cnn.DescendLayer;
import com.suppresswarnings.osgi.nn.cnn.Digit;
import com.suppresswarnings.osgi.nn.cnn.MNIST;
import com.suppresswarnings.osgi.nn.cnn.Row;
import com.suppresswarnings.osgi.nn.cnn.Saver;

public class TestMnist implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8817978059488892956L;
	DescendLayer descendLayer = new DescendLayer();
	PointMatrix pm;
	PointMatrix view;
	public static Clock clock = new Clock();
	public static String[] names = {"0","1","2","3","4","5","6","7","8","9"};
	public static String serializeTo = "D:/lijiaming/digit.nn.best";
	public NN init(Digit digit, int output) {
		double[][] image = digit.data;
		this.pm = new PointMatrix(image.length, image[0].length, 1.0);
		double[][] mask = Util.random(2,2);
		this.view = pm.viewOf(mask, 2);
		this.pm.feedMatrix(image, PointMatrix.TYPE_CONVOLUTION);
		double[][] v = view.normalizeAndTake();
		Util.print(digit.data);
		System.out.println();
		Util.print(digit.label);
		System.out.println();
		Util.print(v);
		double[] input = this.descendLayer.descend(v);
		NN network = new NN(input.length, output, new int[]{27});
		network.forward(input);
		network.loss(digit.label);
		network.clear();
		return network;
	}
	public void test(NN network) {
		MNIST mnist = new MNIST(MNIST.TYPE_TEST);
		mnist.init();
		double accuracy = 0;
		clock.start("test");
		int count = 0;
		int right = 0;
		while(mnist.hasNext()) {
			Digit digit = mnist.next();
			this.pm.feedMatrix(digit.data, PointMatrix.TYPE_CONVOLUTION);
			double[][] v = this.view.normalizeAndTake();
			double[] input = this.descendLayer.descend(v);
			double[] output = digit.label;
			network.forward(input);
			double[] result = network.output();
			int t = Util.argmax(output);
			int r = Util.argmax(result);
			if(t==r) right++;
			else {
				System.out.println(names[Util.argmax(output)]);
				List<Integer> rs = Util.multimax(result);
				if(rs.isEmpty()){
					System.out.println("[Maybe] " + names[r]);
				} else if(rs.size() == 1){
					System.out.println(names[r]);
				} else {
					for(int rt : rs) 
						System.out.print(names[rt] + " ");
				}
				
				Util.print(result);
				System.out.println();
			}
			count ++;
		}
		mnist.close();
		clock.end("test");
		long time = clock.get("test");
		accuracy = (double)right / count;
		System.out.println("test used "+time+"ms of test size: " + right + "/" + count + " = " + accuracy);
		try {
			System.out.println("sleep 10 seconds");
			TimeUnit.SECONDS.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void train(NN network) {
		Data data = new Data();
		MNIST mnist = new MNIST(MNIST.TYPE_TRAIN);
		mnist.init();
		int step = 100;
		int batch = 100;
		int epoch = 0;
		boolean run = true;
		double accuracy = 0;
		ReentrantLock lock = new ReentrantLock(true);
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.scheduleAtFixedRate(new Saver(lock, "train", network, serializeTo+".nn", 100), 35, 85, TimeUnit.SECONDS);
		System.out.println(network);
//		try {
//			PrintStream err = new PrintStream(serializeTo + ".err");
//			System.setErr(err);
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
		while(run) {
			epoch ++;
			Digit[] digits = mnist.random(batch);
			for(int i=0;i<batch;i++) {
				Digit digit = digits[i];
				this.pm.feedMatrix(digit.data, PointMatrix.TYPE_CONVOLUTION);
				double[][] v = this.view.normalizeAndTake();
				double[] input = this.descendLayer.descend(v);
				double[] output = digit.label;
				data.put(new Row(names[Util.argmax(output)], input, output));
			}
			if(epoch % 10 == 0) {
				clock.start("test");
				int count = 0;
				int right = 0;
				for(int i=0;i<data.size();i++) {
					Row row = data.get(i);
					network.forward(row.getFeature());
					double[] result = network.output();
					int t = Util.argmax(row.getTarget());
					int r = Util.argmax(result);
					if(t==r) right++;
					count ++;
				}
				clock.end("test");
				long time = clock.get("test");
				accuracy = (double)right / count;
				System.out.println("test used "+time+"ms of test size: " + right + "/" + count + " = " + accuracy);
			}
			if(accuracy > 0.99) {
				run = false;
				break;
			}
			
			int counter = 0;
			int size = data.size();
			while(counter++ < step) {
				lock.lock();
				try {
					double error = 0;
					for(int n=0;n<size;n++) {
						Row r = data.get(n);
						network.forward(r.getFeature());
						network.loss(r.getTarget());
						network.backprop(network.gradients);
						error += network.error;
						network.clear();
					}
					error = error/batch;
					System.err.println(epoch + " E: " + error);
					network.setLast(error);
					if(error < 0.00001) {
						break;
					}
					
//					under below doesn't work.
////					for(int n=0;n<size;n++) {
////						Row r = data.get(n);
////						network.forward(r.getFeature());
////						network.loss(r.getTarget());
////					}
////					double[] dEdYj = network.dEdYj();
////					for(int n=0;n<size;n++) {
////						Row r = data.get(n);
////						network.forward(r.getFeature());
////						network.backprop(dEdYj);
////					}
////					
////					double error = network.error;
////					System.err.println(epoch + " E: " + error);
////					network.clear();
					
				} finally {
					lock.unlock();
				}
			}
			data.clear();
			if(counter < 2) break;
		}
		executorService.shutdown();
		mnist.close();
	}
	
	public static void init() {
		MNIST mnist = new MNIST(MNIST.TYPE_TEST);
		mnist.init();
		mnist.start(12);
		Digit digit = mnist.next();
		Util.print(digit.data);
		Util.print(digit.label);
		mnist.close();
		TestMnist test = new TestMnist();
		NN nn = test.init(digit, 10);
		Util.serialize(test, serializeTo);
		Util.serialize(nn, serializeTo + ".nn");
		
	}

	public static void main(String[] args) throws Exception {
		TestMnist test = (TestMnist) Util.deserialize(serializeTo);
		NN nn = (NN) Util.deserialize(serializeTo + ".nn");
		test.train(nn);
	}
}
