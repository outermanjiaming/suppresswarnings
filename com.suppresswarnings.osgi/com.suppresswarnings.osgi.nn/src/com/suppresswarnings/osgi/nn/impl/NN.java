package com.suppresswarnings.osgi.nn.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.suppresswarnings.osgi.nn.LossFunction;

/**
 * this neural network could imply minibatch SGD
 * @author lijiaming
 *
 */
public class NN implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5742996892170581827L;
	List<Layer> hiddenLayer;
	Layer input;
	Layer output;
	LossFunction loss = LossFunction.MSE;
	double error;
	double lastErr;
	double[] gradients;
	int count = 0;
	
	/**
	 * for unlinked layers
	 * @param inputSize
	 * @param outputSize
	 * @param hiddenSize
	 */
	public NN(int inputSize, int outputSize, int[] hiddenSize) {
		int level=0;
		Layer linker = null;
		this.input = new Layer(Cell.TYPE_INPUT, inputSize, level);
		linker = this.input;
		this.hiddenLayer = new ArrayList<Layer>();
		for(int hidden : hiddenSize) {
			Layer layer = new Layer(Cell.TYPE_HIDDEN, hidden, level++);
			this.hiddenLayer.add(layer);
			linker.link(layer);
			linker = layer;
		}
		this.output = new Layer(Cell.TYPE_OUTPUT, outputSize, level++);
		linker.link(this.output);
		this.gradients = new double[this.output.size];
		linker = null;
	}

	public void forward(double[] x){
		this.input.assign(x);
		this.input.forward();
		for(Layer layer : hiddenLayer) {
			layer.forward();
		}
		this.output.forward();
	}
	
	public void backprop(double[] dEdYj){
		this.output.gradient(dEdYj);
		
		for(Layer layer : hiddenLayer) {
			double[] dEdYi = layer.backprop();
			layer.gradient(dEdYi);
		}
		
		this.input.backprop();
	}
	public void input(double[] input){
		this.input.assign(input);
	}
	public double[] output(){
		return this.output.value();
	}
	
	public double[] value(Cell[] cells) {
		double[] result = new double[cells.length];
		for(int i=0;i<cells.length;i++) {
			result[i] = cells[i].value();
		}
		return result;
	}
	
	public void loss(double[] target) {
		double[] output = output();
		this.error += loss.f(output, target);
		double[] gradient = loss.d(output, target);
		count ++;
		for(int i=0;i<gradient.length;i++) {
			this.gradients[i] += gradient[i];
		}
	}
	
	
	public void clear(){
		this.lastErr = this.error;
		this.error = 0;
		this.count = 0;
		for(int i=0;i<this.gradients.length;i++) {
			this.gradients[i] = 0;
		}
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NN [network=\n");
		builder.append(input).append("\n");
		for(Layer hidden : hiddenLayer) builder.append(hidden).append("\n");
		builder.append(output).append("\n");
		builder.append("]");
		return builder.toString();
	}
	
	
}
