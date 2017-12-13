package com.suppresswarnings.osgi.nn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Point implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1372886752275285794L;
	double value;
	List<Line> out = new ArrayList<Line>();
	public Point(){}
	public Point(double value) {
		this.value = value;
	}
	public void set(double value) {
		this.value = value;
	}
	public double value(){
		return this.value;
	}
	public void convolution(){
		for(Line line : out) {
			line.to.sum(this.value * line.weight);
		}
	}
	public void maxpooling(){
		for(Line line : out) {
			line.to.max(this.value);
		}
	}
	public synchronized void max(double value) {
		if(this.value < value) this.value = value;
	}
	public synchronized void sum(double valueXweight) {
		this.value += valueXweight;
	}
	public void out(double weight, Point to) {
		Line line = new Line(weight, to);
		out.add(line);
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Point [");
		builder.append(value);
		builder.append("]");
		if(!out.isEmpty()) {
			builder.append("->");
			builder.append(out);
		}
		return builder.toString();
	}
	
}
