package com.suppresswarnings.osgi.nn.cnn;

import java.io.Serializable;

public class NormalizeLayer implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -124842405443099952L;
	double denominator;

	public NormalizeLayer(){}
	public NormalizeLayer(double denominator){
		this.denominator = denominator;
	}
	public void normalize(double[][] input) {
		for(int i=0;i<input.length;i++) {
			for(int j=0;j<input[i].length;j++) {
				input[i][j] /= denominator;
			}
		}
	}
	
	public void denormalize(double[][] input) {
		for(int i=0;i<input.length;i++) {
			for(int j=0;j<input[i].length;j++) {
				input[i][j] *= denominator;
			}
		}
	}
	@Override
	public String toString() {
		return "NormalizeLayer [denominator=" + denominator + "]";
	}
	
}
