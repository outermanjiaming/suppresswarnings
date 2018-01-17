package com.suppresswarnings.osgi.nn.impl;

import com.suppresswarnings.osgi.nn.LossFunction;
import com.suppresswarnings.osgi.nn.Util;

public class DNN {
	public static String serializeTo = "dnn.10.autoencoder.ser";
	public static void main(String[] args) {
		boolean train = false;
		double[][] matrix = Util.random(1, 10);
		double[][] inputs = matrix;
		double[][] outputs = matrix;
		NN nn = new NN(10, 10, new int[]{100, 1000, 100});//(NN) Util.deserialize(serializeTo);//
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
				System.out.println(" <> ");
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
			while(true){
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
				if(error < 1e-22) break;
			}
		}
		
		count = 0;
		all = 0;
		for(int i=0;i<inputs.length;i++) {
			double[] x = inputs[i];
			double[] target = outputs[i];
			nn.forward(x);
			double[] output = nn.output();
			double error = LossFunction.MSE.f(output, target);
			boolean right = error < 0.000001;
			all ++;
			if(right) {
				count ++;
			}
			Util.print(target);
			System.out.println(" == ");
			Util.print(output);
			System.out.println();
			System.out.println();
		}
		accuracy = (double)count/all;
		System.out.println("Accuracy: "+ accuracy);
		if(train) Util.serialize(nn, serializeTo);
	}
}
