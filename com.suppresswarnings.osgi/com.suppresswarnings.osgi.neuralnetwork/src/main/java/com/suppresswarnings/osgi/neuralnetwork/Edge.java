package com.suppresswarnings.osgi.neuralnetwork;

import java.io.Serializable;

/**
 * 有向图的边
 * @author lijiaming
 *
 */
public class Edge implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4295048482858519951L;
	int level;
	double weight;
	double placeholder;
	double delta;
	double neweight;
	Cell up;
	Cell down;
	
	public void down(Cell down) {
		this.down = down;
	}

	public void up(Cell up) {
		this.up = up;
	}
	public Cell up(){
		return up;
	}
	public Cell down(){
		return down;
	}
	
	public Edge(int level, double weight) {
		this.level = level;
		this.weight = weight;
	}
	
	public void delta(double delta) {
		this.delta = delta;
		this.neweight = weight + delta;
	}
	
	public void update(){
		this.weight = neweight;
	}

	public double delta() {
		return delta;
	}

	public void multiply() {
		double value = placeholder * weight;
		assign(value);
	}

	public double value() {
		return placeholder;
	}

	public void assign(double value) {
		this.placeholder = value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Edge [level=");
		builder.append(level);
		builder.append(", weight=");
		builder.append(weight);
		builder.append(", placeholder=");
		builder.append(placeholder);
		builder.append(", delta=");
		builder.append(delta);
		builder.append(", up=");
		builder.append(up);
		builder.append(", down=");
		builder.append(down);
		builder.append("]");
		return builder.toString();
	}

}
