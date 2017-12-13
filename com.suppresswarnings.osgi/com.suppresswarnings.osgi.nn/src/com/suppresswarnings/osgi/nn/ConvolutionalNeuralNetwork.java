package com.suppresswarnings.osgi.nn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConvolutionalNeuralNetwork implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8728357905490226926L;
	PointMatrix inputLayer;
	List<PointMatrix> convolutionLayer = new ArrayList<PointMatrix>();
	List<PointMatrix> maxpoolLayer = new ArrayList<PointMatrix>();
	public void init(int inputWidth, int inputHeight, double inputMax, int kernelSize, int kernelWidth, int kernelHeight, int kernelStep, int poolWidth, int poolHeight, int poolStep) {
		inputLayer = new PointMatrix(inputWidth, inputHeight, inputMax);
		double[][] pool = Util.ones(poolWidth, poolHeight);
		for(int i=0;i<kernelSize;i++) {
			PointMatrix conv = inputLayer.viewOf(Util.random(kernelWidth, kernelHeight), kernelStep);
			convolutionLayer.add(conv);
			PointMatrix max  = conv.viewOf(pool, poolStep);
			maxpoolLayer.add(max);
		}
	}
	
	public double[] input(double[][] matrix, boolean debug) {
		if(debug) Util.print(matrix);
		inputLayer.feedMatrix(matrix, PointMatrix.TYPE_CONVOLUTION);
		for(PointMatrix view : convolutionLayer) {
			double[][] conv = view.normalizeAndTake();
			if(debug) Util.print(conv);
			view.feedMatrix(conv, PointMatrix.TYPE_MAXPOOLING);
		}
		List<double[]> output = new ArrayList<double[]>();
		int length = 0 ;
		for(PointMatrix maxPool : maxpoolLayer) {
			double[][] out = maxPool.take();
			if(debug) Util.print(out);
			for(int i=0;i<out.length;i++) {
				double[] array = out[i];
				for(double x : array) if(x > 1) System.out.println("[WRONG]"+x);
				output.add(array);
				length += array.length;
			}
		}
		double[] result = new double[length];
		int index = 0;
		for(int i=0;i<output.size();i++) {
			double[] temp = output.get(i);
			for(int j=0;j<temp.length;j++) {
				result[index] = temp[j];
				index++;
			}
		}
		length = 0;
		output.clear();
		return result;
	}
	
}
