/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.common;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Thinker {
	private String serializeTo;
	private AI nn;
	public Thinker(){
		this.serializeTo = "/Users/lijiaming/Learn/meyou/20190125.nn";
	}
	public Thinker(String nn) {
		this.serializeTo = nn;
	}
	class Piece {
		int[][] d;
		int x;
		int y;
		
		public Piece(int[][] data, int x, int y) {
			this.d = data;
			this.x = x;
			this.y = y;
		}
		public int[][] getD() {
			return d;
		}
		public void setD(int[][] data) {
			this.d = data;
		}
		public int getX() {
			return x;
		}
		public int getY() {
			return y;
		}
		
	}
	class Supply implements Spliterator<Piece> {
		int startx = 0;
		int starty = 0;
		int width;
		int height;
		int stepx;
		int stepy;
		int w;
		int h;
		int[][] D;
		transient int size;
		public Supply(int[][] D, int width, int height, int stepx, int stepy, int startx, int starty) {
			this.D = D;
			this.width = width;
			this.height = height;
			this.stepx = stepx;
			this.stepy = stepy;
			this.startx = startx;
			this.starty = starty;
			this.w = D.length;
			this.h = D[0].length;
			int m = ((w - width) / stepx + 1);
			int n = ((h - height) / stepy + 1);
			this.size = m * n;
		}
		public Supply(int[][] D, int width, int height, int stepx, int stepy) {
			this.D = D;
			this.width = width;
			this.height = height;
			this.stepx = stepx;
			this.stepy = stepy;
			this.w = D.length;
			this.h = D[0].length;
			int m = ((w - width) / stepx + 1);
			int n = ((h - height) / stepy + 1);
			this.size = m * n;
		}
		
		@Override
		public boolean tryAdvance(Consumer<? super Piece> action) {
			if(size <= 0) return false;
			startx += stepx;
			if(startx + width > w) {
				startx = 0;
				starty += stepy;
				if(starty + height > h) {
					starty =0;
				}
			}
			int[][] frame = Util.frame(width, height, startx, starty, D);
			size --;
			Piece p = new Piece(frame, startx, starty);
			action.accept(p);
			return true;
		}

		@Override
		public Spliterator<Piece> trySplit() {
			return null;
		}

		@Override
		public long estimateSize() {
			return size;
		}

		@Override
		public int characteristics() {
			return 0;
		}
		
	}
	class D {
		double[] x;
		double[] y;
		public D(List<Double> list, double[] target) {
			x = new double[list.size()];
			for(int i=0;i<list.size();i++) {
				x[i] = list.get(i);
			}
			y = target;
		}
	}
	class Capture implements Function<Piece, List<Double>>{

		@Override
		public List<Double> apply(Piece d) {
			int[][] t = d.d;
			List<Double> result = new ArrayList<Double>();
			int w = t.length;
			int h = t[0].length;
			int size = w * h;
			double sum = 0;
			int r = 0;
			int g = 0;
			int b = 0;
			for(int i=0;i<w;i++) {
				for(int j=0;j<h;j++) {
					int color = t[i][j];
					Color pixel = new Color(color);
					if(pixel.getRed() > 240) r ++;
					if(pixel.getGreen() > 240) g ++;
					if(pixel.getBlue() > 240) b ++;
					double rgb = pixel.getRed()*0.299 + pixel.getGreen()*0.587 + pixel.getBlue()*0.114;
					sum += rgb / 255;
				}
			}
			result.add(sum / size);
			result.add(r * 1.0 / size);
			result.add(g * 1.0 / size);
			result.add(b * 1.0 / size);
			return result;
		}
	}
	
	public int decide(BufferedImage bi) {
		long t1 = System.currentTimeMillis();
		List<List<Double>> lists = slide(readImage(bi));
		long t2 = System.currentTimeMillis();
		List<Double> arr = lists.stream().flatMap(l -> l.stream()).collect(Collectors.toList());
		long t3 = System.currentTimeMillis();
		D d = new D(arr, new double[]{0,0});
		long t4 = System.currentTimeMillis();
		if(nn==null) nn = (AI)Util.deserialize(serializeTo);
		long t5 = System.currentTimeMillis();
		double[] y = nn.test(d.x);
		long t6 = System.currentTimeMillis();
		System.out.println(String.format("++++++++++++++++++++++++++++++++++++++\nslide:%s\tflatMap:%s\tserialize:%s\ttest:%s\t++++++++++++++++++++++++++++++++++++++", (t2-t1), (t3-t2), (t5-t4),(t6-t5)));
		return Util.argmax(y);
	}
	public static int[][] readImage(BufferedImage bi) {
		int width = bi.getWidth();
		int height = bi.getHeight();
		int[][] pixels = new int[width][height];
		for(int x =0;x <width;x ++) {
			for(int y=0;y<height;y++) {
				pixels[x][y] = bi.getRGB(x, y);
			}
		}
		return pixels;
	}
	public List<List<Double>> slide(int[][] D) {
		List<List<Double>> list = new ArrayList<List<Double>>();
		StreamSupport.stream(new Supply(D, 640,480,10,10), false)
		.map(f100x240 -> new Supply(f100x240.getD(), 20, 20, 20, 20))
		.forEach(supply20x20 -> {
			List<Double> e = StreamSupport.stream(supply20x20, false)
			.map(f20x20 -> new Supply(f20x20.getD(), 10, 10, 10, 10))
			.map(supply10x10 -> StreamSupport.stream(supply10x10, false)
					.map(new Capture())
					.flatMap(lst -> lst.stream())
					.collect(Collectors.toList())
				)
			.flatMap(lst -> lst.stream())
			.collect(Collectors.toList());
			list.add(e);
		});
		return list;
	}
	
	public List<List<Double>> slide(File file) {
		int[][] D = Util.readImage(file);
		return slide(D);
	}
	
	public void decide() throws IOException{
		Files.list(Paths.get("/Users/lijiaming/Learn/meyou/test/"))
		.peek(path -> System.out.println(path))
		.filter(path -> path.toString().endsWith("jpg"))
		.map(path -> slide(path.toFile()))
		.flatMap(list -> list.stream())
		.forEach(arr -> {
			D d = new D(arr, new double[]{0,0});
			AI nn = (AI)Util.deserialize(serializeTo);
			double[] y = nn.test(d.x);
			if(y[0] > y[1]) {
				System.out.println("it's me");
			} else {
				System.out.println("that's you");
			}
		});
	}
	public void learn() throws IOException{
		String[] yesno = {"/Users/lijiaming/Learn/meyou/me/", "/Users/lijiaming/Learn/meyou/you/", "/Users/lijiaming/Learn/meyou/none/"};
		double[] yes   = {1,0,0};
		double[] no    = {0,1,0};
		double[] none  = {0,0,1};
		int epochs = 10000;
		System.out.println("Start to learn us");
		List<D> all = new ArrayList<D>();
		
		Files.list(Paths.get(yesno[0]))
		.peek(file -> System.out.println(file))
		.filter(path -> path.toString().endsWith("jpg"))
		.map(path -> slide(path.toFile()))
		.flatMap(list -> list.stream())
		.forEach(arr -> {
			D d = new D(arr, yes);
			all.add(d);
		});
		
		Files.list(Paths.get(yesno[1]))
		.peek(file -> System.out.println(file))
		.filter(path -> path.toString().endsWith("jpg"))
		.map(path -> slide(path.toFile()))
		.flatMap(list -> list.stream())
		.forEach(arr -> {
			D d = new D(arr, no);
			all.add(d);
		});
		
		Files.list(Paths.get(yesno[2]))
		.peek(file -> System.out.println(file))
		.filter(path -> path.toString().endsWith("jpg"))
		.map(path -> slide(path.toFile()))
		.flatMap(list -> list.stream())
		.forEach(arr -> {
			D d = new D(arr, none);
			all.add(d);
		});
		
		D d0 = all.get(0);
		
		Collections.shuffle(all);
		File file = new File(serializeTo);
		AI nn = null;
		if(file.exists()) {
			nn = (AI)Util.deserialize(serializeTo);
		} else {
			NN temp = new NN(d0.x.length, d0.y.length, new int[] {20, 10});
			nn = temp;
		}
		System.out.println(nn.toString());
		long start = System.currentTimeMillis();
		for(int n=0;n<epochs;n++) {
			double error = 0;
			for(int i=0;i<all.size();i++) {
				D D = all.get(i);
				error += nn.train(D.x, D.y);
			}
			long time = System.currentTimeMillis();
			
			System.out.println(serializeTo + new Date(time) + " [" +(time - start) /1000 + "s] " + all.size() + "\t" + n + "\tTotal Error: " + error);
			if(error < 1e-4) break;
			if(n % 10 == 0) {
				if(new File(serializeTo).exists()){
					Files.copy(Paths.get(serializeTo), Paths.get(serializeTo+".old"), StandardCopyOption.REPLACE_EXISTING);
				}
				Util.serialize(nn, serializeTo);
			}
		}
		
		Util.serialize(nn, serializeTo);
		
		
	}
	
	public static void main(String[] args) throws IOException {
		Thinker thinker = new Thinker();
		thinker.learn();
	}
	
}
