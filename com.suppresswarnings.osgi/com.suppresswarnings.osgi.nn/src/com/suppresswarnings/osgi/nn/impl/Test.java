package com.suppresswarnings.osgi.nn.impl;

import com.suppresswarnings.osgi.nn.LossFunction;
import com.suppresswarnings.osgi.nn.Util;

public class Test {

	public static String serializeTo = "nn.5.autoencoder.ser";
	public static void main(String[] args) {
		boolean train = false;
		double[][] matrix = Util.random(100000, 5);
//		{
//				{0.20000000000001, 0.40000000000001, 0.40000000000001, 0.13000000000001, 0.22000000000001, 0.64000000000001, 0.10000000000001, 0.00200000000001, 0.21000000000001, 0.61000000000001},
//				{0.04000000000001, 0.12000000000001, 0.31000000000001, 0.91000000000001, 0.42000000000001, 0.30000000000001, 0.61000000000001, 0.02000000000001, 0.53000000000001, 0.15000000000001}
//		};
		double[][] inputs = matrix;//{{1,0},{0,0},{1,1},{0,1}};
		double[][] outputs = matrix;//{{1},{0},{0},{1}};
		NN nn = (NN) Util.deserialize(serializeTo);//new NN(5, 5, new int[]{10});//
		
		System.out.println(nn);
		int count = 0;
		int all = 0;
		for(int i=0;i<inputs.length;i++) {
			double[] x = inputs[i];
			double[] target = outputs[i];
			nn.forward(x);
			double[] output = nn.output();
			double error = LossFunction.MSE.f(output, target);
			boolean right = error < 0.000001;
			all ++;
			if(!right) {
				Util.print(target);
				System.out.print(" <> ");
				Util.print(output);
				System.out.println();
				System.out.println();
			} else {
				count ++;
			}
		}
		double accuracy = (double)count/all;
		if(accuracy < 0.9999) {
			train = true;
			for(int n=0;n<10000000;n++) {
				double error =0;
				for(int i=0;i<inputs.length;i++) {
					double[] x = inputs[i];
					double[] target = outputs[i];
					nn.forward(x);
					nn.loss(target);
					nn.backprop(nn.gradients);
					error +=  nn.error;
					nn.clear();
				}
				System.out.println("Total Error: " + error);
				if(error < 1e-8) break;
			}
		}
		System.out.println("Accuracy: "+ accuracy + " = " + count + " / " + all);
		if(train) Util.serialize(nn, serializeTo);
	}
}
