package com.suppresswarnings.osgi.neuralnetwork;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

public class TestMnist implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8817978059488892956L;
	DescendLayer descendLayer = new DescendLayer();
	PointMatrix pm;
	PointMatrix view;
	double best = Integer.MAX_VALUE;
	public static int batch = 1000;
	public static Clock clock = new Clock();
	public static String[] names = {"0","1","2","3","4","5","6","7","8","9"};
	public static String serializeTo = "D:/lijiaming/digit.nn.relu";
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
		network.backprop(digit.label);
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
	
	public void train(AI network) {
		Data data = new Data();
		MNIST mnist = new MNIST(MNIST.TYPE_TRAIN);
		mnist.init();
		int step = 100;
		int epoch = 0;
		boolean run = true;
		double accuracy = 0;
		ReentrantLock lock = new ReentrantLock(true);
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		Predicate<Serializable> p = new Predicate<Serializable>() {
			double last = best;
			@Override
			public boolean test(Serializable t) {
				NN nn = (NN)t;
				double now = nn.last();
				if(now < last) {
					last = now;
					return true;
				}
				System.out.println(now + " > " + last);
				return false;
			}
		};
		executorService.scheduleAtFixedRate(new Saver(lock, "train", network, serializeTo+".nn", p), 35, 85, TimeUnit.SECONDS);
		System.out.println(network);
		int position = 0;
		List<Row> failed = new ArrayList<Row>();
		while(run) {
			if(position + batch >= mnist.size()) {
				position = 0;
			}
			mnist.start(position);
			Digit[] digits = mnist.next(batch);
			epoch ++;
			position += batch;
			for(int i=0;i<batch;i++) {
				Digit digit = digits[i];
				this.pm.feedMatrix(digit.data, PointMatrix.TYPE_CONVOLUTION);
				double[][] v = this.view.normalizeAndTake();
				double[] input = this.descendLayer.descend(v);
				double[] output = digit.label;
				data.put(new Row(names[Util.argmax(output)], input, output));
			}
			for(int i=0;i<failed.size();i++) {
				Row row = failed.get(i);
				double[] result = network.test(row.getFeature());
				int t = Util.argmax(row.getTarget());
				int r = Util.argmax(result);
				if(t!=r) {
					data.put(row);
				}
			}
			failed.clear();
			if(epoch % 10 == 0) {
				clock.start("test");
				int count = 0;
				int right = 0;
				
				MNIST testmnist = new MNIST(MNIST.TYPE_TEST);
				testmnist.init();
				Data testdata = new Data();
				while(testmnist.hasNext()) {
					Digit digit = testmnist.next();
					this.pm.feedMatrix(digit.data, PointMatrix.TYPE_CONVOLUTION);
					double[][] v = this.view.normalizeAndTake();
					double[] input = this.descendLayer.descend(v);
					double[] output = digit.label;
					testdata.put(new Row(names[Util.argmax(output)], input, output));
				}
				testmnist.close();
				
				for(int i=0;i<testdata.size();i++) {
					Row row = testdata.get(i);
					double[] result = network.test(row.getFeature());
					int t = Util.argmax(row.getTarget());
					int r = Util.argmax(result);
					if(t==r) {
						right++;
					} else {
						failed.add(row);
					}
					count ++;
				}
				testdata.clear();
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
						error += network.train(r.getFeature(), r.getTarget());
					}
					error = error/batch;
					System.err.println(epoch + " E: " + error);
					if(error < 0.001) {
						break;
					}
				} finally {
					lock.unlock();
				}
			}
			data.clear();
			double rand = Util.random();
			if(counter < 2 && rand < 0) break;
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
//		TestMnist.init();
		TestMnist test = (TestMnist) Util.deserialize(serializeTo);
		AI nn = (NN) Util.deserialize(serializeTo + ".nn");
		test.train(nn);
	}
}
