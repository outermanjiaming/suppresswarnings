package com.suppresswarnings.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.imageio.ImageIO;

import com.suppresswarnings.osgi.nn.Network;
import com.suppresswarnings.osgi.nn.Util;
class Piece {
	int[][] data;
	int x;
	int y;
	
	public Piece(int[][] data, int x, int y) {
		this.data = data;
		this.x = x;
		this.y = y;
	}
	public int[][] getData() {
		return data;
	}
	public void setData(int[][] data) {
		this.data = data;
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
	int width = 100;
	int height = 240;
	int stepx;
	int stepy;
	int w;
	int h;
	int[][] data;
	transient int size;

	public Supply(int[][] data, int width, int height, int stepx, int stepy) {
		this.data = data;
		this.width = width;
		this.height = height;
		this.stepx = stepx;
		this.stepy = stepy;
		this.w = data.length;
		this.h = data[0].length;
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
		int[][] frame = AutoJump.frame(width, height, startx, starty, data);
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

class Data{
	public final static double[] yes = {1,0};
	public final static double[] no = {0,1};
	public boolean yes(double[] data){
		double a = Math.sqrt(Math.pow(data[0] - yes[0], 2) + Math.pow(data[1] - yes[1], 2));
		double b = Math.sqrt(Math.pow(data[0] - no[0], 2) + Math.pow(data[1] - no[1], 2));
		//yes error < no error
		return a < b;
	}
	double[] x;
	double[] y;
	public Data(List<Integer> list, double[] target) {
		x = new double[list.size()];
		for(int i=0;i<list.size();i++) {
			int color = list.get(i);
			Color pixel = new Color(color);
			double rgb = pixel.getRed()*0.299 + pixel.getGreen()*0.587 + pixel.getBlue()*0.114;
			x[i] = rgb / 255;
		}
		y = target;
	}
}

public class AutoJump {
	public static final String serializeTo = "D:/tmp/jump/jump-0131.7.ser";
	public static final String adb = "C:/Users/lijiaming/AppData/Local/Android/Sdk/platform-tools/adb.exe";
	public static final double confidence = 0.9;
	public static final int epochs = 10000;
	public static Network nn;
	
	public static void main(String[] args) throws Exception {
		autojump();
	}
	
	public static void autojump() throws Exception {
		File nnfile = new File(serializeTo);
		long old = nnfile.lastModified();
		nn = (Network) Util.deserialize(serializeTo);
		int times = 0;
		while(true) {
			times ++;
			long wait = 1200;
			TimeUnit.MILLISECONDS.sleep(wait);
			File newfile = new File(serializeTo);
			if(old < newfile.lastModified()){
				System.out.println("[update] ------------------------------------------------------------------------ " + old + " --> " + newfile.lastModified());
				old = newfile.lastModified();
				nn = (Network)Util.deserialize(serializeTo);
			}
			
			String name = "p" + System.currentTimeMillis() + ".png";
			String shot = "/sdcard/" + name;
			System.out.println(wait + "\t[jump] =================="+times+"================ start: " + name);
			
			Process screenshot = Runtime.getRuntime().exec(new String[]{adb, "shell", "/system/bin/screencap", "-p", shot});
			screenshot.waitFor();
			Process pullimage = Runtime.getRuntime().exec(new String[]{adb, "pull", shot, name});
			pullimage.waitFor();
			String file = name;
			TimeUnit.MILLISECONDS.sleep(20);
			int[][] data = readImage(new File(file));
			final int[] jumper = new int[2];
			int[] block = new int[2];
			downstair(data, 4, block);
			StreamSupport.stream(new Supply(data, 100, 240, 10, 10), false)
			.filter(f100x240 -> {
				if(f100x240.getY() + 100 < block[0]) return false;
				
				List<Integer> list = StreamSupport.stream(new Supply(f100x240.getData(), 20, 20, 20, 20), false)
				.map(f20x20 -> new Supply(f20x20.getData(), 10, 10, 10, 10))
				.map(supply -> StreamSupport.stream(supply, false)
						.map(f10x10 -> {
							int[][] data10x10 = f10x10.getData();
							int sum = 0;
							for(int i=0;i<data10x10.length;i++) {
								for(int j=0;j<data10x10[i].length;j++) {
									sum += data10x10[i][j];
								}
							}
							return sum/100;
						})
						.collect(Collectors.toList()))
						.flatMap(f4x4 -> f4x4.stream())
						.collect(Collectors.toList());
				Data input = new Data(list, Data.yes);
				double[] y = nn.test(input.x);
				if(input.yes(y) && jumper[0] == 0) {
					jumper[0] =f100x240.x;
					jumper[1] =f100x240.y;
					System.out.println("jumper found:("+ f100x240.x + "," + f100x240.y + ") == " +  y[0]);
					return true;
				}
				return false;
			})
			.findFirst();
			long duration = function(jumper, block);
			Process swipescreen = Runtime.getRuntime().exec(new String[]{adb, "shell", "input", "touchscreen", "swipe", "200", "200", "202", "202", "" + duration});
			swipescreen.waitFor();
			System.err.println(block[0] + ","+ block[1] + "," + jumper[0] + "," + jumper[1] + "," + duration);
		}
	}
	
	public static long function(int[] jumper, int[] block) {
		int w = jumper[0] - (block[0] + 50);
		int h = jumper[1] - (block[1] + 40);
		double x = Math.sqrt(w * w + h * h) / 520;
		return (long)(x * x * 260 + 260 * x + 260);
	}

	/**
	 * find the curve to indicate a new block
	 * @param data
	 * @param stepy
	 * @param xy
	 */
	public static void downstair(int[][] data, int stepy, int[] xy) {
		int w = data.length;
		int h = data[0].length;
		for(int i=300;i<h;i+=stepy) {
			int[][] line = new int[w][stepy];
			for(int k=0;k<stepy;k++) {
				for(int j =0;j<w;j++) {
					line[j][k] = data[j][i+k];
				}
			}
			boolean straight = straight(line, 6, i, xy);
			if(!straight) {
				break;
			}
		}
	}
	
	/**
	 * TODO: from both side to decide
	 * is it a straight line
	 * @param cube
	 * @param stepx
	 * @param y
	 * @param xy
	 * @return
	 */
	public static boolean straight(int[][] cube, int stepx, int y, int[] xy) {
		int w = cube.length;
		int h = cube[0].length;
		int[] two = new int[2];
		int index = 0;
		int size = stepx * h;
		for(int i=0;i<w;i+=stepx) {
			int sum = 0;
			for(int k=0;k<stepx;k++) {
				for(int j=0;j<h;j++) {
					int color = cube[i+k][j];
					Color pixel = new Color(color);
					double rgb = pixel.getRed()*0.333 + pixel.getGreen()*0.334 + pixel.getBlue()*0.333;
					sum += rgb;
				}
			}
			two[index] = sum/size;
			if(index == 1) {
				if(Math.abs(two[0] - two[1]) > 2) {
					xy[0] = i;
					xy[1] = y;
					System.out.println("curve found: (" + i + "," + y + ") = " + Math.abs(two[0] - two[1]));
					return false;
				}
			}
			index ++;
			index %= 2;
		}
		return true;
	}
	
	public static int[][] readImage(File file) {
		try {
			BufferedImage bi = ImageIO.read(file);
			int width = bi.getWidth();
			int height = bi.getHeight();
			int[][] pixels = new int[width][height];
			for(int x =0;x <width;x ++) {
				for(int y=0;y<height;y++) {
					pixels[x][y] = bi.getRGB(x, y);
				}
			}
			return pixels;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static int[][] frame(int w, int h, int x, int y, int[][] image) {
		int width = image.length;
		int height = image[0].length;
		int[][] frame = new int[w][h];
		for(int i=0;i<w;i++) {
			if(i+x >= width) {
				break;
			}
			for(int j=0;j<h;j++) {
				if(j+y >= height) {
					break;
				}
				frame[i][j] = image[i+x][j+y];
			}
		}
		return frame;
	}
	
	public static void printImage(int[][] image, String saveTo){
		int w = image.length;
		int h = image[0].length;
		BufferedImage nbi=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);  
	    for (int x = 0; x < w; x++) {
	        for (int y = 0; y < h; y++) {
	        	int b = image[x][y];
	        	nbi.setRGB(x, y, b);
	        }  
	    }
	    try {
			ImageIO.write(nbi, "jpg", new File(saveTo));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * cutting the image into pieces
	 * @param image
	 * @param n number of pieces according to x
	 * @param m number of pieces according to y
	 * @param prefix path prefix
	 * @return
	 */
	public static int[][] [][] cutting(int[][] image, int n, int m, String prefix) {
		int[][] [][] result = new int[n][m] [][];
		int w = image.length;
		int h = image[0].length;
		int stepx = w / n;
		int stepy = h / m;
		int startx = 0;
		for(int i=0;i<n;i++) {
			int starty = 0;
			for(int j=0;j<m;j++) {
				int[][] frame = frame(stepx, stepy, startx, starty, image);
				result[i][j] = frame;
				String saveTo = prefix + "_" + i + "_" + j + ".jpg";
				printImage(frame, saveTo);
				starty += stepy;
			}
			startx += stepx;
		}
		return result;
	}
	
	public static void print(int[][] cube) {
		int w = cube.length;
		int h = cube[0].length;
		System.out.println(("[" + w + "," + h + "]"));
		for(int j=0;j<h;j++) {
			for(int i=0;i<w;i++) {
				System.out.print(cube[i][j] + ",");
			}
			System.out.println();
		}
		System.out.println();
	}
}