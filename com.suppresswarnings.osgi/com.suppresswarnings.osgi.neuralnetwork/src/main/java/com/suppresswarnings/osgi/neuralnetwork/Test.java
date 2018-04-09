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
		
		AI net = (Network) Util.deserialize(serialization);
		
//		Digit sample = mnist.next();
//		double[] x = descend.descend(sample.data);
//		double[] y = sample.label;
//		Network net = new Network(x.length, new int[] {100}, y.length, 0.8, 0.0015);
//		net.fullConnect();
		System.out.println(net.toString());
		
		for(int i=0;i<100000;i++) {
			Digit[] train = mnist.random(1000);
			double err = 0;
			for(Digit d : train) {
				double[] input = descend.descend(d.data);
				double[] target = d.label;
				err += net.train(input, target);
			}
			err /= train.length;
			if(err < 0.001) {
				break;
			}
			System.out.println(i + "\t[Total Error]\t" + err);
		}
		Util.serialize(net, serialization);
		mnist.close();
	}
}
