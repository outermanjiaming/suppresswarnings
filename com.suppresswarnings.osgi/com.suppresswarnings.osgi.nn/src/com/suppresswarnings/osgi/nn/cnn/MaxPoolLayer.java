package com.suppresswarnings.osgi.nn.cnn;

import java.io.Serializable;
import java.util.Random;

import com.suppresswarnings.osgi.nn.Util;

public class MaxPoolLayer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5054599342041927252L;
	int width;
	int height;
	int step;
	public MaxPoolLayer() {
	}
	public MaxPoolLayer(int w, int h, int step) {
		this.step  = step;
		this.width = w;
		this.height= h;
	}
	
	public double[][] pool(double[][] input) {
		return Util.maxpooling(input, width, height, step);
	}
	
	
	@Override
	public String toString() {
		return "MaxPoolLayer [width=" + width + ", height=" + height + ", step=" + step + "]";
	}
	public static void main(String[] args) {
		double[][] input = new double[7][7];
		double sum = Util.initReturnSum(input, new Random());
		Util.print(input);
		NormalizeLayer normal = new NormalizeLayer(sum);
		normal.normalize(input);
		Util.print(input);
		System.out.println();
		normal.denormalize(input);
		Util.print(input);
		System.out.println();
		MaxPoolLayer layer = new MaxPoolLayer(3, 3, 2);
		double[][] result = layer.pool(input);
		Util.print(result);
	}
}
