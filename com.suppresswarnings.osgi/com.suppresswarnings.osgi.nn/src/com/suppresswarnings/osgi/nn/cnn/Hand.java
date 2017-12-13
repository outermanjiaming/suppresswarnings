package com.suppresswarnings.osgi.nn.cnn;

public class Hand{
	String[] filename;
	double[] target;
	public Hand(String[] filename, double[] target) {
		this.filename = filename;
		this.target = target;
	}
}