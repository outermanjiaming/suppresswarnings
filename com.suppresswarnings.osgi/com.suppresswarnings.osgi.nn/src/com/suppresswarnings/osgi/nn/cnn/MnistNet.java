package com.suppresswarnings.osgi.nn.cnn;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.suppresswarnings.osgi.nn.Network;
import com.suppresswarnings.osgi.nn.PointMatrix;
import com.suppresswarnings.osgi.nn.Util;

public class MnistNet implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7414761069664389410L;
	/**
	 * 
	 */
	DescendLayer descendLayer = new DescendLayer();
	PointMatrix pm;
	PointMatrix view;
	public static Clock clock = new Clock();
	public static String[] names = {"0","1","2","3","4","5","6","7","8","9"};
	public static String serializeTo = "/Users/lijiaming/Codes/gesture/digit.net";
	
	public Network init(Digit digit, int output, double momentum, double learningRate) {
		double[][] image = digit.data;
		this.pm = new PointMatrix(image.length, image[0].length, 1);
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
		Network network = new Network(input.length, new int[]{31}, output, momentum, learningRate);
		network.fullConnect();
		return network;
	}
	public void test(Network network) {
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
			double[] result = network.test(input);
			int t = Util.argmax(output);
			int r = Util.argmax(result);
			if(t==r) right++;
			else {
				System.out.println(names[Util.argmax(output)]);
				System.out.println(names[t]);
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
	
	public void train(Network network) {
		Data data = new Data();
		MNIST mnist = new MNIST(MNIST.TYPE_TRAIN);
		mnist.init();
		int batch = 1000;
		int epoch = 0;
		boolean run = true;
		double accuracy = 0;
		ReentrantLock lock = new ReentrantLock(true);
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.scheduleAtFixedRate(new Saver(lock, "train", network, serializeTo+".nn"), 35, 55, TimeUnit.SECONDS);
		System.out.println(network);
		try {
			PrintStream err = new PrintStream(serializeTo + ".err");
			System.setErr(err);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
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
				for(Row row : data.rows) {
					double[] result = network.test(row.feature);
					int t = Util.argmax(row.target);
					int r = Util.argmax(result);
					if(t==r) right++;
					else {
						System.out.println(row.file);
						System.out.println(names[t]);
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
			if(accuracy > 0.99) {
				run = false;
				break;
			}
			
			int counter = 0;
			int step = 1000;
			int size = data.size();
			Random random = new Random();
			while(counter ++ < step) {
				lock.lock();
				try {
					network.clear();
					for(int n=0;n<size;n++) {
						int i = random.nextInt(size);
						Row r = data.get(i);
						network.train(r.feature, r.target);
					}
					double error = network.error();
					System.err.println(error);
					if(error < 0.001) {
						batch = 1000;
						break;
					}
				} finally {
					lock.unlock();
				}
			}
			data.clear();
		}
		executorService.shutdown();
		mnist.close();
	}
	
	public static void init() {
		MnistNet net = new MnistNet();
		MNIST mnist = new MNIST(MNIST.TYPE_TEST);
		mnist.init();
		mnist.start(12);
		Digit digit = mnist.next();
		Util.print(digit.data);
		Util.print(digit.label);
		mnist.close();
		Network network = net.init(digit, 10, 0.8, 0.0025);
		Util.serialize(net, serializeTo);
		Util.serialize(network, serializeTo + ".nn");
	}

	public static void test() {
		MnistNet net = (MnistNet) Util.deserialize(serializeTo);
		Network network = (Network) Util.deserialize(serializeTo + ".nn.last");
		net.test(network);
	}
	public static void train() {
		MnistNet net = (MnistNet) Util.deserialize(serializeTo);
		Network network = (Network) Util.deserialize(serializeTo + ".nn");
		net.train(network);
	}
	public static void main(String[] args) throws Exception {
//		init();
//		train();
		test();
	}
}
