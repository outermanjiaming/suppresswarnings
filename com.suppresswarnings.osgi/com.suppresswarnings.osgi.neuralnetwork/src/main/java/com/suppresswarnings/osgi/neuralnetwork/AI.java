/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.osgi.neuralnetwork;

import java.io.Serializable;

public interface AI extends Serializable {
	public void train(double[][] inputs, double[][] outputs);
	public double train(double[] input, double[] output);
	public double[] test(double[] input);
	public double[] layer(int layerIndexWhereInputIs0);
	public double last();
	public void last(double error);
}
