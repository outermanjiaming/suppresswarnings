package com.suppresswarnings.osgi.nn.parellel;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ElementModifyTask extends RecursiveAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8347093685405424641L;
	final int threshold = 10;
	final long[] array;
	final int low,high;
	public ElementModifyTask(long[] array){
		this.array = array;
		this.low = 0;
		this.high = array.length;
	}
	
	public ElementModifyTask(long[] array, int lo, int hi) {
		this.array = array;
		this.low = lo;
		this.high = hi;
	}

	@Override
	protected void compute() {
		if(high - low < threshold) {
			for(int i=low;i<high;i++) {
				array[i] %= 3;
			}
		} else {
			int mid = (low + high) >>> 1;
			ElementModifyTask left = new ElementModifyTask(array, low, mid);
			ElementModifyTask right= new ElementModifyTask(array, mid, high);
			invokeAll(left, right);
		}
	}
	
	public static void main(String[] args) {
		long[] init = new long[10000];
		Random rand = new Random(1020421);
		for(int i=0;i<init.length;i++) {
			init[i] = rand.nextInt(1000);
		}
		System.out.println(Arrays.toString(init));
		ElementModifyTask task = new ElementModifyTask(init);
		ForkJoinPool pool = new ForkJoinPool();
		pool.invoke(task);
		System.out.println(Arrays.toString(init));
	}

}
