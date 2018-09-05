/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.data;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.PersistUtil;
import com.suppresswarnings.corpus.common.Provider;
import com.suppresswarnings.corpus.common.Set;
import com.suppresswarnings.osgi.leveldb.LevelDB;
import com.suppresswarnings.osgi.leveldb.LevelDBImpl;
import com.suppresswarnings.osgi.neuralnetwork.AI;
import com.suppresswarnings.osgi.neuralnetwork.Layer;
import com.suppresswarnings.osgi.neuralnetwork.NN;
import com.suppresswarnings.osgi.neuralnetwork.Network;

public class DataProvider implements Provider<LevelDB> {
	static final String dbname = "/data";
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	
	LevelDB levelDB;
	public void activate() {
		if(this.levelDB == null) {
			this.levelDB = new LevelDBImpl(dbname);
			logger.info("[data] create.");
		}
		logger.info("[data] activate.");
	}

	public void deactivate() {
		if(this.levelDB != null) {
			this.levelDB.close();
			logger.info("[data] close.");
		}
		this.levelDB = null;
		logger.info("[data] deactivate.");
	}

	public void modified() {
		logger.info("[data] modified.");
	}
	
	@Override
	public String identity() {
		return "Data";
	}

	@Override
	public String description() {
		return "data leveldb";
	}

	@Override
	public LevelDB instance() {
		return levelDB;
	}
	public static double[] onehot(int index, int size) {
		double[] onehot = new double[size];
		onehot[index] = 1.0d;
		return onehot;
	}
	
	public static String head = "001.Collect.Corpus.Quiz.T_Corpus_oDqlM1TyKpSulfMC2OsZPwhi-9Wk_1535356133666_154.Answer";
	
	public static void main3(String[] args) {
		LevelDBImpl leveldb = new LevelDBImpl("data1/data");
		HashMap<String, String> words = new HashMap<>();
		leveldb.page(head, head, null, 100000, (k, v) ->{
			if(!k.contains("Similar") && !k.contains("Reply")) {
				words.put(v,v);
				String similar = k + ".Similar";
				leveldb.page(similar, similar, null, 1000, (x, y) ->{
					words.put(v, y);
				});
			}
		});
		
		words.forEach((x, y)-> {System.out.println(x + " == " + y);} );
		LevelDBImpl level = new LevelDBImpl("echo");
		byte[] wordbytes = PersistUtil.bytes(words);
		level.put("words", wordbytes);
		level.close();
		leveldb.close();
	}
	
	public static double[] words2vec(String words, Map<String, double[]> chars, int size) {
		double[] result = new double[size];
		char[] cs = words.toCharArray();
		int index = 0;
		for(char c : cs) {
			String key = ""+c;
			double[] value = chars.get(key);
			if(value == null) value = new double[5];
			fillVector(result, index, value);
			index += 5;
		}
		return result;
	}
	
	public static List<double[]> cutVector(double[] longer, int size) {
		int index = 0;
		int length = longer.length;
		List<double[]> result = new ArrayList<>();
		while(index < length) {
			double[] item = new double[size];
			for(int i=0;i<size;i++,index++) {
				item[i] = longer[index];
			}
			result.add(item);
		}
		return result;
	}
	public static List<String> vectors2words(List<double[]> vectors, AI network, Map<String, String> doubles) {
		List<String> result = new ArrayList<>();
		for(double[] x : vectors) {
			double[] y = network.test(x);
			double[] near = nearly(y);
			String word = doubles2string(near);
			String value = doubles.get(word);
			result.add(value);
		}
		return result;
	}
	public static void fillVector(double[] longer, int index, double[] shorter) {
		for(int i=index,j=0;j<shorter.length;i++,j++) {
			longer[i] = shorter[j];
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void main1(String[] args) {
		int inputSize = 5 * 40;
		
		NN ai = (NN) PersistUtil.deserialize(System.getenv("HB_HOME") + "echo/ai2.network");//new NN(inputSize, outputSize, new int[]{450});//
		NN network = (NN) PersistUtil.deserialize(System.getenv("HB_HOME") + "echo/echo2.network");
		ai.iteration(3500);
		LevelDBImpl leveldb = new LevelDBImpl("levelecho");
//		byte[] echo = leveldb.read("echonet2");
//		NN network = null;
//		if(echo == null) {
//			network = new NN(inputSize, outputSize, new int[]{50});
//		} else {
//			network = PersistUtil.object(echo, NN.class);
//		}
		
		
		byte[] charsB = leveldb.read("chars");
		HashMap<String, double[]> chars = PersistUtil.object(charsB, HashMap.class);
		byte[] vecB = leveldb.read("vecbytes");
		HashMap<String, double[]> string2vec = PersistUtil.object(vecB, HashMap.class);
		byte[] indexB = leveldb.read("indexs");
		HashMap<String, Integer> indexs = PersistUtil.object(indexB, HashMap.class);
		byte[] doubleB = leveldb.read("doubles");
		HashMap<String, String> doubles = PersistUtil.object(doubleB, HashMap.class);
		byte[] wordB = leveldb.read("words");
		HashMap<String, String> words = PersistUtil.object(wordB, HashMap.class);
		
		java.util.Set<String> cs = words.keySet();
		int length = cs.size();
		double[][] inputs = new double[length][];
		double[][] outputs = new double[length][];
		int j = 0;
		for(String key : cs) {
			String value = words.get(key);
			double[] inputx = words2vec(key, chars, inputSize);
			double[] inputy = words2vec(value, chars, inputSize);
			System.out.println(doubles2string(inputx));
			System.out.println(doubles2string(inputy));
			System.out.println("=====================================================");
			inputs[j] = inputx;
			outputs[j] = inputy;
			j ++;
		}
		System.out.println();
		ai.train(inputs, outputs);
		System.out.println();
		
//		List<double[]> xx = new ArrayList<>();
//		List<double[]> yy = new ArrayList<>();
//		for(String key : chars.keySet()) {
//			double[] x = chars.get(key);
//			double[] y = network.test(x);
//			double[] near = nearly(y);
//			String result = doubles2string(near);
//			
//			String value = doubles.get(result);
//			if(!key.equals(value)) {
//				System.out.println(Arrays.toString(x));
//				System.out.println(Arrays.toString(near));
//				System.out.println(result);
//			    System.out.println(key + " == " + value);
//			    xx.add(near);
//			    yy.add(x);
//			    xx.add(y);
//			    yy.add(x);
//			}
//		}
//		inputs = new double[length + xx.size()][];
//		outputs = new double[length + yy.size()][];
//		for(int n =0;n<xx.size();n++) {
//			double[] m = xx.get(n);
//			double[] l = yy.get(n);
//			inputs[n] = m;
//			outputs[n] = l;
//		}
//		j = xx.size();
//		for(String key : cs) {
//			double[] value = chars.get(key);
//			int index = indexs.get(key);
//			System.out.println(index + "\t" + Arrays.toString(value));
//			inputs[j] = value;
//			outputs[j] = value;
//			j ++;
//		}
//		
//		network.train(inputs, outputs);
		
		
//		PersistUtil.serialize(network, System.getenv("HB_HOME") + "echo/echo.network");
		PersistUtil.serialize(ai, System.getenv("HB_HOME") + "echo/ai2.network");
		byte[] net = PersistUtil.bytes(network);
		leveldb.put("echonet2", net);
		
		leveldb.close();
	}
	public static void output2train(AI echo, Map<String, String> words, AI reply, Map<String, double[]> chars) {
		AtomicInteger integer = new AtomicInteger(0);
		words.forEach((x,y) ->{
			integer.addAndGet(y.length());
		});
		int length = integer.get();
		double[][] inputs = new double[length * 2][];
		double[][] outputs = new double[length * 2][];
		int inputSize = 5 * 40;
		int charSize = 5;
		AtomicInteger index = new AtomicInteger(0);
		words.forEach((input, output) ->{
			double[] inputx = words2vec(input, chars, inputSize);
			double[] outputy = reply.test(inputx);
			List<double[]> result = cutVector(outputy, charSize);
			char[] outputchars = output.toCharArray();
			
			for(int i=0;i<outputchars.length;i++) {
				String key = "" + outputchars[i];
				double[] target = chars.get(key);
				inputs[index.get()] = target;
				outputs[index.get()] = target;
				index.incrementAndGet();
				double[] they = result.get(i);
				inputs[index.get()] = they;
				outputs[index.get()] = target;
				index.incrementAndGet();
			}
		});
		
		echo.train(inputs, outputs);
	}
	public static void main(String[] args) {
		int inputSize = 5 * 40;
		NN ai = (NN) PersistUtil.deserialize(System.getenv("HB_HOME") + "echo/ai2.network");
		NN network = (NN) PersistUtil.deserialize(System.getenv("HB_HOME") + "echo/echo.network");
		String input = "最近吃的不好";
		LevelDBImpl leveldb = new LevelDBImpl("levelecho");
		byte[] charsB = leveldb.read("chars");
		HashMap<String, double[]> chars = PersistUtil.object(charsB, HashMap.class);
		byte[] doubleB = leveldb.read("doubles");
		HashMap<String, String> doubles = PersistUtil.object(doubleB, HashMap.class);
		byte[] wordB = leveldb.read("words");
		HashMap<String, String> words = PersistUtil.object(wordB, HashMap.class);
		words.forEach((x,y)->{System.out.println(x + " == " + y);});
		network.iteration(10);
		output2train(network, words, ai, chars);
//		PersistUtil.serialize(network, System.getenv("HB_HOME") + "echo/echo3.network");
		
		Scanner scanner = new Scanner(System.in);
		while(!"stop".equals(input)) {
			
			
			double[] inputx = words2vec(input, chars, inputSize);
			double[] outputy = ai.test(inputx);
			List<double[]> result = cutVector(outputy, 5);
			char[] cs = input.toCharArray();
			StringBuffer sb = new StringBuffer();
			for(int i = 0;i < result.size(); i++) {
				String realstr = " ";
				double[] reald = new double[5];
				if(i<cs.length) {
					realstr = ""+ cs[i];
					reald = chars.get(realstr);
				}
				double[] item = result.get(i);
				double[] ret = network.test(item);
				double[] near = nearly(ret);
				String string = doubles2string(near);
				
				double[] ret1 = network.test(reald);
				double[] near1 = nearly(ret1);
				String string1 = doubles2string(near1);
				String predict = predict(chars, near);
				if(toosmall(near))break;
				sb.append(predict);
			}
			System.out.println("电脑回复：" + sb.toString());
			System.out.println("Enter :");
			input = scanner.nextLine();
		}
		scanner.close();
	}
	public static boolean toosmall(double[] near) {
		double sum = 0;
		for(double d : near) {
			sum += d * d;
		}
		double m = Math.sqrt(sum);
		return m < 0.15;
	}
	private static String predict(HashMap<String, double[]> chars, double[] near) {
		AtomicReference<Double> err = new AtomicReference<Double>(new Double(10000));
		AtomicReference<String> the = new AtomicReference<String>("");
		chars.forEach((ch, dbl) ->{
			double e = distance(dbl, near);
			if(err.get().doubleValue() > e) {
				err.set(e);
				the.set(ch);
			}
		});
		return the.get();
	}
	
	public static double distance(double[] d1, double[] d2) {
		double err = 0;
		for(int i=0;i<d1.length;i++) {
			double e = Math.abs(d1[i] - d2[i]);
			err += e * e;
		}
		return Math.sqrt(err);
	}

	public static double[] nearly(double[] array) {
		DecimalFormat format = new DecimalFormat(".##");
		for(int i=0;i<array.length;i++) {
			double d = array[i];
			array[i] = Double.valueOf(format.format(d));
		}
		return array;
	}
	public static void main2(String[] args) {
		LevelDBImpl leveldb = new LevelDBImpl("data1/data");
		HashMap<String, double[]> chars = new HashMap<>();
		HashMap<String, double[]> string2vec = new HashMap<>();
		HashMap<String, Integer> indexs = new HashMap<>();
		HashMap<String, String> doubles = new HashMap<>();
		String head = "001.Collect.Corpus.Quiz.T_Corpus_oDqlM1TyKpSulfMC2OsZPwhi-9Wk_1535356133666_154.Answer";
		
		HashMap<String, String> words = new HashMap<>();
		
		leveldb.page(head, head, null, 10000, (k,v) -> {
			if(!k.contains("Similar") && !k.contains("Reply")) {
				words.put(v,v);
				String similar = k + ".Similar";
				leveldb.page(similar, similar, null, 1000, (x, y) ->{
					words.put(v, y);
				});
			}
			char[] cs = v.toCharArray();
			for(char c : cs) {
				String key = "" +c;
				double[] value = chars.get(key);
				if(value == null) {
					value = allVector(string2vec, key, doubles);
				}
				chars.put(key, value);
			}
		});
		
		for(String key : chars.keySet()) {
			indexs.put(key, indexs.size());
		}
		
		LevelDBImpl level = new LevelDBImpl("levelecho");
		
		words.forEach((x, y)-> {System.out.println(x + " == " + y);} );
		
		byte[] wordbytes = PersistUtil.bytes(words);
		level.put("words", wordbytes);
		
		byte[] charsbytes = PersistUtil.bytes(chars);
		level.put("chars", charsbytes);
		
		byte[] vecbytes = PersistUtil.bytes(string2vec);
		level.put("vecbytes", vecbytes);
		
		byte[] indexbytes = PersistUtil.bytes(indexs);
		level.put("indexs", indexbytes);
		
		byte[] doublebytes = PersistUtil.bytes(doubles);
		level.put("doubles", doublebytes);
		
		level.list("0", 10, (x, y) -> System.out.println(x));
	}
	
	public static double[] randomVector(){
		double[] vec = new double[5];
		Random random = new Random();
		for(int i=0;i<vec.length;i++) {
			int x = random.nextInt(100);
			double d = x / 100.0d;
			vec[i] = d;
		}
		return vec;
	}
	public static String doubles2string(double[] doubles) {
		StringBuffer sb = new StringBuffer();
		for(double d : doubles) {
			sb.append(Math.abs(d) + " ");
		}
		sb.deleteCharAt(sb.length() -1);
		return sb.toString();
	}
	public static double[] allVector(Map<String, double[]> string2vec, String key, Map<String, String> doubles){
		double[] vec = randomVector();
		String string = doubles2string(vec);
		double[] exist = string2vec.get(string);
		while(exist != null) {
			System.err.println(string2vec.size() + "===========" + string);
			vec = randomVector();
			string = doubles2string(vec);
			exist = string2vec.get(string);
		}
		string2vec.put(string, vec);
		doubles.put(string, key);
		return vec;
	}
	
	public static void main7(String[] args) {
		LevelDBImpl leveldb = new LevelDBImpl("data1/data");
		String head = "001.Collect.Corpus.Quiz.T_Corpus_oDqlM1TyKpSulfMC2OsZPwhi-9Wk_1535356133666_154.Answer";
		List<String> words = new ArrayList<>();
		leveldb.page(head, head, null, 10000, (k,v) -> {
			if(!k.contains("Similar") && !k.contains("Reply")) {
				words.add(v);
			}
		});
		AtomicInteger integer = new AtomicInteger(0);
		words.forEach((x) -> {System.out.println(integer.getAndIncrement() +"\t"+ x);});
	}
}
