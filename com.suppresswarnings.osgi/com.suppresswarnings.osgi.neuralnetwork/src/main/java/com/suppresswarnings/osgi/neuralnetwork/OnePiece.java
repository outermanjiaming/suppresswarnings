/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.osgi.neuralnetwork;

import java.io.Serializable;
import java.util.Random;

public class OnePiece implements AI, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3135501776068732116L;
	transient double[] dEdyj;
	transient double[] dEdzj;
	transient double[][] dEdWij;
	transient double[] dEdyi;
	transient double[] dEdzi;
	transient double[][] dEdWni;

	double[] x;
	double[] x_1;
	double[] zi;
	double[] yi;
	double[] yi_1;
	double[] zj;
	double[] yj;
	
	int input;
	int hidden;
	int output;
	double[][] Wni;
	double[][] Wij;
	double[][] delta_ij;
	double[][] delta_ni;
	double momentum = 0.88;
	double learningRate = 0.015;
	double error = 0;
	int step = 0;
	int max = 1000000000;
	double tolerate = 0.001;
	double init = 0.12;
	int mini_batch = 1;
	public static final Activation fx = Activation.ReLU;
	public static final String serializeTo = "xor.nn.ser";
	public OnePiece(int input, int hidden, int output) {
		this.x = new double[input];
		this.x_1 = new double[input + 1];
		this.x_1[input] = 1;
		this.Wni = new double[input + 1][hidden];
		this.zi = new double[hidden];
		this.yi = new double[hidden];
		this.yi_1 = new double[hidden + 1];
		this.yi_1[hidden] = 1;
		this.Wij = new double[hidden + 1][output];
		this.zj = new double[output];
		this.yj = new double[output];
		this.input = input;
		this.hidden = hidden;
		this.output = output;
		init();
	}
	public void construct() {
		this.dEdyj = new double[output];
		this.dEdzj = new double[output];
		this.dEdWij = new double[hidden + 1][output];
		this.delta_ij = new double[hidden + 1][output];
		this.dEdyi = new double[hidden + 1];
		this.dEdzi = new double[hidden + 1];
		this.dEdWni = new double[input + 1][hidden];
		this.delta_ni = new double[input + 1][hidden];
	}
	public void init() {
		Random r = new Random();
		System.out.println("=== init ===");
		for(int n=0;n<input+1;n++) {
			for(int i=0;i<hidden;i++) {
				double rand = r.nextDouble();
				this.Wni[n][i] = rand * 2 * init - (rand / Math.abs(rand)) * init;
			}
		}
		for(int i=0;i<hidden+1;i++) {
			for(int j=0;j<output;j++) {
				double rand = r.nextDouble();
				this.Wij[i][j] = rand * 2 * init - (rand / Math.abs(rand)) * init;
			}
		}
	}
	
	@Override
	public void train(double[][] inputs, double[][] outputs) {
		int size = inputs.length;
		while(step ++ < max) {
			double err = 0;
			for(int i=0;i<size;i++) {
				double[] in = inputs[i];
				double[] out = outputs[i];
				forward(in);
				err += backprop(out);
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
	public double[] test(double[] x) {
		forward(x);
		return yj;
	}
	
	public void forward(double[] x) {
		System.arraycopy(x, 0, x_1, 0, input);
		for(int i=0;i<hidden;i++) {
			double temp = 0;
			for(int n=0;n<input+1;n++) {
				temp += x_1[n] * Wni[n][i];
			}
			zi[i] = temp;
		}
		
		for(int i=0;i<hidden;i++) {
			yi[i] = fx.f(zi[i]);
		}
		
		System.arraycopy(yi, 0, yi_1, 0, hidden);
		for(int j=0;j<output;j++) {
			double temp = 0;
			for(int i=0;i<hidden+1;i++) {
				temp += yi_1[i] * Wij[i][j];
			}
			zj[j] = temp;
		}
		for(int j=0;j<output;j++) {
			yj[j] = fx.f(zj[j]);
		}
	}
	public double backprop(double[] t) {
		double error = LossFunction.MSE.f(yj, t);
		
		for(int j=0;j<output;j++) {
			dEdyj[j] = (t[j] - yj[j]);
		}
		
		for(int j=0;j<output;j++) {
			dEdzj[j] = dEdyj[j] * fx.d(yj[j]);
		}
		
		for(int i=0;i<hidden+1;i++) {
			for(int j=0;j<output;j++) {
				//dzjdWij = yi
				dEdWij[i][j] = dEdzj[j] * yi_1[i];
			}
		}
		
		for(int i=0;i<hidden+1;i++) {
			double temp = 0;
			for(int j=0;j<output;j++) {
				temp += Wij[i][j] * dEdzj[j];
			}
			dEdyi[i] = temp;
		}
		
		for(int i=0;i<hidden;i++) {
			dEdzi[i] = dEdyi[i] * fx.d(yi_1[i]);
		}
		
		for(int n=0;n<input+1;n++) {
			for(int i=0;i<hidden;i++) {
				dEdWni[n][i] = dEdzi[i] * x_1[n];
			}
		}
		//update
		
		for(int i=0;i<hidden+1;i++) {
			for(int j=0;j<output;j++) {
				double delta = momentum * delta_ij[i][j] + learningRate * dEdWij[i][j];
				delta_ij[i][j] = delta;
				Wij[i][j] += delta;
			}
		}
		
		for(int n=0;n<input+1;n++) {
			for(int i=0;i<hidden;i++) {
				double delta = momentum * delta_ni[n][i] + learningRate * dEdWni[n][i];
				delta_ni[n][i] = delta;
				Wni[n][i] += delta;
			}
		}
		return error;
	}
	
	public static void main(String[] args) {
		OnePiece one = (OnePiece) Util.deserialize(serializeTo);//new OnePiece(2, 5, 2);//
		one.construct();
		double[][] inputs = new double[4][2];
		double[][] outputs = new double[4][2];
		inputs[0] = new double[]{0,0};
		inputs[1] = new double[]{0,1}; 
		inputs[2] = new double[]{1,0}; 
		inputs[3] = new double[]{1,1}; 
		outputs[0] = new double[]{1,0};
		outputs[1] = new double[]{0,1}; 
		outputs[2] = new double[]{0,1}; 
		outputs[3] = new double[]{1,0}; 
		one.train(inputs, outputs);
		Util.serialize(one, serializeTo);
	}
	
	@Override
	public double last() {
		return error;
	}
	
	@Override
	public void last(double error) {
		this.error = error;
	}
}
