/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.ai;

import java.io.File;

/**
 * xor neural network demo
 * @author lijiaming
 *
 */
public class Demo {
	public static void main(String[] args) {
		AI ai = null;
		String file = "xor.ai.ser";
		if(new File(file).exists()) {
			System.out.println("deserialize from existing file");
			ai = (AI) Util.deserialize(file);
		} else {
			System.out.println("create new ai object");
			ai = new NN(2,2, new int[] {5,5});
		}
		
		double[][] inputs = new double[4][2];
		double[][] outputs = new double[4][2];
		inputs[0] = new double[]{0,0};
		inputs[1] = new double[]{0,1}; 
		inputs[2] = new double[]{1,0}; 
		inputs[3] = new double[]{1,1}; 
		outputs[0] = new double[]{1,0};
		outputs[1] = new double[]{0,1}; 
		outputs[2] = new double[]{0,1}; 
		outputs[3] = new double[]{1,0}; 
		System.out.println("train until converge");
		ai.train(inputs, outputs);
		System.out.println("serialize ai ser to file");
		Util.serialize(ai, file);
	}
}
