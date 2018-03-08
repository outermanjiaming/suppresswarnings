package com.suppresswarnings.osgi.nn.cnn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import com.suppresswarnings.osgi.nn.Network;
import com.suppresswarnings.osgi.nn.PointMatrix;
import com.suppresswarnings.osgi.nn.Util;

public class ImageNet implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2274975284085722514L;
	DescendLayer descendLayer = new DescendLayer();
	PointMatrix pm;
	PointMatrix view;
	
	public Network init(String imageFile, int output, double momentum, double learningRate) {
		double[][] image = Util.readImageAsGray(imageFile);
		this.pm = new PointMatrix(image.length, image[0].length, 255);
		double[][] mask = Util.random(5, 5);
		this.view = pm.viewOf(mask, 3);
		this.pm.feedMatrix(image, 0);
		double[][] v = view.normalizeAndTake();
		Util.print(image);
		System.out.println();
		Util.print(v);
		double[] input = this.descendLayer.descend(v);
		Network network = new Network(input.length, new int[]{177}, output, momentum, learningRate);
		network.fullConnect();
		return network;
	}
	
	public void train(List<Hand> hands, Network network) {
		Data data = new Data();
		for(Hand hand : hands) {
			for(String file : hand.filename){
				double[][] image = Util.readImageAsGray(file);
				this.pm.feedMatrix(image,0);
				double[][] v = this.view.normalizeAndTake();
				double[] input = this.descendLayer.descend(v);
				double[] output = hand.target;
				data.put(new Row(file, input, output));
			}
		}
		
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
		System.out.println("test used "+time+"ms of test size: " + right + "/" + count + " = " + (double)right / count);
		try {
			TimeUnit.MINUTES.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ReentrantLock lock = new ReentrantLock(true);
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		Predicate<Serializable> p = new Predicate<Serializable>() {
			double last = network.error();
			@Override
			public boolean test(Serializable t) {
				Network nn = (Network)t;
				double now = nn.last();
				if(now < last) return true;
				return false;
			}
		};
		executorService.scheduleAtFixedRate(new Saver(lock, "train", network, serializeTo+".nn", p), 70, 130, TimeUnit.SECONDS);
		System.out.println(network);
		int counter = 0;
		int step = 100000;
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
				if(error < 0.001) break;
			} finally {
				lock.unlock();
			}
		}
		executorService.shutdown();
	}
	public void test(List<Hand> hands, Network network) {

		Data data = new Data();
		for(Hand hand : hands) {
			for(String file : hand.filename){
				double[][] image = Util.readImageAsGray(file);
				this.pm.feedMatrix(image,0);
				double[][] v = this.view.normalizeAndTake();
				double[] input = this.descendLayer.descend(v);
				double[] output = hand.target;
				data.put(new Row(file, input, output));
			}
		}
		
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
		System.out.println("test used "+time+"ms of test size: " + right + "/" + count + " = " + (double)right / count);
	
	}
	public static Clock clock = new Clock();
	public static String[] names = {};
	public static String serializeTo = "/Users/lijiaming/Codes/gesture/image2.net";
	public static void init(String[] args) {
		String imageFile = "/Users/lijiaming/Codes/gesture/6/2801512180600_.pic_thumb.jpg";
		ImageNet net = new ImageNet();
		Network network = net.init(imageFile, 9, 0.8, 0.015);
		Util.serialize(net, serializeTo);
		Util.serialize(network, serializeTo + ".nn");
	}
	
	public static void main(boolean test) {
		ImageNet net = (ImageNet) Util.deserialize(serializeTo);
		Network network = (Network) Util.deserialize(serializeTo + ".nn");
		int size = 9;
		String base = "/Users/lijiaming/Codes/gesture/";
		String[] folders = {"6","8","fuck", "index", "thumb", "pinkie", "yeah", "love", "ring","?"};
		names = folders;
		String suffix = "pic_thumb.jpg";
		List<Hand> hands = new ArrayList<Hand>();
		if(test) {
			String folder = base + "test/";
			String[] name = Util.getFileNames(folder, suffix);
			double[] vector = Util.onehot(size, size+1);
			hands.add(new Hand(Util.getAbsoluteName(folder, name), vector));
			
			net.test(hands, network);
		} else {
			for(int i=0;i<size;i++) {
				String folder = base + folders[i] + "/";
				String[] name = Util.getFileNames(folder, suffix);
				double[] vector = Util.onehot(i, size);
				hands.add(new Hand(Util.getAbsoluteName(folder, name), vector));
			}
			net.train(hands, network);
		}
	}
	public static void main(String[] args) {
		main(false);
	}
}
