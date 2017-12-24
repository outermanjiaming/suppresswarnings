package com.suppresswarnings.osgi.nn;

public enum LossFunction {
	MSE {
		@Override
		public double f(double[] output, double[] target) {
			double meanSquaredError = 0;
			int m = output.length;
			double sum = 0;
			for(int i=0;i<output.length;i++) {
				double delta = target[i] - output[i];
				sum += delta * delta;
			}
			meanSquaredError = sum / m;
			return meanSquaredError;
		}

		@Override
		public double[] d(double[] output, double[] target) {
			double[] delta = new double[output.length];
			for(int i=0;i<output.length;i++) {
				delta[i] = target[i] - output[i];
			}
			return delta;
		}
	};
	public abstract double f(double[] output, double[] target);
	public abstract double[] d(double[] output, double[] target);
}
