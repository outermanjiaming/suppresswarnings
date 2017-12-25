package com.suppresswarnings.osgi.nn.impl;

import java.util.Arrays;

import com.suppresswarnings.osgi.nn.Util;

public class Test {

	public static String serializeTo = "D:/tmp/nn.100.ser";
	public static void main(String[] args) {
		double[][] matrix = Util.random(11, 100);
		double[][] inputs = matrix;//{{1,0},{0,0},{1,1},{0,1}};
		double[][] outputs = matrix;//{{1},{0},{0},{1}};
		NN nn = new NN(100, 100, new int[]{70,50,70});//(NN) Util.deserialize(serializeTo);//
		for(int n=0;n<3000000;n++) {
//			for(int i=0;i<inputs.length;i++) {
//				double[] input = inputs[i];
//				double[] target = outputs[i];
//				nn.forward(input);
//				nn.loss(target);
//			} for(int i=0;i<inputs.length;i++) {
//				double[] input = inputs[i];
//				nn.forward(input);
//				nn.backprop(nn.gradients);
//				System.out.println("ERROR: " + nn.error);
//			}
//			nn.clear();
			double error =0;
			for(int i=0;i<inputs.length;i++) {
				double[] x = inputs[i];
				double[] target = outputs[i];
				nn.forward(x);
				nn.loss(target);
				nn.backprop(nn.gradients);
//				System.out.println("ERROR: " + nn.error);
				error +=  nn.error;
				nn.clear();
			}
			System.out.println("Total Error: " + error);
		}
		System.out.println(nn);
		for(int i=0;i<inputs.length;i++) {
			double[] x = inputs[i];
			double[] target = outputs[i];
			nn.forward(x);
			System.out.println(Arrays.toString(target));
		}
		Util.serialize(nn, serializeTo);
	}
}
