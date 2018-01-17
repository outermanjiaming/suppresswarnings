package com.suppresswarnings.osgi.nn.impl;

import com.suppresswarnings.osgi.nn.LossFunction;
import com.suppresswarnings.osgi.nn.Util;

public class Calc {
	public static String serializeTo = "nn.calc.ser";
	public static void main(String[] args) {
		double[][] inputs = {{3,2},{4,7},{5,9},{6,9}, {7,3},{8,5},{9,3}, {1,1},{2,2},{3,3},{4,4}     ,{2,3},{4,3},{4,6},{5,2},{7,7},{1,2},{3,4},{5,6},{7,8},{9,10},{3,6},{4,5},{5,6},{5,7}, {7,8},{5,9},{3,7},{5,5},{7,9},{3,10}};
		double[][] outputs = {{6},{28},{45},{54},      {21},{40},{27}    ,{1},{4}  ,{9}     ,{16} ,{6},{12},{24},{10},{49},{2},{12},{30},{56},{90},{18},{20},{30},{35},{56},{45},{21},{25},{63},{30}};
		NN nn = (NN) Util.deserialize(serializeTo);//new NN(2, 1, new int[]{1000});//
//		for(int n=0;n<1000000;n++) {
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
			System.out.println(error < 0.0001);
//			Util.print(target);
//			System.out.println(" == ");
//			Util.print(output);
//			System.out.println();
		}
//		Util.serialize(nn, serializeTo);
	}
}
