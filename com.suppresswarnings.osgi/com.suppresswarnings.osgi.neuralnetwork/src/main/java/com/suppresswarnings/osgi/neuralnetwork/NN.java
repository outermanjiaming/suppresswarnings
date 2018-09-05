package com.suppresswarnings.osgi.neuralnetwork;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * this neural network could imply minibatch SGD
 * @author lijiaming
 *
 */
public class NN implements AI, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5742996892170581827L;
	List<Layer> hiddenLayer;
	Layer input;
	Layer output;
	LossFunction loss = LossFunction.MSE;
	int inputSize;
	int outputSize;
	int[] hiddenSize;
	int max = 10;
	double tolerate = 0.001;
	double error = 0;
	/**
	 * for unlinked layers
	 * @param inputSize
	 * @param outputSize
	 * @param hiddenSize
	 */
	public NN(int inputSize, int outputSize, int[] hiddenSize) {
		this.inputSize = inputSize;
		this.outputSize = outputSize;
		this.hiddenSize = hiddenSize;
		int level=0;
		Layer linker = null;
		this.input = new Layer(Cell.TYPE_INPUT, inputSize, level++);
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
		linker = null;
	}

	public void forward(double[] x){
		this.input.assign(x);
		this.input.forward();
		for(int i=0;i<hiddenLayer.size();i++) {
			Layer layer = hiddenLayer.get(i);
			layer.forward();
		}
		this.output.forward();
	}
	
	public double backprop(double[] target){
		double[] output = output();
		double[] dEdYj = loss.d(output, target);
		this.output.gradient(dEdYj);
		
		for(int i=hiddenLayer.size()-1;i>=0;i--) {
			Layer layer = hiddenLayer.get(i);
			double[] dEdYi = layer.backprop();
			layer.gradient(dEdYi);
		}
		
		this.input.backprop();
		return loss.f(output, target);
	}
	
	public double[] output(){
		return this.output.value();
	}
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NN [network=\n");
		builder.append("[").append(inputSize).append(" -> ").append(Arrays.toString(hiddenSize)).append(" -> ").append(outputSize).append("]\n");
		builder.append(input).append("\n");
		for(Layer hidden : hiddenLayer) builder.append(hidden).append("\n");
		builder.append(output).append("\n");
		builder.append("]");
		return builder.toString();
	}

	@Override
	public void train(double[][] inputs, double[][] outputs) {
		int size = inputs.length;
		int step = 0;
		while(step ++ < max) {
			double err = 0;
			for(int i=0;i<size;i++) {
				err += train(inputs[i], outputs[i]);
			}
			System.out.println(step + "\tErr: " + err);
			if(err < tolerate) {
				break;
			}
		}
	}

	@Override
	public double train(double[] input, double[] output) {
		forward(input);
		return backprop(output);
	}

	@Override
	public double[] test(double[] input) {
		forward(input);
		return output();
	}


	@Override
	public double last() {
		return error;
	}
	
	@Override
	public void last(double error) {
		this.error = error;
	}

	@Override
	public double[] layer(int layerIndexWhereInputIs0) {
		if(layerIndexWhereInputIs0 == 0) return input.value();
		if(layerIndexWhereInputIs0 > hiddenLayer.size()) return output.value();
		return hiddenLayer.get(layerIndexWhereInputIs0 - 1).value();
	}
	
	public void tolerate(double tolerate) {
		this.tolerate = tolerate;
	}
	public void iteration(int max) {
		this.max = max;
	}
	public void saveTo() {}
}
