/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.osgi.neuralnetwork;

public class TestNN {
	public static String serializeTo = "D:/lijiaming/NN.dnn.024";
	public static void main(String[] args) {
//		Network nn = new Network(5, new int[] {5, 5}, 3, 0.0, 0.0001);
//		nn.fullConnect();
		//NN nn = new NN(5, 3, new int[] {5, 5});
		AI nn = (AI) Util.deserialize(serializeTo);
		int step = 0;
		int epoch = 10000000;
		int size = 1000;
		double[][] inputs = Util.random(size, 5);
		
		Util.print(inputs[1]);
		System.out.println();
		Util.print(get024(inputs[1]));
		System.out.println();
		
		while(step < epoch) {
			double error = 0;
			for(int i=0;i<size;i++) { 
				double[] input = inputs[i];
				double[] output = get024(inputs[i]);
				error += nn.train(input, output);
			}
			System.out.println(error);
			if(error < 1e-4) break;
			step ++;
		}
		System.out.println(nn.toString());
		Util.serialize(nn, serializeTo);
	}
	
	public static double[] get024(double[] len5) {
		double[] len3 = new double[3];
		len3[0] = len5[0];
		len3[1] = len5[2];
		len3[2] = len5[4];
		return len3;
	}
}
