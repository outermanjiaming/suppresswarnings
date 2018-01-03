package com.suppresswarnings.osgi.nn;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import com.suppresswarnings.osgi.nn.cnn.ConvolutionLayer;
import com.suppresswarnings.osgi.nn.cnn.DescendLayer;
import com.suppresswarnings.osgi.nn.cnn.MaxPoolLayer;
import com.suppresswarnings.osgi.nn.cnn.NormalizeLayer;

public class Test {

	public static void main2(String[] args) {
		String file = "./gesture/6/2801512180600_.pic_thumb.jpg";
		double[][] image = Util.readImageAsGray(file);
		ConvolutionLayer convolutionLayer = new ConvolutionLayer(5, 5, 1);
		image = convolutionLayer.conv(image, true);
		MaxPoolLayer maxPoolLayer = new MaxPoolLayer(2, 2, 2);
		image = maxPoolLayer.pool(image);
		NormalizeLayer normalizeLayer = new NormalizeLayer(17);
		normalizeLayer.normalize(image);
		Util.print(image);
		Util.saveImage("./gesture/6/2801512180600_.conv.jpg", image);
	}
	public static void filter(String[] args) {
		try {
			String[] names = Util.getFileNames("./gesture/6/", "pic_thumb.jpg");
			for(String string : names) System.out.println("./gesture/6/"+string);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void backup(String[] args) {
		String origin = "./hand11.jpeg";
		boolean result = Util.backup(origin, true);
		System.out.println("backup = "+result);
	}
	public static void togray(String[] args) {
		File file = new File("./hand11.jpeg");
		try {
			BufferedImage bi = ImageIO.read(file);
			int width = bi.getWidth();
			int height = bi.getHeight();
			BufferedImage gray = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
			for(int i=0;i<width;i++) {
				for(int j=0;j<height;j++) {
					int color = bi.getRGB(i, j);
					Color pixel = new Color(color);
					int c = (int)(pixel.getRed()*0.299 + pixel.getGreen()*0.587 + pixel.getBlue()*0.114);
					System.out.print(c);System.out.print(' ');
					gray.setRGB(i, j, new Color(c,c,c).getRGB());
				}
				System.out.println();
			}
			System.out.println();
			ImageIO.write(gray, "jpeg", new File("./grayhand11.jpeg"));

			System.out.println("w: "+width);
			System.out.println("h: "+height);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static void main4(String[] args) {
		double[][] image = Util.readImageAsGray("D:/image/0.png");
		NormalizeLayer normal = new NormalizeLayer(255);
		normal.normalize(image);
		DescendLayer descend = new DescendLayer();
		double[] input = descend.descend(image);
		System.out.println("input: "+input.length);
		System.out.println("input: " + Arrays.toString(input));
		Network network = new Network(input.length, new int[]{800}, input.length, 0.88d, 0.015d);
		network.fullConnect();
		int step = 1000000;
		int counter = 0;
		while(counter++ < step) {
			network.train(input, input);
			if(counter % 100 == 0) System.out.println(network.error());
			network.clear();
		}
		System.out.println("input: "+input.length);
		System.out.println("input: " + Arrays.toString(input));
		network.close();
	}
	
	public static void main(String[] args) {
		Network network = (Network) Util.deserialize("./add.ser");
		double[][] inputs = {{7,3},{9,4},{8,7},{7,6}};
		double[][] targets = {{10},{13},{15},{13}};
		
		for(int i=0;i<inputs.length;i++) {
			double[] input = inputs[i];
			double[] target = targets[i];
			network.train(input, target);
			double[] output = network.output();
			System.out.println("input = "+Arrays.toString(input));
			System.out.print("target = "+Arrays.toString(target));
			System.out.println(" output = "+Arrays.toString(output));
		}
	}
	public static void main1(String[] args) {
		Network network = new Network(2, new int[]{3}, 1, 0.88d, 0.015d);
		network.fullConnect();
		double[][] inputs = {{2,3},{3,4},{4,5},{5,6}};
		double[][] targets = {{5},{7},{9},{11}};
		int step = 10000;
		int counter = 0;
		while(counter++ < step) {
			network.error = 0;
			for(int i=0;i<inputs.length;i++) {
				double[] input = inputs[i];
				double[] target = targets[i];
				network.train(input, target);
			}
			if(network.error < 0.0001) {
				break;
			}
			System.out.println("step:" + counter + "\t" + network.error);
		}
		
		for(int i=0;i<inputs.length;i++) {
			double[] input = inputs[i];
			double[] target = targets[i];
			network.train(input, target);
			double[] output = network.output();
			System.out.println("input = "+Arrays.toString(input));
			System.out.println("output = "+Arrays.toString(output));
			System.out.println("target = "+Arrays.toString(target));
		}
		Util.serialize(network, "./add.ser");
	}
}
