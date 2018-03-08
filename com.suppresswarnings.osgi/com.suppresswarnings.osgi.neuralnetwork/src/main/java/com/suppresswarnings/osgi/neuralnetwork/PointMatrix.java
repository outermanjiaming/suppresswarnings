package com.suppresswarnings.osgi.neuralnetwork;

import java.io.Serializable;

public class PointMatrix implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8032233949525087698L;
	Point[][] matrix;
	int width;
	int height;
	double max;
	public static final int TYPE_CONVOLUTION = 0;
	public static final int TYPE_MAXPOOLING  = 1;
	public PointMatrix(int width, int height, double max) {
		this.width  = width;
		this.height = height;
		this.max    = max;
		this.matrix = new Point[width][height];
		for(int i=0;i<width;i++) {
			for(int j=0;j<height;j++) {
				this.matrix[i][j] = new Point();
			}
		}
	}
	public PointMatrix(Point[][] matrix, double max) {
		this.width  = matrix.length;
		this.height = matrix[0].length;
		this.matrix = matrix;
		this.max    = max;
	}
	public double[][] normalizeAndTake() {
		double[][] value = new double[this.width][this.height];
		for(int i=0;i<width;i++) {
			for(int j=0;j<height;j++) {
				value[i][j] = this.matrix[i][j].value() / this.max;
				this.matrix[i][j].set(0);
			}
		}
		return value;
	}
	public double[][] take(){
		double[][] value = new double[this.width][this.height];
		for(int i=0;i<width;i++) {
			for(int j=0;j<height;j++) {
				value[i][j] = this.matrix[i][j].value();
				this.matrix[i][j].set(0);
			}
		}
		return value;
	}
	public void feedMatrix(double[][] matrix, int type) {
		int w = matrix.length ;
		int h = matrix[0].length;
		for(int i=0;i<w;i++) {
			for(int j=0;j<h;j++) {
				this.matrix[i][j].set(matrix[i][j]);
				if(type == TYPE_CONVOLUTION) this.matrix[i][j].convolution();
				else if(type == TYPE_MAXPOOLING) this.matrix[i][j].maxpooling();
				else System.err.println("Wrong Type: " + type);
			}
		}
	}
	
	public PointMatrix viewOf(double[][] mask, int step) {
		int frameW = mask.length;
		int frameH = mask[0].length;
		int w = (this.width - frameW + step - 1) / step + 1;
		int h = (this.height- frameH + step - 1) / step + 1;
		Point[][] view = new Point[w][h];
		int x = 0;
		for(int i=0;i<w;i++) {
			int y = 0;
			int stopX = Math.min(x + frameW, this.width);
			for(int j=0;j<h;j++) {
				Point v = new Point();
				view[i][j] = v;
				int stopY = Math.min(y + frameH, this.height);
				for(int m =x;m<stopX;m++) {
					for(int n=y;n<stopY;n++) {
						this.matrix[m][n].out(mask[m-x][n-y], v);//
					}
				}
				y += step;
			}
			x += step;
		}
		double max = 0;
		for(int i=0;i<frameW;i++) {
			for(int j=0;j<frameH;j++) {
				if(max < mask[i][j]) max = mask[i][j];
			}
		}
		return new PointMatrix(view, this.max * frameW * frameH * max);
	}

	
	
}
