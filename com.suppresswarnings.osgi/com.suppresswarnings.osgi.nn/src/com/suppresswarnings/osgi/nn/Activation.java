package com.suppresswarnings.osgi.nn;

public enum Activation {
	Sigmoid {
		@Override
		public double f(double x) {
			return 1.0d / (1.0d + Math.exp(-x));
		}

		@Override
		public double d(double x) {
			return x * (1.0d - x);
		}
	},Tanh {
		@Override
		public double f(double x) {
			return Math.tanh(x);
		}

		@Override
		public double d(double x) {
			return 1.0d - (x * x);
		}
	},ReLU {
		double a = 0.0001d;
		@Override
		public double f(double x) {
			return x > 0 ? x : a * x;
		}

		@Override
		public double d(double x) {
			return x > 0 ? 1 : a;
		}
	},SoftPlus {
		@Override
		public double f(double x) {
			return Math.log(1.0d + Math.exp(x));
		}

		@Override
		public double d(double x) {
			return 1.0d / (1.0d + Math.exp(-x));
		}
	};
	public abstract double f(double x);
	public abstract double d(double x);
}
