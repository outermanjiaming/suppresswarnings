package com.suppresswarnings.osgi.neuralnetwork;


import java.io.Serializable;

public class LinkImpl implements Link, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6185548321485415042L;
	int level;
	double weight;
	double placeholder;
	double delta;
	Node up;
	Node down;

	public static Link link(int level, double weight, Node up, Node down) {
		LinkImpl link = new LinkImpl();
		link.level = level;
		link.weight = weight;
		
		link.up(up);
		link.down(down);
		up.downLink(link);
		down.upLink(link);
		return link;
	}
	
	@Override
	public void update(double delta) {
		this.delta = delta;
		this.weight += delta;
	}

	@Override
	public double delta() {
		return delta;
	}

	@Override
	public void multiply() {
		double value = placeholder * weight;
		assign(value);
	}

	@Override
	public double value() {
		return placeholder;
	}

	@Override
	public void assign(double value) {
		this.placeholder = value;
	}

	@Override
	public Node down() {
		return down;
	}

	@Override
	public Node up() {
		return up;
	}

	@Override
	public void down(Node down) {
		this.down = down;
	}

	@Override
	public void up(Node up) {
		this.up = up;
	}

	@Override
	public String toString() {
		return "{"+ weight + "}";
	}
}
