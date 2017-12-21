package com.suppresswarnings.osgi.nn.rnn;

import java.io.Serializable;
import java.util.Random;

import com.suppresswarnings.osgi.nn.Char2Vec;
import com.suppresswarnings.osgi.nn.Node;
import com.suppresswarnings.osgi.nn.Util;
import com.suppresswarnings.osgi.nn.impl.LinkImpl;
import com.suppresswarnings.osgi.nn.impl.NodeImpl;

public class RNN implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4542054691282591123L;
	Random random = new Random();
	Node[] biases;
	Node[] input;
	Node[] hidden;
	Node[] hiddent;
	Node[] hxWhh;
	Node[] output;
	Node[] target;
	int t;
	double lastErr = Double.MAX_VALUE;
	double error;
	public RNN(int inputSize, int hiddenSize, int t, double momentum, double learningRate){
		this.input = new Node[inputSize];
		for(int i=0;i<inputSize;i++) {
			input[i] = NodeImpl.node(Node.TYPE_INPUT, Node.LEVEL_INPUT, i, momentum, learningRate);
		}
		this.hidden = new Node[hiddenSize];
		for(int i=0;i<hiddenSize;i++) {
			hidden[i] = NodeImpl.node(Node.TYPE_HIDDEN, 1, i, momentum, learningRate);
		}
		this.hiddent = new Node[hiddenSize];
		for(int i=0;i<hiddenSize;i++) {
			hiddent[i] = NodeImpl.node(Node.TYPE_HIDDEN, 1, i, momentum, learningRate);
		}
		this.hxWhh = new Node[hiddenSize];
		for(int i=0;i<hiddenSize;i++) {
			hxWhh[i] = NodeImpl.node(Node.TYPE_BIAS, 1, i, momentum, learningRate);
		}
		this.output = new Node[inputSize];
		for(int i=0;i<inputSize;i++) {
			output[i] = NodeImpl.node(Node.TYPE_OUTPUT, Node.LEVEL_OUTPUT, i, momentum, learningRate);
		}
		this.target = new Node[inputSize];
		for(int i=0;i<inputSize;i++) {
			target[i] = NodeImpl.node(Node.TYPE_OUTPUT, Node.LEVEL_OUTPUT, i, momentum, learningRate);
		}
		this.t = t;
		this.biases = new Node[3];
		for(int i=0;i<biases.length;i++) {
			biases[i] = NodeImpl.node(Node.TYPE_BIAS, i, Node.INDEX_BIAS, momentum, learningRate);
		}
		//hidden bias -> hidden
		for(int j=0;j<hiddenSize;j++) {
			LinkImpl.link(0, random(), biases[0], hidden[j]);
		}
		//hidden bias -> hiddent
		for(int j=0;j<hiddenSize;j++) {
			LinkImpl.link(1, random(), biases[1], hiddent[j]);
		}
		//output bias -> output
		for(int j=0;j<inputSize;j++) {
			LinkImpl.link(2, random(), biases[2], output[j]);
		}
		//x -> h
		for(int i=0;i<inputSize;i++) {
			for(int j=0;j<hiddenSize;j++) {
				LinkImpl.link(0, random(), input[i], hidden[j]);
			}
		}
		
		//h(t-1) -> h(t)
		for(int i=0;i<hiddenSize;i++) {
			for(int j=0;j<hiddenSize;j++) {
				LinkImpl.link(1, random(), hidden[i], hiddent[j]);
			}
		}
		
		//hiddent == hxWhh
		for(int i=0;i<hiddenSize;i++) {
			LinkImpl.link(0, 1.0, hxWhh[i], hidden[i]);
		}
		
		//h(t) -> y
		for(int i=0;i<hiddenSize;i++) {
			for(int j=0;j<inputSize;j++) {
				LinkImpl.link(1, random(), hiddent[i], output[j]);
			}
		}
		
	}
	
	private double random() {
		double init = 0.12d;
		double r = random.nextDouble();
		r = 2 * r * init - (r / Math.abs(r)) * init;
		return r;
	}
	
	public void train(double[] x, double[] y) {
		for(int i=0;i<x.length;i++) {
			input[i].assign(x[i]);
		}
		forward();
		
		for(int i=0;i<y.length;i++) {
			target[i].assign(y[i]);
		}
		backward();
	}
	public void forward(){
		for(Node node : biases) {
			node.forward();
		}
		for(Node node : hidden) {
			node.propagate();
		}
		for(int i=0;i<hiddent.length;i++) {
			Node node = hiddent[i];
			node.receive();
			double hixWhh = node.value();
			hxWhh[i].assign(hixWhh);
		}
		for(Node node : hxWhh) {
			node.forward();
		}
		for(Node node : input) {
			node.forward();
		}
	}
	public void backward(){
		for(int i=0;i<target.length;i++) {
			Node o = output[i];
			Node t = target[i];
			double err = t.value() - o.value();
			error += 0.5d * err * err;
			o.gradient(err);
		}
	}
	
	public double[] test(double[] x){
		for(int i=0;i<input.length;i++) {
			input[i].assign(x[i]);
		}
		forward();
		double[] result = new double[output.length];
		for(int i=0;i<output.length;i++) {
			Node o = output[i];
			result[i] = o.value();
		}
		return result;
	}
	
	public void clear() {
		if(error != 0) lastErr = error;
		error = 0;	
	}
	public double error() {
		return error;
	}
	public static void train(String[] args) {
		String hello = "char[] targets = world.toCharArray()n";
		String world = "System.out.println(toVec.toString());";
		char[] chars = hello.toCharArray();
		char[] targets = world.toCharArray();
		Char2Vec toVec = new Char2Vec();
		toVec.feed(hello).feed(world).build();
		System.out.println(toVec.toString());
		
		RNN rnn = new RNN(toVec.size(), toVec.size(), targets.length, 0.8, 0.015);
		
		for(int n=0;n<100000;n++) {
			rnn.clear();
			for(int i=0;i<targets.length;i++) {
				char x = chars[i];
				char y = targets[i];
				double[] vec = toVec.onehot(x);
				double[] tar = toVec.onehot(y);
				rnn.train(vec, tar);
			}
			
			System.out.println("error: "+rnn.error());
			if(rnn.error() < 0.001) break;
		}
		for(int i=0;i<targets.length;i++) {
			char x = chars[i];
			char y = targets[i];
			double[] vec = toVec.onehot(x);
			double[] out = rnn.test(vec);
			int index = Util.argmax(out);
			char o = toVec.get(index);
			System.out.print(x + " -> " + y + " == " + o + " ? " + (y == o));
			System.out.println();
		}
		Util.serialize(rnn, "D:/files/tmp/rnn.ser");
	}
	
	public static void main(String[] args) {
		String hello = "char[] targets = world.toCharArray()n";
		String world = "System.out.println(toVec.toString());";
		char[] targets = world.toCharArray();
		Char2Vec toVec = new Char2Vec();
		toVec.feed(hello).feed(world).build();
		System.out.println(toVec.toString());
		RNN rnn = (RNN) Util.deserialize("D:/files/tmp/rnn.ser");
		String test = "ets = world.toCharArray()nchar[] targ";
		char[] tests = test.toCharArray();
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<targets.length;i++) {
			char x = tests[i];
			double[] vec = toVec.onehot(x);
			double[] out = rnn.test(vec);
			int index = Util.argmax(out);
			char o = toVec.get(index);
			sb.append(o);
		}
		System.out.println(hello);
		System.out.println(world);
		System.out.println(test);
		System.out.println(sb.toString());
	}
}
