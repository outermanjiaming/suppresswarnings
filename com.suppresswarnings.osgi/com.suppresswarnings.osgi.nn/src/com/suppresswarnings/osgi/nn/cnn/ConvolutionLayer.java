package com.suppresswarnings.osgi.nn.cnn;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

import com.suppresswarnings.osgi.nn.Util;

public class ConvolutionLayer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3277229167524933495L;

	double[][] kernel;
	int width;
	int height;
	int step;
	double sum;
	public ConvolutionLayer() {
	}
	public ConvolutionLayer(int w, int h, int step) {
		this.step  = step;
		this.width = w;
		this.height= h;
		this.kernel = new double[w][h];
		this.sum = Util.initReturnSum(kernel, new Random());
	}
	
	public double[][] conv(double[][] input, boolean same) {
		double[][] result = Util.conv2(kernel, input, step, same);
		return result;
	}
	public static void main(String[] args) {
		ConvolutionLayer convolutionLayer = new ConvolutionLayer(3, 3, 2);
		Util.print(convolutionLayer.kernel);
	}
	@Override
	public String toString() {
		return "ConvolutionLayer [kernel=" + Arrays.toString(kernel) + ", width=" + width + ", height=" + height
				+ ", step=" + step + ", sum=" + sum + "]";
	}
	
}
