package com.suppresswarnings.osgi.nn.cnn;

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
}
