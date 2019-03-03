package com.suppresswarnings.corpus.common;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Supplier;

public class ImageFrameSupplier implements Supplier<int[][]> {
	int capacity;
	ArrayBlockingQueue<int[][]> queue;
	
	public ImageFrameSupplier(int size) {
		this.capacity = size;
		this.queue = new ArrayBlockingQueue<int[][]>(capacity);
	}
	
	public void put(int[][] e) {
		try {
			queue.put(e);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
	@Override
	public int[][] get() {
		try {
			int[][] e = queue.take();
			return e;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
