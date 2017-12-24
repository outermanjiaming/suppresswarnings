package com.suppresswarnings.osgi.nn.impl;

import java.util.Arrays;

import com.suppresswarnings.osgi.nn.Util;

public class Test {

	public static String serializeTo = "D:/tmp/nn.ser.new";
	public static void main(String[] args) {
		double[][] matrix = {
				{0.6936640202187698, 0.029268482161491804, 0.6306748768026917, 0.18425000567242622},
						{0.8180268025646483, 0.5987180903745107, 0.409199972714567, 0.5526248419237468},
						{0.9389016470689922, 0.07873130976820497, 0.7873996626734437, 0.5891535441903848},
						{0.5878314354213111, 0.8751443265417698, 0.4004497562039059, 0.8374984228844423},
						{0.5922423164315338, 0.38151102442220564, 0.34452676040550434, 0.7106548177848059}
		};
		double[][] inputs = matrix;//{{1,0},{0,0},{1,1},{0,1}};
		double[][] outputs = matrix;//{{1},{0},{0},{1}};
		NN nn = (NN) Util.deserialize(serializeTo);//new NN(4, 4, new int[]{5});
		for(int n=0;n<1000000;n++) {
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
				System.out.println("ERROR: " + nn.error);
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
