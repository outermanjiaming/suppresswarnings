package com.suppresswarnings.osgi.nn.cnn;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;

import com.suppresswarnings.osgi.nn.Util;

public class MNIST implements Closeable {
	interface Data {
		int magic = 2051;
		int train  = 60000;
		int test = 10000;
		int width = 28;
		int height= 28;
	}
	interface Label {
		int magic = 2049;
		int train  = 60000;
		int test = 10000;
		int step  = 1;
	}
	int magix = 4;
	int size  = 4;
	int mask  = 0xff;
	int width = 4;
	int height= 4;
	double max = 255.0;
	
	Random random = new Random();
	int bound = 0;
	long dataStart = 0;
	long labelStart = 0;
	String path = "D:/lijiaming/";
	String[] train = {"train-images-idx3-ubyte", "train-labels-idx1-ubyte"};
	String[] test  = {"t10k-images-idx3-ubyte",  "t10k-labels-idx1-ubyte"};
	String[][] files = {train, test};
	public static final int TYPE_TRAIN= 0;
	public static final int TYPE_TEST = 1;
	public static final int LABELS = 10;
	int type;
	FileInputStream data;
	FileInputStream label;
	FileChannel fcdata;
	FileChannel fclabel;
	public MNIST(int type) {
		this.type = type;
		String[] file = files[type];
		try {
			data = new FileInputStream(new File(path + file[0]));
			fcdata = data.getChannel();
			label = new FileInputStream(new File(path + file[1]));
			fclabel = label.getChannel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void init(){
		ByteBuffer dst = ByteBuffer.allocate(4);
		try {
			fcdata.read(dst);
			dst.flip();
			System.out.println("magic:"+dst.getInt());
			dst.clear();
			fcdata.read(dst);
			dst.flip();
			bound = dst.getInt();
			System.out.println("size:"+ bound);
			dst.clear();
			fcdata.read(dst);
			dst.flip();
			System.out.println("width:"+dst.getInt());
			dst.clear();
			fcdata.read(dst);
			dst.flip();
			System.out.println("height:"+dst.getInt());
			dst.clear();
			dataStart = fcdata.position();
			System.out.println("dataStart:" + dataStart);
			
			
			fclabel.read(dst);
			dst.flip();
			System.out.println("magic:"+dst.getInt());
			dst.clear();
			fclabel.read(dst);
			dst.flip();
			System.out.println("size:"+dst.getInt());
			dst.clear();
			labelStart = fclabel.position();
			System.out.println("labelStart:" + labelStart);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void close(){
		try {
			data.close();
			label.close();
		} catch (Exception e) {
		}
	}
	public void start(int position) {
		long positionData = Data.width * Data.height * position + dataStart;
		long positionLabel= Label.step * position + labelStart;
		try {
			fcdata.position(positionData);
			fclabel.position(positionLabel);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public double[][] getData() throws Exception{
		double[][] data = new double[Data.width][Data.height];
		ByteBuffer dst = ByteBuffer.allocate(Data.width * Data.height);
		fcdata.read(dst);
		dst.flip();
		for(int i=0;i<Data.width;i++) {
			for(int j=0;j<Data.height;j++) {
				data[i][j] = (dst.get() & mask) / max;
				if(data[i][j] > 1) {
					System.out.println(data[i][j]);
					throw new RuntimeException();
				}
			}
		}
		return data;
	}
	public double[] getLabel() throws Exception {
		double[] label = new double[LABELS];
		ByteBuffer dst = ByteBuffer.allocate(Label.step);
		fclabel.read(dst);
		dst.flip();
		label[dst.get()] = 1.0;
		return label;
	}
	public Digit[] random(int size) {
		int position = Math.max(0, random.nextInt(bound) - size);
		start(position);
		return next(size);
	}
	public Digit next(){
		try {
			double[][] data = getData();
			double[]   label= getLabel();
			Digit digit = new Digit(data, label);
			return digit;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public Digit[] next(int size) {
		Digit[] digits = new Digit[size];
		for(int i=0;i<size;i++) {
			try {
				double[][] data = getData();
				double[]   label= getLabel();
				digits[i] = new Digit(data, label);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return digits;
	}
	public boolean hasNext(){
		try {
			return fcdata.position() < fcdata.size();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	public static void main(String[] args) throws Exception {
		MNIST mnist = new MNIST(TYPE_TEST);
		mnist.init();
		mnist.start(12);
		for(int i=0;i<10;i++) {
			double[][] data = mnist.getData();
			double[]   label= mnist.getLabel();
			Util.print(data);
			Util.print(label);
		}
		mnist.close();
	}

	public int size() {
		return type == TYPE_TEST ? Data.test : Data.train;
	}
}
