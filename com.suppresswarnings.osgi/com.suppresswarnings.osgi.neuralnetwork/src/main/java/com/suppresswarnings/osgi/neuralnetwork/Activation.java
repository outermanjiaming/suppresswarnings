package com.suppresswarnings.osgi.neuralnetwork;


public enum Activation {
	Sigmoid {
		@Override
		public double f(double x) {
			return 1.0d / (1.0d + Math.exp(-x));
		}

		@Override
		public double d(double y) {
			return y * (1.0d - y);
		}
	},Tanh {
		@Override
		public double f(double x) {
			return Math.tanh(x);
		}

		@Override
		public double d(double y) {
			return 1.0d - (y * y);
		}
	},ReLU {
		double a = 0.0001d;
		@Override
		public double f(double x) {
			return x > 0 ? x : a * x;
		}

		@Override
		public double d(double y) {
			return y > 0 ? 1 : a;
		}
	},SoftPlus {
		@Override
		public double f(double x) {
			return Math.log(1.0d + Math.exp(x));
		}

		@Override
		public double d(double y) {
			return 1.0d / (1.0d + Math.exp(-y));
		}
	};
	public abstract double f(double x);
	public abstract double d(double y);
}
