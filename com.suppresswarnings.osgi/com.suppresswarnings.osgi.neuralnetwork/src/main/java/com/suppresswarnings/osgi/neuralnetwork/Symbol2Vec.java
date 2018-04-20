/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.osgi.neuralnetwork;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Symbol2Vec {
	public static final String ser = "D:/files/todo/Fundamental.ser";
	public static final String ser_chars = "D:/files/todo/TutorialCrawler.chars";
	public static final String ser_data = "D:/files/todo/TutorialCrawler.dat";
	public static final String ser_nn = "D:/files/todo/Symbol2Vec.nn";
	public static final String ser_auto = "D:/files/todo/Symbol2Vec.autoencode";
	public static final String ser_map = "D:/files/todo/Symbol2Vec.map";
	Chars chars;
	AI network;
	AI autoencoder;
	HashMap<String, double[]> symbol2vec;
	
	@SuppressWarnings("unchecked")
	public Symbol2Vec() {
		Fundamental f = (Fundamental) Util.deserialize(ser);
		chars = f.read();
		network = (AI) Util.deserialize(ser_nn);//network = new NN(103, 206, new int[] {20});
		autoencoder = (AI) Util.deserialize(ser_auto);
		symbol2vec = (HashMap<String, double[]>) Util.deserialize(ser_map);
	}
	public static void main(String[] args) throws Exception {
		Symbol2Vec s2v = new Symbol2Vec();
		char[] cs = s2v.chars.chars;
		for(char c : cs) {
			double[] vec = s2v.symbol2vec.get("" + c);
			System.out.println(c + " = " + Arrays.toString(vec));
		}
	}
	
	public static void main0(String[] args) {
		Fundamental f = (Fundamental) Util.deserialize(ser);
		Chars chars = f.read();
		p(chars.toString());
		Util.print(chars.onehot("y"));
		
		"–, —".chars().forEach(action -> System.out.println(action));
		double[] result = concat(new double[]{1, 7, 2}, new double[] {3,4} );
		Util.print(result);
	}
	
	public static void p(String msg) {
		System.out.println("[Symbol2Vec] " + msg);
	}
	public Data readData() throws Exception {
		Data data = new Data();
		Files.lines(Paths.get(ser_data))
		.limit(10000)
		.map(line -> line.split("\\s+"))
		.forEach(words -> {
			for(String word : words) {
				word2data(data, word);
			}
		});
		return data;
	}
	public Data word2data(Data rows, String word) {
		char[] chs = word.toCharArray();
		int index = 0;
		for(;index < chs.length; index++) {
			String x = "" + chs[index];
			String y0 = (index - 1 < 0) ? " " : "" + chs[index - 1];
			String y1 = (index + 1 >= chs.length) ? " " : "" + chs[index + 1];
			double[] output0 = chars.onehot(y0);
			double[] output1 = chars.onehot(y1);
			double[] input  = chars.onehot(x);
			double[] output = concat(output0, output1);
			Row row = new Row(word, input, output);
			rows.put(row);
		}
		return rows;
	}
	
	public static double[] concat(double[] x1, double[] x2) {
		double[] y = new double[x1.length + x2.length];
		System.arraycopy(x1, 0, y, 0, x1.length);
		System.arraycopy(x2, 0, y, x1.length, x2.length);
		return y;
	}
	
	public static void once() {
		try {
			Fundamental f = generate(ser_chars, ser_data);
			Util.serialize(f, ser);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public double[] symbol2vec(String c) {
		double[] input = chars.onehot(c);
		network.test(input);
		return network.layer(1);
	}
	public static Fundamental generate(String charsPath, String wordsPath) throws Exception {
		File file = new File(charsPath);
		if(file.exists()) {
			p("file exists");
			return (Fundamental) Util.deserialize(ser);
		}
		
		Optional<String> chars = Files.lines(Paths.get(wordsPath))
		.map(line -> line.split(""))
		.flatMap(x -> Arrays.stream(x))
		.distinct()
		.sorted()
		.reduce((r, x) -> r + x);
		String string = chars.get();
		Files.write(Paths.get(charsPath), string.getBytes("UTF-8"), StandardOpenOption.CREATE_NEW);
		file = new File(charsPath);
		long created = file.lastModified();
		Fundamental result = new Fundamental(charsPath, created, created, string.length());
		return result;
	}
}

class Chars {
	int size;
	char[] chars;
	Map<String, Integer> indexs;
	
	public Chars(int size, char[] chars, Map<String, Integer> indexs) {
		super();
		this.size = size;
		this.chars = chars;
		this.indexs = indexs;
	}

	public double[] onehot(String c) {
		int index = indexs.get(c);
		double[] x = new double[size];
		x[index] = 1;
		return x;
	}

	@Override
	public String toString() {
		return "Chars [size=" + size + ", \nchars=" + Arrays.toString(chars) + ", \nindexs=" + indexs + "]";
	}
	
}

class Fundamental implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2008984001910262584L;
	String charsPath;
	long created;
	long modified;
	int charsCount;
	public Fundamental() {}
	public Fundamental(String charsPath, long created, long modified, int charsCount) {
		super();
		this.charsPath = charsPath;
		this.created = created;
		this.modified = modified;
		this.charsCount = charsCount;
	}

	public Chars read() {
		try {
			File file = new File(charsPath);
			char[] chars = Files.lines(Paths.get(charsPath)).limit(1).findFirst().get().toCharArray();
			int index = 0;
			Map<String, Integer> indexs = new HashMap<String, Integer>();
			for(char s : chars) {
				indexs.put(""+s, index);
				index ++;
			}
			if(charsCount != chars.length && file.lastModified() != modified) {
				Symbol2Vec.p("this file has been modified");
			}
			Chars result = new Chars(charsCount, chars, indexs);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}