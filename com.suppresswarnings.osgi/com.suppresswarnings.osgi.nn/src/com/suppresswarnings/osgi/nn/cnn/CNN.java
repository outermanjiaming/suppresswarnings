package com.suppresswarnings.osgi.nn.cnn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import com.suppresswarnings.osgi.nn.Network;
import com.suppresswarnings.osgi.nn.Util;

public class CNN implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1488154217279795555L;
	CNNLayer layer1;
	CNNLayer layer2;
	NormalizeLayer layer = new NormalizeLayer(255);
	DescendLayer descendLayer = new DescendLayer();
	public CNN(){}
	public CNN(CNNLayer cnnLayer1, CNNLayer cnnLayer2) {
		this.layer1 = cnnLayer1;
		this.layer2 = cnnLayer2;
	}

	public double[] convert(double[][] matrix){
		double[][][] featureMap = layer1.conv(matrix);
		for(int i=0;i<featureMap.length;i++) {
			double[][][] feature = layer2.conv(featureMap[i]);
			descendLayer.put(feature);
		}
		double[] result = descendLayer.take();
		return result;
	}
	
	public static void cnn(String imageFile, String serializeTo) {
		CNNLayer cnnLayer1 = new CNNLayer(1, 3, 3, 2, false, 3, 3, 2, true);
		CNNLayer cnnLayer2 = new CNNLayer(1, 3, 3, 2, false, 3, 3, 2, true);
		CNN cnn = new CNN(cnnLayer1, cnnLayer2);
		clock.start("readimage");
		double[][] image = Util.readImageAsGray(imageFile);
		clock.end("readimage");
		clock.start("normalize");
		cnn.layer.normalize(image);
		clock.end("normalize");
		clock.start("cnn");
		double[] input = cnn.convert(image);
		clock.end("cnn");
		int size = input.length / 2;
		for(int i=0;i<size;i++) System.out.println(input[i]);
		System.out.println("origin="+(image.length*image[0].length)+"("+image.length + " * " + image[0].length+")");
		System.out.println("input="+input.length);
		clock.start("serialize");
		Util.serialize(cnn, serializeTo);
		clock.end("serialize");
		clock.listAll();
	}
	static String[] names = {"TT=(1)", "ME=(2)", "EX=(3)", "WHO?"};
	public void train(List<Hand> hands, String serializeTo, boolean create) {
		Data data = new Data();
		int inputLength = 0;
		int outputLength = 0;
		for(Hand hand : hands) {
			for(String file : hand.filename){
				double[][] image = Util.readImageAsGray(file);
				layer.normalize(image);
				double[] input = this.convert(image);
				if(input.length > inputLength) inputLength = input.length;
				double[] output = hand.target;
				if(output.length > outputLength) outputLength = output.length;
				data.put(new Row(file, input, output));
			}
		}
		Network network = null;
		if(create) {
			network = new Network(inputLength, new int[]{37}, outputLength, 0.8, 0.015);
			network.fullConnect();
		} else {
			network = (Network) Util.deserialize(serializeTo);
		}
		double lastErr = network.error();
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		Predicate<Serializable> p = new Predicate<Serializable>() {
			double last = lastErr;
			@Override
			public boolean test(Serializable t) {
				Network nn = (Network)t;
				double now = nn.last();
				if(now < last) return true;
				return false;
			}
		};
		executorService.scheduleAtFixedRate(new Saver(create?"train":"improve", network, serializeTo, p), 20, 30, TimeUnit.SECONDS);
		System.out.println(network);
		int counter = 0;
		int step = 100000;
		int size = data.size();
		Random random = new Random();
		while(counter ++ < step) {
			network.clear();
			for(int n=0;n<size;n++) {
				int i = random.nextInt(size);
				Row r = data.get(i);
				network.train(r.feature, r.target);
			}
			double error = network.error();
			System.err.println(error);
			if(error < 0.001) break;
		}
		executorService.shutdown();
	}
	
	
	public void test(List<Hand> hands, String serializeTo) {
		Data data = new Data();
		for(Hand hand : hands) {
			for(String file : hand.filename){
				double[][] image = Util.readImageAsGray(file);
				layer.normalize(image);
				double[] input = this.convert(image);
				double[] output = hand.target;
				data.put(new Row(file, input, output));
			}
		}

		Network network = (Network) Util.deserialize(serializeTo);
		
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
					System.out.println("I Don't Know! Maybe " + names[r]);
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
		System.out.println(network);
	}
	
	static Clock clock = new Clock();
	public static void hand(String[] args) {
		double[] taozi = Util.onehot(0, 3);
		double[] myself= Util.onehot(1, 3);
		double[] xpd   = Util.onehot(2, 3);
//		double[] who   = Util.onehot(3, 4);
		List<Hand> hands = new ArrayList<Hand>();
		hands.add(new Hand(Util.getAbsoluteName("/Users/lijiaming/Codes/hands/", new String[]{"hand11.jpeg","hand12.jpeg","taozi.jpeg","taozi2.jpeg", "taozi3.jpeg","tz1.jpeg","tz2.jpeg","testtt1.jpeg","testtt2.jpeg","testtt3.jpeg","test3.jpeg","test4.jpeg","taozi4.jpeg","taozi5.jpeg","taozi6.jpeg"}), taozi));
		hands.add(new Hand(Util.getAbsoluteName("/Users/lijiaming/Codes/hands/", new String[]{"wo3.jpeg","mine.jpeg", "wo2.jpeg","wo1.jpeg","hand01.jpeg","hand02.jpeg","lijiaming.jpeg","lijiaming2.jpeg", "lijiaming3.jpeg","testme1.jpeg","testme2.jpeg","testme3.jpeg","test.jpeg", "test2.jpeg", "lijiaming4.jpeg", "lijiaming5.jpeg"}), myself));
		hands.add(new Hand(Util.getAbsoluteName("/Users/lijiaming/Codes/hands/", new String[]{"ex6.jpeg","ex1.jpeg","ex2.jpeg","ex3.jpeg","ex4.jpeg","ex5.jpeg","WechatIMG561512110422_.pic.jpg","WechatIMG601512110424_.pic.jpg","WechatIMG581512110423_.pic.jpg","WechatIMG591512110423_.pic.jpg", "WechatIMG611512110424_.pic.jpg", "WechatIMG571512110423_.pic.jpg"}), xpd));
		
		//test never add them to train list
//		hands.add(new Hand(Util.getAbsoluteName("/Users/lijiaming/Codes/hands/", new String[]{"xp1.jpeg"}), who));
	}
	
	//TODO: main
	public static void main(String[] args) {
		if(!true) {
			cnn("/Users/lijiaming/Codes/gesture/6/2801512180600_.pic_thumb.jpg", 
					"/Users/lijiaming/Codes/gesture/cnn5.ser");
			return;
		}
		int size = 9;
		String base = "/Users/lijiaming/Codes/gesture/";
		String[] folders = {"6","8","fuck", "index", "thumb", "pinkie", "yeah", "love", "ring","?"};
		names = folders;
		String suffix = "pic_thumb.jpg";
		List<Hand> hands = new ArrayList<Hand>();
		for(int i=0;i<size;i++) {
			String folder = base + folders[i] + "/";
			String[] name = Util.getFileNames(folder, suffix);
			double[] vector = Util.onehot(i, size);
			hands.add(new Hand(Util.getAbsoluteName(folder, name), vector));
		}
//		String folder = base + "test/";
//		String[] name = Util.getFileNames(folder, suffix);
//		double[] vector = Util.onehot(size, size+1);
//		hands.add(new Hand(Util.getAbsoluteName(folder, name), vector));
		
		String cnnser = base + "cnn5.ser";
		CNN cnn = (CNN) Util.deserialize(cnnser);
		// TODO: test train improve
//		cnn.train(hands, base + "gesture8.ser", false);
		cnn.test(hands, base + "gesture8.ser");
	}
	
	@Deprecated
	public static void expand(List<Hand> hands) {
		clock.start("cnn.ser");
		CNN cnn = (CNN) Util.deserialize("/Users/lijiaming/Codes/hands/cnn.ser");
		clock.end("cnn.ser");
		NormalizeLayer layer = new NormalizeLayer(255);
		Data data = new Data();
		clock.start("readimage");
		for(Hand hand : hands) {
			for(String file : hand.filename){
				double[][] image = Util.readImageAsGray(file);
				layer.normalize(image);
				double[] input = cnn.convert(image);
				double[] output = hand.target;
				data.put(new Row(file, input, output));
			}
		}
		clock.end("readimage");
		clock.start("handnetwork.ser");
		Network network = (Network) Util.deserialize("/Users/lijiaming/Codes/hands/handnetwork.ser");
//		network.construct();
		network.expandOutput(1);
		clock.end("handnetwork.ser");
		System.out.println(network);
		int counter = 0;
		int step = 10000;
		data.shuffle();
		clock.start("improve");
		while(counter ++ < step) {
			network.clear();
			for(Row r : data.rows) {
				network.train(r.feature, r.target);
			}
			double error = network.error();
			System.out.println(error);
			if(error < 0.001) break;
		}
		clock.end("improve");
		clock.start("serialize");
		Util.serialize(network, "/Users/lijiaming/Codes/hands/handnetworkexpand.ser");
		clock.end("serialize");
		clock.listAll();
	}
	@Override
	public String toString() {
		return "CNN [layer1=" + layer1 + ", layer2=" + layer2 + "]";
	}

	
}
