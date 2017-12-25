package com.suppresswarnings.osgi.nn.impl;

import java.text.DecimalFormat;
import java.util.Random;

import com.suppresswarnings.osgi.nn.Activation;
import com.suppresswarnings.osgi.nn.Util;

public class BP {
	double[] x = new double[3];
	double[] hi = new double[3];
	double[] y = new double[1];
	double[][] Wxh;
	double[][] Why;
	double theta = 0.015;
	Random random = new Random();
	DecimalFormat format = new DecimalFormat("0.0000");
	public void init(){
		Wxh = Util.random(3, 3);
		Why = Util.random(3, 1);
	}
	
	public void train(double[] input, double[] target) {
		x[0] = 1.0;
		x[1] = input[0];
		x[2] = input[1];
		
		hi[0] = 1.0;
		for(int j =0;j<hi.length;j++) {
			double sum =0;
			for(int i=0;i<x.length;i++) {
				sum += x[i] * Wxh[i][j];
			}
			hi[j] = Activation.Sigmoid.f(sum);
		}
		
		for(int j=0;j<y.length;j++) {
			double sum = 0;
			for(int i=0;i<hi.length;i++) {
				sum += hi[i] * Why[i][j];
			}
			y[j] = Activation.Sigmoid.f(sum);
		}
		
		double E = 0;
		double[] dEdY = new double[y.length];
		for(int j=0;j<y.length;j++) {
			dEdY[j] = (target[j] - y[j]);
			E += dEdY[j] * dEdY[j];
		}
		E /= 0.5;
		
		double[] dEdZ = new double[y.length];
		for(int j=0;j<y.length;j++) {
			dEdZ[j] = Activation.Sigmoid.d(y[j]) * dEdY[j];
		}
		
		double[] dEdYi = new double[hi.length];
		for(int i=0;i<hi.length;i++) {
			double sum =0;
			for(int j=0;j<y.length;j++) {
				sum += Why[i][j] * dEdZ[j];
			}
			dEdYi[i] = sum;
		}
		
		double[][] dEdWij = new double[hi.length][y.length];
		for(int i=0;i<hi.length;i++) {
			for(int j=0;j<y.length;j++) {
				dEdWij[i][j] = hi[i] * dEdZ[j];
				Why[i][j] += theta*dEdWij[i][j];
			}
		}
		
		double[] dEdZh = new double[hi.length];
		for(int j=0;j<hi.length;j++) {
			dEdZh[j] = Activation.Sigmoid.d(hi[j]) * dEdYi[j];
		}
		
		
		double[][] dEdWx = new double[x.length][hi.length];
		for(int i=0;i<x.length;i++) {
			for(int j=0;j<hi.length;j++) {
				dEdWx[i][j] = x[i] * dEdZh[j];
				Wxh[i][j] += theta * dEdWx[i][j];
			}
		}
		System.out.println(format.format(E) + "   .");
	}
	
	public static void main(String[] args) {
		BP bp = new BP();
		bp.init();
		double[][] inputs = {{0,0},{1,0},{1,1},{0,1}};
		double[][] outputs = {{0},{1},{0},{1}};
		for(int n=0;n<2000;n++) {
			for(int i=0;i<inputs.length;i++) {
				double[] input = inputs[i];
				double[] target = outputs[i];
				bp.train(input, target);
			}
		}
	}
}
