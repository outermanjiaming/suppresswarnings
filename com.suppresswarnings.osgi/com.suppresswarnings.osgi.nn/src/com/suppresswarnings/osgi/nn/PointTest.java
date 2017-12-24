package com.suppresswarnings.osgi.nn;

import java.util.ArrayList;
import java.util.List;

public class PointTest {

	public static void main(String[] args) {
		System.out.println(Activation.Sigmoid.f(Activation.Sigmoid.f(0.2)*0.4+0.2));
	}
	public static void main2(String[] args) {
		PointMatrix pm = new PointMatrix(11, 11, 1);
		List<PointMatrix> convolutionLayer = new ArrayList<PointMatrix>();
		List<PointMatrix> maxpoolLayer = new ArrayList<PointMatrix>();
		int kernelSize = 3;
		double[][] pool = Util.ones(2, 2);
		double[][] matrix = Util.random(11, 11);
		for(int i=0;i<kernelSize;i++) {
			PointMatrix conv = pm.viewOf(Util.zerone(3, 3), 1);
			convolutionLayer.add(conv);
			maxpoolLayer.add(conv.viewOf(pool, 2));
		}
		
		
		pm.feedMatrix(matrix, PointMatrix.TYPE_CONVOLUTION);
		for(PointMatrix view : convolutionLayer) {
			view.feedMatrix(view.normalizeAndTake(), PointMatrix.TYPE_MAXPOOLING);
		}
		
		for(PointMatrix maxPool : maxpoolLayer) {
			Util.print(maxPool.take());
		}
	}
}
