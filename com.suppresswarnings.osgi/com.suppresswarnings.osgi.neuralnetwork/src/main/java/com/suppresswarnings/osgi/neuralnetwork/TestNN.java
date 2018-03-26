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
	public static String serializeTo = "D:/lijiaming/random.nn.024";
	public static void main(String[] args) {
//		NN nn = new NN(5, 3, new int[] {10});
		NN nn = (NN) Util.deserialize(serializeTo);
		int step = 0;
		int epoch = 100000;
		int size = 1000;
		double[][] inputs = Util.random(size, 5);
		
		Util.print(inputs[1]);
		System.out.println();
		Util.print(get024(inputs[1]));
		while(step < epoch) {
			double error = 0;
			for(int i=0;i<size;i++) { 
				double[] input = inputs[i];
				double[] output = get024(inputs[i]);
				nn.forward(input);
				nn.loss(output);
				error += nn.error;
				nn.backprop(nn.gradients);
				nn.clear();
			}
			System.out.println(error);
			if(error < 1e-10) break;
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
