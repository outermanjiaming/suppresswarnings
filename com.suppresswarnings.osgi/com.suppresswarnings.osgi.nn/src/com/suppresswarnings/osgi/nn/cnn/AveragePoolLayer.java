package com.suppresswarnings.osgi.nn.cnn;

import java.io.Serializable;
import java.util.Random;

import com.suppresswarnings.osgi.nn.Util;

public class AveragePoolLayer implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6141101153841746225L;
	int width;
	int height;
	int step;
	public AveragePoolLayer() {
	}
	public AveragePoolLayer(int w, int h, int step) {
		this.step  = step;
		this.width = w;
		this.height= h;
	}
	
	public double[][] pool(double[][] input) {
		return Util.averagepooling(input, width, height, step);
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
		AveragePoolLayer layer = new AveragePoolLayer(3, 3, 2);
		double[][] result = layer.pool(input);
		Util.print(result);
	}
}
