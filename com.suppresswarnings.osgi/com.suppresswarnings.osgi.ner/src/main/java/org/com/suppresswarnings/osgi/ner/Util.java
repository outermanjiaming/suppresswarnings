package org.com.suppresswarnings.osgi.ner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class Util {
	/**
	 * Uniform characters, UpperCase letter ->A, LowerCase letter ->a, Number ->1, Whitespace->#
	 * 『』 【】()（）. ->#
	 * @param word
	 * @return
	 */
	public static char uniform(char word) {
		if(word == '『' || word == '【' || word == '』' || word == '】'||word == '(' || word == ')'||word == '（' || word == '）' || word == '.' || word == '!') {
			word = '#';
		}
		else if(Character.isWhitespace(word)) word = '#';
		else if(Character.isDigit(word)) word = '1';
		else if(Character.isUpperCase(word)) word = 'A';
		else if(Character.isLowerCase(word)) word = 'a';
		else if(word == '、' || word == ',') word = '，';
		return word;
	}
	
	/**
	 * keep 1 space, for instance: 2015-01-01 10:01 should keep the space.
	 * @param result
	 * @return
	 */
	public static String keep1Space(String result) {
		return result.replace("  ", "`").replace(" ", "").replace("`", " ");
	}
	
	public static String removeLastLeftBraces(String result) {
		if(result.endsWith("(")) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}
	
	
	public static String format(String text, boolean uniform) {
		StringBuffer sb = new StringBuffer();
		for(char c : text.toCharArray()) {
			if(uniform) sb.append(Util.uniform(c));
			else sb.append(c);
			sb.append(' ');
		}
		return sb.toString();
	}
	
//	@Deprecated
	public static void CRF(List<String> inputdata, String output) {
		Path outputpath = Paths.get(output);
		StringBuffer crf = new StringBuffer();
		int js = 0;
		for(String i : inputdata){
			System.out.println(++js*1.0/inputdata.size() * 100.0 + "%");
			String[] temp = i.split("[{]"); 
			if(temp.length == 1){
				continue;
			}
			for(int y = 0;y<temp.length; y++){
				String yString = temp[y];
				boolean key = true;
				
				if(yString.length() == 0) continue;
				if(yString.charAt(yString.length()-1) == '}'){
					key = false;
				}
				String[] ttemp = yString.split("[}]");
				if(ttemp.length < 2 && key){
					for(int x = 0;x<ttemp[0].length();x++){
						crf.append(Util.uniform(ttemp[0].charAt(x))).append("\tO").append("\n");
					}
					continue;
				}
				String[] dtemp = ttemp[0].split("[|]");
				for(int x = 0;x<dtemp[0].length();x++){
					crf.append(Util.uniform(ttemp[0].charAt(x))).append("\t").append(dtemp[1]).append("\n");
				}
				if(ttemp.length == 1){
					continue;
				}
				temp[y] =  ttemp[1];
				y--;
			}
			crf.append("\n");
		}
		try {
			Files.write(outputpath, crf.toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void Origin(List<String> inputdata, String origin) {
		Path originpath = Paths.get(origin);
		StringBuffer text = new StringBuffer();
		
		int js = 0;
		for(String i : inputdata){
			System.out.println(++js*1.0/inputdata.size() * 100.0 + "%");
			String[] temp = i.split("[{]"); 
			if(temp.length == 1){
				continue;
			}
			for(int y = 0;y<temp.length; y++){
				String yString = temp[y];
				boolean key = true;
				
				if(yString.length() == 0) continue;
				if(yString.charAt(yString.length()-1) == '}'){
					key = false;
				}
				String[] ttemp = yString.split("[}]");
				if(ttemp.length < 2 && key){
					for(int x = 0;x<ttemp[0].length();x++){
						text.append(ttemp[0].charAt(x));
					}
					continue;
				}
				String[] dtemp = ttemp[0].split("[|]");
				for(int x = 0;x<dtemp[0].length();x++){
					text.append(ttemp[0].charAt(x));
				}
				if(ttemp.length == 1){
					continue;
				}
				temp[y] =  ttemp[1];
				y--;
			}
			text.append("\n");
		}
		try {
			Files.write(originpath, text.toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
