package com.suppresswarnings.osgi.neuralnetwork;

/**
 * @author lijiaming
 *
 */
public class Row {
	String file;
	double[] feature;
	double[] target;
	public Row(String file, double[] feature, double[] target) {
		this.file = file;
		this.feature = feature;
		this.target = target;
	}
	public double[] getFeature() {
		return feature;
	}
	public double[] getTarget() {
		return target;
	}
	public String getFile() {
		return file;
	}
	@Override
	public String toString() {
		if(feature == null || target == null) return "null";
		return "Row [file=" + file + ", feature=" + feature.length + ", target=" + target.length + "]";
	}
	
}
