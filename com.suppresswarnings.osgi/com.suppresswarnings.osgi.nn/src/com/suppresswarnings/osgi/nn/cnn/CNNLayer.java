package com.suppresswarnings.osgi.nn.cnn;

import java.io.Serializable;
import java.util.Arrays;

public class CNNLayer implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8462749082153436320L;
	ConvolutionLayer[] convolutionLayers;
	MaxPoolLayer maxPoolLayer;
	NormalizeLayer normalizeLayer;
	boolean normalize;
	boolean same;
	public CNNLayer(){}
	public CNNLayer(int convolutionLayerSize, int convolutionLayerW, int convolutionLayerH, int convolutionLayerStep, boolean same, int maxPoolLayerW, int maxPoolLayerH, int maxPoolLayerStep, boolean normalize) {
		this.convolutionLayers = new ConvolutionLayer[convolutionLayerSize];
		for(int i=0;i<convolutionLayerSize;i++) {
			convolutionLayers[i] = new ConvolutionLayer(convolutionLayerW, convolutionLayerH, convolutionLayerStep);
		}
		this.maxPoolLayer = new MaxPoolLayer(maxPoolLayerW, maxPoolLayerH, maxPoolLayerStep);
		double normalizeLayerMax = convolutionLayerW * convolutionLayerH;
		this.normalize = normalize;
		this.same = same;
		this.normalizeLayer = normalize ? new NormalizeLayer(normalizeLayerMax) : null;
	}
	
	public double[][][] conv(double[][] image) {
		double[][][] results = new double[convolutionLayers.length][][];
		for(int i=0;i<convolutionLayers.length;i++){
			ConvolutionLayer convolutionLayer = convolutionLayers[i];
			double[][] conv = convolutionLayer.conv(image, same);
			double[][] result = maxPoolLayer.pool(conv);
			if(normalize) normalizeLayer.normalize(result);
			results[i] = result;
		}
		System.out.println("[" + image.length + " x " + image[0].length + "] -> [" + results[0].length + " x " + results[0][0].length + "]");
		return results;
	}
	@Override
	public String toString() {
		return "CNNLayer [convolutionLayers=" + Arrays.toString(convolutionLayers) + ", maxPoolLayer=" + maxPoolLayer
				+ ", normalizeLayer=" + normalizeLayer + ", normalize=" + normalize + "]";
	}
	
}
