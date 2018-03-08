package com.suppresswarnings.osgi.neuralnetwork;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DescendLayer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6151677809572051864L;
	int length = 0;
	List<double[]> results = new ArrayList<double[]>();

	public double[] descend(double[][] input) {
		int length = 0;
		for(int i=0;i<input.length;i++){
			length += input[i].length;
		}
		double[] result = new double[length];
		int index = 0;
		for(int i=0;i<input.length;i++){
			for(int j=0;j<input[i].length;j++) {
				result[index] = input[i][j];
				index ++;
			}
		}
		return result;
	}
	
	public double[] take(){
		double[] result = new double[length];
		int index = 0;
		for(double[] temp : results) {
			System.arraycopy(temp, 0, result, index, temp.length);
			index += temp.length;
		}
		length = 0;
		results.clear();
		return result;
	}
	
	public void put(double[][][] cube) {
		for(int i=0;i<cube.length;i++) {
			double[] temp = descend(cube[i]);
			length += temp.length;
			results.add(temp);
		}
	}
}
