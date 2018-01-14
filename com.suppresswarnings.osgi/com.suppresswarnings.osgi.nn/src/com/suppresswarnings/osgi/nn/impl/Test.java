package com.suppresswarnings.osgi.nn.impl;

import java.util.Arrays;

import com.suppresswarnings.osgi.nn.LossFunction;
import com.suppresswarnings.osgi.nn.Util;

public class Test {

	public static String serializeTo = "nn.10.autoencoder.ser";
	public static void main(String[] args) {
		double[][] matrix = {
				{0.20000000000001, 0.40000000000001, 0.40000000000001, 0.13000000000001, 0.22000000000001, 0.64000000000001, 0.10000000000001, 0.00200000000001, 0.21000000000001, 0.61000000000001},
				{0.04000000000001, 0.12000000000001, 0.31000000000001, 0.91000000000001, 0.42000000000001, 0.30000000000001, 0.61000000000001, 0.02000000000001, 0.53000000000001, 0.15000000000001}
		};
		double[][] inputs = matrix;//{{1,0},{0,0},{1,1},{0,1}};
		double[][] outputs = matrix;//{{1},{0},{0},{1}};
		NN nn = (NN) Util.deserialize(serializeTo);//new NN(10, 10, new int[]{100});//
//		for(int n=0;n<10000000;n++) {
//			double error =0;
//			for(int i=0;i<inputs.length;i++) {
//				double[] x = inputs[i];
//				double[] target = outputs[i];
//				nn.forward(x);
//				nn.loss(target);
//				nn.backprop(nn.gradients);
//				error +=  nn.error;
//				nn.clear();
//			}
//			System.out.println("Total Error: " + error);
//			if(error < 1e-6) break;
//		}
		System.out.println(nn);
		for(int i=0;i<inputs.length;i++) {
			double[] x = inputs[i];
			double[] target = outputs[i];
			nn.forward(x);
			double[] output = nn.output();
			double error = LossFunction.MSE.f(output, target);
			System.out.println(error);
			Util.print(target);
			System.out.println(" == ");
			Util.print(output);
			System.out.println();
		}
		Util.serialize(nn, serializeTo);
	}
}
