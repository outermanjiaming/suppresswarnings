package com.suppresswarnings.corpus.common;

import java.io.Serializable;
import java.util.Arrays;

public class Layer implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1473266256640666460L;
	String name;
	int type;
	int level;
	int size;
	Cell[] cells;
	public Layer(int type, int size, int level) {
		this.name = Cell.getType(type);
		this.type = type;
		this.level = level;
		this.size = size;
		if(type == Cell.TYPE_OUTPUT) {
			cells = new Cell[size];
		} else {
			cells = new Cell[size + 1];
			Cell bias = new Cell(Cell.TYPE_BIAS, size, level, Activation.ReLU);;
			bias.assign(1);
			cells[size] = bias;
		}
		for(int i=0;i<size;i++) {
			cells[i] = new Cell(type, i, level, Activation.ReLU);
		}
	}
	
	public void link(Layer down) {
		for(int i=0;i<down.size;i++) {
			for(Cell cell : cells) {
				cell.link(down.cells[i], Util.random());
			}
		}
	}
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Layer [");
		builder.append(name);
		builder.append(", level=");
		builder.append(level);
		builder.append(Arrays.toString(cells));
		builder.append("]");
		return builder.toString();
	}

	public void assign(double[] x) {
		for(int i=0;i<size;i++) {
			cells[i].assign(x[i]);
		}
	}
	
	public void forward(){
		for(Cell cell : cells) {
			cell.sumUp();
			cell.propagate();
		}
	}

	public void gradient(double[] dEdYj) {
		for(int i=0;i<dEdYj.length;i++) {
			Cell cell = cells[i];
			cell.gradient(dEdYj[i]);
		}
	}

	public double[] backprop() {
		double[] dEdYi = new double[cells.length];
		for(int i=0;i<cells.length;i++) {
			dEdYi[i] = cells[i].backprop();
		}
		return dEdYi;
	}

	public double[] value() {
		double[] value = new double[size];
		for(int i=0;i<size;i++) {
			value[i] = cells[i].value();
		}
		return value;
	}
	
	public double[] softmax() {
		double[] value = new double[size];
		double sum = 0; 
		for(int i=0;i<size;i++) {
			sum += Math.exp(cells[i].value());
		}
		for(int i=0;i<size;i++) {
			value[i] = Math.exp(cells[i].value()) / sum;
		}
		return value;
	}
	
}
