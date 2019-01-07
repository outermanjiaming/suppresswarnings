package com.suppresswarnings.corpus.common;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NodeImpl implements Node, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4818062655321531664L;
	double placeholder;
	double momentum;
	double learningRate;
	int index;
	int level;
	int type;
	Activation fx;
	List<Link> up = new ArrayList<Link>();
	List<Link> down = new ArrayList<Link>();
	int downCounter;
	int upCounter;

	public static Node node(int type, int level, int index, double momentum, double learningRate) {
		NodeImpl node = new NodeImpl();
		node.type  = type;
		node.level = level;
		node.index = index;
		node.momentum = momentum;
		node.learningRate = learningRate;
		if(type == TYPE_BIAS) {
			node.placeholder = 1;
			node.fx = Activation.ReLU;
		} else if(type == TYPE_HIDDEN){
			node.fx = Activation.ReLU;
		} else {
			node.fx = Activation.ReLU;
		}
		return node;
	}
	@Override
	public Activation getFx() {
		return fx;
	}

	@Override
	public void upLink(Link link) {
		up.add(link);
	}

	@Override
	public void downLink(Link link) {
		down.add(link);
	}

	@Override
	public void forward() {
		propagate();
		for(Link link : down) {
			link.down().countDown();
		}
	}

	@Override
	public void propagate() {
		for(Link link : down) {
			link.assign(placeholder);
		}
	}

	@Override
	public void receive() {
		double sum = 0;
		for(Link link : up) {
			link.multiply();
			sum += link.value();
		}
		double value = fx.f(sum);
		assign(value);
	}

	@Override
	public void gradient(double error) {
		double gradient = fx.d(placeholder) * error;
		for(Link link : up) {
			double delta = momentum * link.delta() + learningRate * gradient * link.up().value();
			link.update(delta);
			link.assign(gradient);
			link.up().countUp();
		}
	}

	@Override
	public void assign(double value) {
		this.placeholder = value;
	}

	@Override
	public void countDown(){
		downCounter ++;
		if(downCounter >= up.size()) {
			downCounter = 0;
			double sum = 0;
			for(Link link : up) {
				link.multiply();
				sum += link.value();
			}
			double value = fx.f(sum);
			assign(value);
			this.forward();
		}
	}
	
	@Override
	public void countUp(){
		upCounter ++;
		if(upCounter >= down.size()) {
			upCounter = 0;
			double sum = 0;
			for(Link link : down) {
				link.multiply();
				sum += link.value();
			}
			this.gradient(sum);
		}
	}
	
	@Override
	public double value() {
		return placeholder;
	}
	
	@Override
	public void connect() {
		for(Link link : up) {
			link.down(this);
		}
		for(Link link : down) {
			link.up(this);
		}
	}
	public String getType(int type){
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
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(getType(type));
		sb.append("<").append(fx.name()).append(">");
		if(up.size() > 0) {
		sb.append("\nup:");
		for(Link link : up) {
			sb.append(link.toString()).append("\n");
		}
		}
		if(down.size() > 0) {
		sb.append("\ndown:");
		for(Link link : down) {
			sb.append(link.toString()).append("\n");
		}
		}
		return sb.toString();
	}
}
