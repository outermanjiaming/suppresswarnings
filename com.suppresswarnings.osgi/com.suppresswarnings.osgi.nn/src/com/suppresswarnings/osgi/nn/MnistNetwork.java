package com.suppresswarnings.osgi.nn;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import com.suppresswarnings.osgi.nn.Network;
import com.suppresswarnings.osgi.nn.Util;
import com.suppresswarnings.osgi.nn.cnn.Clock;
import com.suppresswarnings.osgi.nn.cnn.Data;
import com.suppresswarnings.osgi.nn.cnn.Digit;
import com.suppresswarnings.osgi.nn.cnn.MNIST;
import com.suppresswarnings.osgi.nn.cnn.Row;
import com.suppresswarnings.osgi.nn.cnn.Saver;

public class MnistNetwork implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7414761069664389410L;
	public static Clock clock = new Clock();
	public static String[] names = {"0","1","2","3","4","5","6","7","8","9"};
	public static String serializeTo = "/Users/lijiaming/Codes/mnist/digit.net";
	ConvolutionalNeuralNetwork convolutionalNeuralNetwork = new ConvolutionalNeuralNetwork();
	public Network init(Digit digit, int output, double momentum, double learningRate) {
		double[][] image = digit.data;
		convolutionalNeuralNetwork.init(image.length, image[0].length, 1.0, 7, 4, 4, 1, 3, 3, 3);
		Util.print(digit.data);
		System.out.println();
		Util.print(digit.label);
		System.out.println();
		double[] input = convolutionalNeuralNetwork.input(image, true);
		Network network = new Network(input.length, new int[]{57}, output, momentum, learningRate);
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
			double[] input = convolutionalNeuralNetwork.input(digit.data, false);
			double[] output = digit.label;
			double[] result = network.test(input);
			int t = Util.argmax(output);
			int r = Util.argmax(result);
			if(t==r) right++;
			else {
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
		int batch = 300;
		int epoch = 0;
		boolean run = true;
		double accuracy = 0;
		Predicate<Serializable> p = new Predicate<Serializable>() {
			double last = network.error;
			@Override
			public boolean test(Serializable t) {
				Network nn = (Network)t;
				double now = nn.last();
				if(now < last) return true;
				return false;
			}
		};
		ReentrantLock lock = new ReentrantLock(true);
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.scheduleAtFixedRate(new Saver(lock, "train", network, serializeTo+".nn", p), 35, 55, TimeUnit.SECONDS);
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
				double[] input = convolutionalNeuralNetwork.input(digit.data, false);
				double[] output = digit.label;
				data.put(new Row(names[Util.argmax(output)], input, output));
			}
			if(epoch % 10 == 0) {
				clock.start("test");
				int count = 0;
				int right = 0;
				int size = data.size();
				for(int i=0;i<size;i++) {
					Row row = data.get(i);
					double[] result = network.test(row.getFeature());
					int t = Util.argmax(row.getTarget());
					int r = Util.argmax(result);
					if(t==r) right++;
					else {
						System.out.println(row.getFile());
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
			boolean print = true;
			while(counter ++ < step) {
				lock.lock();
				try {
					network.clear();
					for(int n=0;n<size;n++) {
						int i = random.nextInt(size);
						Row r = data.get(i);
						if(print) {
							Util.print(r.getFeature());
							System.out.println(" = ");
							Util.print(r.getTarget());
							print = false;
						}
						
						network.train(r.getFeature(), r.getTarget());
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
		MnistNetwork net = new MnistNetwork();
		MNIST mnist = new MNIST(MNIST.TYPE_TRAIN);
		mnist.init();
		mnist.start(1);
		Digit digit = mnist.next();
		mnist.close();
		Network network = net.init(digit, 10, 0.8, 0.0015);
		Util.serialize(net, serializeTo);
		Util.serialize(network, serializeTo + ".nn");
	}

	public static void test() {
		MnistNetwork net = (MnistNetwork) Util.deserialize(serializeTo);
		Network network = (Network) Util.deserialize(serializeTo + ".nn");
		net.test(network);
	}
	public static void train() {
		MnistNetwork net = (MnistNetwork) Util.deserialize(serializeTo);
		Network network = (Network) Util.deserialize(serializeTo + ".nn");
		net.train(network);
	}
	public static void main(String[] args) throws Exception {
		init();
	}
}
