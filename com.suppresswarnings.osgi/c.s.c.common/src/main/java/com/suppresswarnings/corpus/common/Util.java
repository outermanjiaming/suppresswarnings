package com.suppresswarnings.corpus.common;


import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

public class Util {
	public static DecimalFormat format = new DecimalFormat("0.0000");
	public static Random random = new Random();
	public static void serialize(Serializable object, String serializeTo) {
		File file = new File(serializeTo);
		ObjectOutputStream oos = null;
		try {
			OutputStream stream = new FileOutputStream(file);
			oos = new ObjectOutputStream(stream);
			oos.writeObject(object);
			oos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(oos != null) {
				try {
					oos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static Object deserialize(String serializeTo) {
		File file = new File(serializeTo);
		ObjectInputStream ois = null;
		try {
			InputStream stream = new FileInputStream(file);
			ois = new ObjectInputStream(stream);
			Object object = ois.readObject();
			return object;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if(ois != null) {
				try {
					ois.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static double[][] conv2(double[][] mask, double[][] input, int step, boolean same) {
		int maskx = mask.length;
		int masky = mask[0].length;
		int inputx = input.length;
		int inputy = input[0].length;
		int resultx = maskx + inputx - 1;
		int resulty = masky + inputy - 1;
		int resultw = (resultx + step - 1) / step;
		int resulth = (resulty + step - 1) / step;

		double[][] result = new double[resultw][resulth];
		int i=0;
		for (int x = 0; x < resultx; x += step) {
			int j=0;
			for (int y = 0; y < resulty; y += step) {
				double data = 0;
				for (int m = 0; m < maskx; m++) {
					int cursorx = x - m;
					for (int n = 0; n < masky; n++) {
						double temp = 0;
						int cursory = y - n;
						if (cursorx < 0 || cursorx > inputx - 1 || cursory < 0 || cursory > inputy - 1){
							temp = 0;
						} else {
							temp = input[cursorx][cursory];
						}
						data += mask[m][n] * temp;
					}
				}
				result[i][j] = data;
				j++;
			}
			i++;
		}
		if (same) {
			double[][] temp = new double[inputx][inputy];
			fill(result, temp);
			result = temp;
		}
		return result;
	}
	public static void saveImage(String imageFilePath, double[][] image) {
		File file = new File(imageFilePath);
		int w = image.length;
		int h = image[0].length;
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++) {
				int rgb = (int) image[i][j];
				Color color = new Color(rgb, rgb, rgb);
				bi.setRGB(i, j, color.getRGB());
			}
		}
		try {
			ImageIO.write(bi, "jpg", file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static double[][] readImageAsGray(String imageFilePath) {
		double[][] matrix = null;
		File file = new File(imageFilePath);
		try {
			BufferedImage bi = ImageIO.read(file);
			int width = bi.getWidth();
			int height = bi.getHeight();
			matrix = new double[width][height];
			for(int i=0;i<width;i++) {
				for(int j=0;j<height;j++) {
					int color = bi.getRGB(i, j);
					Color pixel = new Color(color);
					double rgb = pixel.getRed()*0.299 + pixel.getGreen()*0.587 + pixel.getBlue()*0.114;
					matrix[i][j] = rgb;
				}
			}
		} catch (Exception e) {
		}
		return matrix;
	}
	public static double[][] maxpooling(double[][] input, int w, int h, int step) {
		int inputw = input.length;
		int inputh = input[0].length;
		int resultw = ((inputw - w + step - 1) / step) + 1;
		int resulth = ((inputh - h + step - 1) / step) + 1;
		double[][] result = new double[resultw][resulth];
		int cursorx = 0;
		for(int i=0;i<resultw;i++) {
			int cursory = 0;
			for(int j=0;j<resulth;j++) {
				double max = 0;
				int xx = cursorx;
				for(int x=0;x<w && xx < inputw;x++) {
					int yy = cursory;
					for(int y=0;y < h && yy < inputh;y++) {
						double temp = input[xx][yy];
						yy++;
						if(temp > max) max = temp;
					}
					xx++;
				}
				result[i][j] = max;
				cursory += step;
			}
			cursorx += step;
		}
		return result;
	}
	
	public static double[][] averagepooling(double[][] input, int w, int h, int step) {
		int inputw = input.length;
		int inputh = input[0].length;
		int resultw = ((inputw - w + step - 1) / step) + 1;
		int resulth = ((inputh - h + step - 1) / step) + 1;
		double[][] result = new double[resultw][resulth];
		int cursorx = 0;
		int m = w * h;
		for(int i=0;i<resultw;i++) {
			int cursory = 0;
			for(int j=0;j<resulth;j++) {
				double sum = 0;
				int xx = cursorx;
				for(int x=0;x<w && xx < inputw;x++) {
					int yy = cursory;
					for(int y=0;y < h && yy < inputh;y++) {
						double temp = input[xx][yy];
						yy++;
						sum += temp;
					}
					xx++;
				}
				result[i][j] = sum / m;
				cursory += step;
			}
			cursorx += step;
		}
		return result;
	}
	
	public static void fill(double[][] src, double[][] dest) {
		int x = src.length;
		int y = src[0].length;
		int w = dest.length;
		int h = dest[0].length;
		if(x > w && y > h) {
			int starti = (x - w) / 2;
			int startj = (y - h) / 2;
			for(int i=0;i<w;i++) {
				for(int j=0;j<h;j++) {
					dest[i][j] = src[starti+i][startj+j];
				}
			}
		}
		if(x < w && y < h) {
			int starti = (w - x) / 2;
			int startj = (h - y) / 2;
			for(int i=0;i<x;i++) {
				for(int j=0;j<y;j++) {
					dest[i+starti][j+startj] = src[i][j];
				}
			}
		}
	}
	
	public static double initReturnSum(double[][] matrix, Random random) {
		double sum = 0;
		for(int i=0;i<matrix.length;i++) {
			for(int j=0;j<matrix[i].length;j++) {
				double value = random.nextDouble();
				matrix[i][j] = value;//1.0d / (1.0d + Math.exp(-value));;
				sum += value;
			}
		}
		return sum;
	}
	
	//TODO: main
	public static void main(String[] args) {
		double[][] mask = {
				{0,0.25,0},
				{0.25,0,0.25},
				{0,0.25,0}
		};
		
		double[][] input = new double[11][11];
		initReturnSum(input, new Random());
		double[][] result = conv2(mask, input, 2, false);
		print(input);
		print(mask);
		print(result);
		double[][] poll = maxpooling(result, 2, 2, 2);
		print(poll);
		double[][] ave = averagepooling(result, 2, 2, 2);
		print(ave);
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
	
	public static int[][] readImage(File file) {
		try {
			BufferedImage bi = ImageIO.read(file);
			return readImage(bi);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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
	public static double print(double[] array) {
		double max = 0;
		int index = 0;
		for(int i=0;i<array.length;i++){
			double v = array[i];
			if(v > max) {
				max = v;
				index = i;
			}
			System.out.print(" " + format.format(v));
		}
		System.out.print(" MAX=" + format.format(max) + " : " + index);
		return max;
	}
	public static void print(double[][] matrix) {
		System.out.println("matrix:[" + matrix.length+","+matrix[0].length+"]");
		double max = 0;
		for(int i=0;i<matrix.length;i++) {
			double v = print(matrix[i]);
			if(v > max) max = v;
			System.out.println();
		}
		System.out.println(" MAX=" + format.format(max));
	}
	public static void print(double[][][] cube) {
		for(int i=0;i<cube.length;i++) {
			print(cube[i]);
			System.out.println();
		}
		System.out.println();
	}
	
	public static int argmax(double[] vector) {
		double max = vector[0];
		int index = 0;
		for(int i=1;i<vector.length;i++) {
			if(vector[i] > max) {
				max = vector[i];
				index = i;
			}
		}
		return index;
	}
	
	public static List<Integer> multimax(double[] vector) {
		List<Integer> index = new ArrayList<Integer>();
		for(int i=0;i<vector.length;i++) {
			if(vector[i] > 0.88) {
				index.add(i);
			}
		}
		return index;
	}
	public static double[] onehot(int index, int size) {
		double[] vector = new double[size];
		vector[index] = 1.0d;
		return vector;
	}
	
	public static boolean backup(String originFile, boolean replace) {
		File origin = new File(originFile);
		if(!origin.exists()) {
			return false;
		}
		String backupFile = originFile + ".backup";
		File backup = new File(backupFile);
		if(!replace) {
			if(backup.exists()) {
				return false;
			}
		}
		if(!backup.exists()) {
			try {
				backup.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileInputStream input = null;
		FileOutputStream output = null;
		try {
			input = new FileInputStream(origin);
			output = new FileOutputStream(backup);
			FileChannel inputChannel = input.getChannel();
			FileChannel outputChannel = output.getChannel();
			ByteBuffer buffer = null;
			int length = 2097152;
			while(true) {
				if(inputChannel.position() == inputChannel.size()) {
					inputChannel.close();
					outputChannel.close();
					break;
				}
				long block = inputChannel.size() - inputChannel.size();
				int size = length;
				if(block < length) {
					size = length;
				}
				buffer = ByteBuffer.allocateDirect(size);
				inputChannel.read(buffer);
				buffer.flip();
				outputChannel.write(buffer);
				outputChannel.force(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(input != null){
				try {
					input.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(output != null) {
				try {
					output.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	
	public static String[] getFileNames(String folder, String suffix) {
		try {
			return Files.list(Paths.get(folder)).filter(path -> path.getFileName().toString().endsWith(suffix)).map(path -> path.getFileName().toString()).collect(Collectors.toList()).toArray(new String[0]);
		} catch (IOException e) {
			e.printStackTrace();
			return new String[0];
		}
	}
	public static String[] getAbsoluteName(String folder, String[] names) {
		String[] absolute = new String[names.length];
		for(int i=0;i<names.length;i++){
			absolute[i] = folder + names[i];
		}
		return absolute;
	}

	public static double random() {
		double rand = random.nextDouble();
		double init = 0.12;
		return rand * 2 * init - (rand / Math.abs(rand)) * init;
	}

	public static double[][] ones(int w, int h) {
		double[][] ones = new double[w][h];
		for(int i=0;i<w;i++) {
			for(int j=0;j<h;j++) {
				ones[i][j] = 1;
			}
		}
		return ones;
	}
	public static double[][] random(int w, int h) {
		double[][] matrix = new double[w][h];
		for(int i=0;i<w;i++) {
			for(int j=0;j<h;j++) {
				matrix[i][j] = random.nextDouble();
			}
		}
		return matrix;
	}
	public static double[][] zerone(int w, int h) {
		double[][] matrix = new double[w][h];
		for(int i=0;i<w;i++) {
			for(int j=0;j<h;j++) {
				matrix[i][j] = random.nextDouble() > 0.5 ? 1 : 0;
			}
		}
		return matrix;
	}
	public static Image getImage(double[][] image) {
		int y = image.length;
		int x = image[0].length;
		BufferedImage bi = new BufferedImage(x, y, BufferedImage.TYPE_BYTE_GRAY);
		for(int j=0;j<y;j++) {
			for(int i=0;i<x;i++){
				int rgb = (int) (255* image[j][i]);
				Color color = new Color(rgb, rgb, rgb);
				bi.setRGB(i, j, color.getRGB());
			}
		}
		return bi;
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
	
	public static String getLocalMac() throws Exception {
		try {
			byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
			StringBuffer sb = new StringBuffer();
			for(int i=0; i<mac.length; i++) {
				String str = Integer.toHexString(mac[i] & 0xff);
				sb.append(str.length()==1 ? "0"+str : str);
			}
			return sb.toString();
		} catch (SocketException e) {
			throw new Exception("没有联网吗？请打开网络。");
		} catch (UnknownHostException e) {
			throw new Exception("本机网卡异常，检查一下正常联网吗？");
		} catch (NullPointerException e) {
			throw new Exception("请打开网络，不然没法确认身份！");
		} catch (Exception e) {
			throw new Exception("获取本机身份异常，请联系素朴网联客服！");
		}
	}
}
