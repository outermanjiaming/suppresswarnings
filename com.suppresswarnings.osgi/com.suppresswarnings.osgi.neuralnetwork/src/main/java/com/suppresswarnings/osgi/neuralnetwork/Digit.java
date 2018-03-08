package com.suppresswarnings.osgi.neuralnetwork;

public class Digit {

	public double[][] data;
	public double[]  label;
	public Digit(double[][] data, double[]   label) {
		this.label = label;
		this.data = data;
	}
}
