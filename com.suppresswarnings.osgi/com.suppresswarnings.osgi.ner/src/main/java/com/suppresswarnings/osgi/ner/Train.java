package com.suppresswarnings.osgi.ner;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.Timing;

/**
 * tsv is like this:
 * 政{1月71日|checkin}{株洲凤凰鑫城酒店|hotel},{行政商务单人间|type}{1间|rooms}总价
 * @author lijiaming
 *
 */
public class Train {
	public static Charset cs = Charset.forName("UTF-8");
	public static void train(String tsvFile, String serializeToFile) {
		Properties props = new Properties();
		props.setProperty("map", "word=0,answer=1");
		props.setProperty("useClassFeature", "true");
		props.setProperty("useWord", "true");
		props.setProperty("useNGrams", "true");
		props.setProperty("noMidNGrams", "true");
		props.setProperty("useDisjunctive", "true");
		props.setProperty("maxNGramLeng", "6");
		props.setProperty("usePrev", "true");
		props.setProperty("useNext", "true");
		props.setProperty("useSequences", "true");
		props.setProperty("usePrevSequences", "true");
		props.setProperty("maxLeft", "1");
		props.setProperty("useTypeSeqs", "true");
		props.setProperty("useTypeSeqs2", "true");
		props.setProperty("useTypeySequences", "true");
		props.setProperty("wordShape", "chris2useLC");
		props.setProperty("trainFile", tsvFile);
		props.setProperty("serializeTo", serializeToFile);
		fit(props);
	}
	
	public static void fit(Properties props) {
		SeqClassifierFlags flags = new SeqClassifierFlags(props);
	    CRFClassifier<CoreLabel> crf = new CRFClassifier<CoreLabel>(flags);
	    String serializeTo = flags.serializeTo;
	    Timing timing = new Timing();
	    crf.train();
	    timing.done("CRFClassifier training");
	    crf.serializeClassifier(serializeTo);
	}
	
	public static void tsv(String taggedFile) {
		String step3 = taggedFile + "_tsv";
		try {
			PrintStream print = new PrintStream(step3);
			List<String> taggedList = Files.readAllLines(Paths.get(taggedFile), cs);
			for(String text : taggedList) {
				print.print(tsv0(text));
			}
			print.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void tsv(List<String> taggedList, String tsvFile) {
		try {
			PrintStream print = new PrintStream(tsvFile);
			for(String text : taggedList) {
				print.print(tsv0(text));
			}
			print.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String tsv0(String tagText) {
		char[] chars = tagText.toCharArray();
		StringBuffer tsv = new StringBuffer();
		StringBuffer key = new StringBuffer();
		StringBuffer value = new StringBuffer();
		int state = 0;
		int last = 0;
		int left = 1;
		int middle = 3;
		int right = 2;
		String split = "\t";
		String rf = "\n";
		for(char c : chars) {
			if(c == '{') {
				state = left;
			} else if(c == '}') {
				state = right;
				//in case {not a real one}
				if(key.length() == 0) {
					key.append('O');
				}
				for(int i =0;i<value.length();i++){
					 char cv = value.charAt(i);
					 tsv.append(cv).append(split).append(key).append(rf);
				}
				key.setLength(0);
				value.setLength(0);
			} else if(c == '|') {
				state = middle;
			} else {
				 if(state > 0) {
					 last = state;
				 }
				 state = 0;
				 
				 if(last == left) {
					 value.append(Util.uniform(c));
				 }else if(last == middle) {
					 key.append(c);
				 } else if(last == right) {
					 last = state;
					 tsv.append(Util.uniform(c)).append(split).append('O').append(rf);
				 } else {
					 tsv.append(Util.uniform(c)).append(split).append('O').append(rf);
				 }
			}
			if(state > 0) {
				 last = state;
			}
		}
		tsv.append(rf);
		return tsv.toString();
	}
	
	public static void optimize(String tagged, String type, int index, int start) {
		String step3  = tagged + "_step3";
		String origin = tagged + "_origin";
		String result = tagged + "_result";
		String wrong  = tagged + "_wrong";
		boolean go = true;
		int n = start;
		String modelFormat = "%s_%d.%d.model";//type,x,n++
		String resultFormat = "[%d\t]%f=%d / %d\n";//accuracy,count,size
		double step = 0.2;
		int times = 0;
		try {
			new File(result).createNewFile();
			new File(wrong).createNewFile();
			List<String> taggedList = Files.readAllLines(Paths.get(tagged), cs);
			List<String> originList = Files.readAllLines(Paths.get(origin), cs);
			List<String> todoList   = new ArrayList<String>();
			List<String> wrongList  = new ArrayList<String>();
			//0 choose 20% of taggedList into todoList
			Collections.shuffle(taggedList);
			int count = (int)(step * taggedList.size());
			for(int i=0;i<count;i++) {
				todoList.add(taggedList.get(i));
			}
			int all = taggedList.size();
			while(go) {
				System.err.println("try " + (times++) + " times");
				//1 add 20% of wrong wrongList
				Collections.shuffle(wrongList);
				if(wrongList.size() > 0) {
					count = (int)Math.ceil((step * wrongList.size()));
					if(count == 1) {
						count = wrongList.size();
					}
					int added = 0;
					for(int i=0;i<wrongList.size();i++) {
						if(todoList.contains(wrongList.get(i))) continue;
						added ++;
						todoList.add(wrongList.get(i));
						if(added >= count) break;
					}
					wrongList.clear();
				}
				//2 turn todoList into step3
				tsv(todoList, step3);
				//3 train step3 to type-x.n.model
				String model = String.format(modelFormat, type, index, n++);
				train(step3, model);
				//4 use this model to detect originList result in resultList
				AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(model);
				int right =0;
				List<String> temp = new ArrayList<String>();
				temp.addAll(taggedList);
				for(String text : originList) {
					String tag = API.tag(classifier, text);
					if(taggedList.contains(tag)) {
						right ++;
						temp.remove(tag);
					}else {
						wrongList.add(tag);
					}
				}
				//5 compare resultList and taggedList result in wrongList
				System.err.println("accuracy:" + (double)right/all);
				//6 if wrongList is empty, go = false
				if(wrongList.isEmpty()) go = false;
				Files.write(Paths.get(tagged + n), todoList, cs, StandardOpenOption.CREATE_NEW);
				Files.write(Paths.get(result), String.format(resultFormat, times, (double)right/all, right, all).getBytes(), StandardOpenOption.APPEND);
				Files.write(Paths.get(wrong), wrongList, cs, StandardOpenOption.TRUNCATE_EXISTING);
				wrongList.clear();
				wrongList.addAll(temp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	public static void main(String[] args) {
		try {
			if(args.length < 3) {
				return;
			}
			tsv(args[0]);
			train(args[1], args[2]);
			
			tsv("D:/files/work/text/hotel/hotel_20170525.txt12");
//			train("D:/files/work/text/hotel/hotel_20170525.txt12_tsv", "D:/files/work/text/hotel/hotel_11.9.model");
//			optimize("D:/files/work/text/hotel/hotel_20170525.txt", 11, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
