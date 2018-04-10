package com.suppresswarnings.osgi.neuralnetwork;


import java.io.Serializable;
import java.util.Random;

public class Network implements AI, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7001839902498475151L;
	Random random = new Random();
	Node[] inputs;
	Node[] biases;
	Node[] outputs;
	Node[][] network;
	double error = 0;
	double momentum = 0.8d;
	double learningRate = 0.015d;
	int step = 0;
	int max = 10000000;
	double tolerate = 0.001;
	public Network(int input, int[] hidden, int output, double momentum, double learningRate) {
		this.learningRate = learningRate;
		this.momentum = momentum;
		//input
		inputs = new Node[input];
		for(int index=0;index<input;index++) {
			inputs[index] = NodeImpl.node(Node.TYPE_INPUT, Node.LEVEL_INPUT, index, momentum, learningRate);
		}
		//hidden
		int size = 1 + hidden.length + 1;
		network = new Node[size][];
		network[0] = inputs;
		for(int i=1;i<size - 1;i++) {
			int hiddenSize = hidden[i-1];
			network[i] = new Node[hiddenSize];
			for(int index=0;index<hiddenSize;index++) {
				network[i][index] = NodeImpl.node(Node.TYPE_HIDDEN, i, index, momentum, learningRate);
			}
		}
		//output
		outputs = new Node[output];
		for(int index=0;index<output;index++) {
			outputs[index] = NodeImpl.node(Node.TYPE_OUTPUT, Node.LEVEL_OUTPUT, index, momentum, learningRate);
		}
		network[size - 1] = outputs;
		
		//bias
		biases = new Node[size - 1];
		for(int level=0;level < biases.length;level ++) {
			biases[level] = NodeImpl.node(Node.TYPE_BIAS, level, Node.INDEX_BIAS, momentum, learningRate);
		}
		System.out.println("Connect before train.");
	}
	public void fullConnect(){
		int size = network.length;
		for(int level=0;level<size - 1;level++) {
			int thisSize = network[level].length;
			int nextSize = network[level+1].length;
			for(int n=0;n<nextSize;n++) {
				LinkImpl.link(level, random(), biases[level], network[level+1][n]);
				for(int m=0;m<thisSize;m++) {
					LinkImpl.link(level, random(), network[level][m], network[level+1][n]);
				}
			}
		}
	}

	private double random() {
		double init = 0.12d;
		double r = random.nextDouble();
		r = 2 * r * init - (r / Math.abs(r)) * init;
		return r;
	}
	
	public double[] output(){
		double[] result = new double[outputs.length];
		for(int i=0;i<outputs.length;i++) {
			result[i] = outputs[i].value();
		}
		return result;
	}
	
	public void forward(){
		for(Node node : biases) {
			node.forward();
		}
		for(Node node : inputs) {
			node.forward();
		}
	}
	public double backward(double[] targets){
		double mse = 0;
		for(int i=0;i<targets.length;i++) {
			Node o = outputs[i];
			double target = targets[i];
			double err = target - o.value();
			mse += 0.5d * err * err;
			o.gradient(err);
		}
		return mse;
	}
	public double last(){
		return error;
	}
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Network[(bias=").append(biases.length).append(",error=").append(error).append(",momentum=").append(momentum).append(",learningRate=").append(learningRate).append(") ");
		for(Node[] nodes : network) {
			sb.append(nodes.length).append(" - ");
		}
		sb.setLength(sb.length() - 1);
		sb.setCharAt(sb.length() - 1, ']');

		sb.append("Nodes:\n");
		for(Node[] nodes : network) {
			sb.append(nodes[0]);
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public void expandOutput(int more){
		int newOutputSize = outputs.length + more;
		Node[] output = new Node[newOutputSize];
		for(int i=0;i<newOutputSize;i++) {
			if(i<outputs.length) {
				output[i] = outputs[i];
			} else {
				Node node = NodeImpl.node(Node.TYPE_OUTPUT, Node.LEVEL_OUTPUT, i, momentum, learningRate);
				output[i] = node;
				int level = network.length - 2;
				Node[] lastLayer = network[level];
				for(Node hidden : lastLayer) {
					LinkImpl.link(level, random(), hidden, node);
				}
			}
		}
		int outputLevel = network.length - 1;
		network[outputLevel] = output;
	}
	
	@Override
	public double train(double[] input, double[] target){
		for(int i=0;i<inputs.length;i++) {
			inputs[i].assign(input[i]);
		}
		forward();
		return backward(target);
	}
	
	@Override
	public double[] test(double[] input){
		for(int i=0;i<inputs.length;i++) {
			inputs[i].assign(input[i]);
		}
		forward();
		double[] result = new double[outputs.length];
		for(int i=0;i<outputs.length;i++) {
			Node o = outputs[i];
			result[i] = o.value();
		}
		return result;
	}
	
	@Override
	public void train(double[][] inputs, double[][] outputs) {
		int size = inputs.length;
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
	public void last(double error) {
		this.error = error;
	}
}
