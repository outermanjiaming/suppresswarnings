package com.suppresswarnings.osgi.nn.impl;

import java.util.Arrays;

import com.suppresswarnings.osgi.nn.Util;

public class Test {

	public static String serializeTo = "D:/tmp/nn.10.ser";
	public static void main(String[] args) {
		double[][] matrix = Util.random(5, 10);
		double[][] inputs = matrix;//{{1,0},{0,0},{1,1},{0,1}};
		double[][] outputs = matrix;//{{1},{0},{0},{1}};
		NN nn = new NN(10, 10, new int[]{4});//(NN) Util.deserialize(serializeTo);//
		for(int n=0;n<10000;n++) {
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
		}
		System.out.println(nn);
		for(int i=0;i<inputs.length;i++) {
			double[] x = inputs[i];
			double[] target = outputs[i];
			nn.forward(x);
			System.out.println(Arrays.toString(target) + " == \n" + Arrays.toString(nn.output()));
		}
		Util.serialize(nn, serializeTo);
	}
}
