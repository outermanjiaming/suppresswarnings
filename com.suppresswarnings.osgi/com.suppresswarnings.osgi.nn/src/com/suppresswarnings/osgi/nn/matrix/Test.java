package com.suppresswarnings.osgi.nn.matrix;

import org.jblas.DoubleMatrix;

public class Test {

	public static void main(String[] args) {
		int rows = 5;
		int cols = 7;
		DoubleMatrix m = DoubleMatrix.rand(1, cols);
		System.out.println(m.toString("%f", "[", "]", ", ", ";\n"));
		System.out.println();
		DoubleMatrix n = DoubleMatrix.rand(rows, cols);
		System.out.println(n.toString("%f", "[", "]", ", ", ";\n"));
		System.out.println();
		System.out.println(n.transpose().toString("%f", "[", "]", ", ", ";\n"));
		DoubleMatrix multi = m.mmul(n.transpose());
		System.out.println();
		System.out.println(multi.toString("%f", "[", "]", ", ", ";\n"));
		
	}
}
