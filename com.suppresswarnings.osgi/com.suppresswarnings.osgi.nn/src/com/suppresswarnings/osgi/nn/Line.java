package com.suppresswarnings.osgi.nn;

import java.io.Serializable;

public class Line implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8589651650588483115L;
	double weight;
	Point to;
	public Line(double weight, Point to) {
		this.weight = weight;
		this.to = to;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(to.hashCode());
		builder.append(" {");
		builder.append(weight);
		builder.append("} ");
		return builder.toString();
	}
	
}
