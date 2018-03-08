package com.suppresswarnings.osgi.neuralnetwork;

public class Test {

	public static void main(String[] args) {
		testNeuralNetwork();
	}
	
	public static void testNeuralNetwork() {
		String serialization = "D:/lijiaming/network.ser";
		DescendLayer descend = new DescendLayer();
		
		MNIST mnist = new MNIST(MNIST.TYPE_TRAIN);
		mnist.init();
		
		Network net = (Network) Util.deserialize(serialization);
		
//		Digit sample = mnist.next();
//		double[] x = descend.descend(sample.data);
//		double[] y = sample.label;
//		Network net = new Network(x.length, new int[] {130}, y.length, 0.8, 0.0015);
//		net.fullConnect();
		System.out.println(net.toString());
		
		for(int i=0;i<10;i++) {
			Digit[] train = mnist.random(10000);
			for(Digit d : train) {
				double[] input = descend.descend(d.data);
				double[] target = d.label;
				net.train(input, target);
			}
			double err = net.error();
			if(err < 0.00001) {
				break;
			}
			System.out.println(i + "\t[Total Error]\t" + err);
			net.clear();
		}
		Util.serialize(net, serialization);
		mnist.close();
		net.close();
	}
}
