package com.suppresswarnings.corpus.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 有向图的节点
 * @author lijiaming
 *
 */
public class Cell implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4613811449299202920L;
	public static final int TYPE_BIAS   = 100;
	public static final int TYPE_INPUT  = 101;
	public static final int TYPE_HIDDEN = 102;
	public static final int TYPE_OUTPUT = 103;
	int index;
	int level;
	int type;
	double placeholder;
	Activation fx;
	List<Edge> up = new ArrayList<Edge>();
	List<Edge> down = new ArrayList<Edge>();
	int downCounter;
	int upCounter;
	double momentum = 0.8;
	double learningRate = 0.0025;
	public Cell(int type, int index, int level, Activation fx){
		this.type = type;
		this.index = index;
		this.level = level;
		this.fx = fx;
	}
	public void assign(double value) {
		this.placeholder = value;
	}
	public void upLink(Edge link) {
		up.add(link);
	}

	public void downLink(Edge link) {
		down.add(link);
	}
	
	public Edge link(Cell cell, double weight) {
		Edge edge = new Edge(level, weight);
		edge.up(this);
		edge.down(cell);
		this.downLink(edge);
		cell.upLink(edge);
		return edge;
	}

	public void propagate() {
		if(down.isEmpty()) {
			return;
		}
		for(Edge edge : down) {
			edge.assign(placeholder);
		}
	}

	public void sumUp() {
		if(up.isEmpty()) {
			return;
		}
		double sum = 0;
		for(Edge edge : up) {
			edge.multiply();
			sum += edge.value();
		}
		double y = fx.f(sum);
		assign(y);
	}
	
	public double value() {
		return placeholder;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Cell [(");
		builder.append(index);
		builder.append(",");
		builder.append(level);
		builder.append(") ");
		builder.append(getType(type));
		builder.append(", ${");
		builder.append(placeholder);
		builder.append("} ");
		builder.append(fx);
		builder.append(",");
		builder.append(up.size());
		builder.append(":->");
		builder.append(down.size());
		builder.append("]");
		return builder.toString();
	}
	
	public void gradient(double dEdYj) {
		double dEdZj = fx.d(placeholder) * dEdYj;
		for(Edge link : up) {
			Cell hi = link.up();
			double Yi = hi.value();
			double delta = momentum * link.delta() + learningRate * Yi * dEdZj;
			link.delta(delta);
			link.assign(dEdZj);
		}
	}
	
	public double backprop() {
		double dEdYi = 0;
		for(Edge link : down) {
			link.multiply();
			link.update();
			dEdYi += link.value();
		}
		return dEdYi;
	}
	
	public static String getType(int type){
		String name = type +"";
		switch (type) {
		case TYPE_BIAS:
			name = "bias";
			break;
		case TYPE_INPUT:
			name = "input";
			break;
		case TYPE_HIDDEN:
			name = "hidden";
			break;
		case TYPE_OUTPUT:
			name = "output";
			break;
		default:
			name = "unknown";
			break;
		}
		return name;
	}
}
