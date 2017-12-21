package com.suppresswarnings.osgi.nn;

import java.util.BitSet;

public class Char2Vec {

	StringBuffer buffer = new StringBuffer();
	String dict;
	int size;
	boolean built = false;
	BitSet bitset = new BitSet(2>>Character.SIZE);
	public Char2Vec(){}
	public Char2Vec feed(String word) {
		built = false;
		char[] chars = word.toCharArray();
		for(char c : chars) {
			if(bitset.get(c)){
				System.out.println(c + " exists");
			} else {
				bitset.set(c);
				buffer.append(c);
			}
		}
		return this;
	}
	public void build(){
		this.dict = buffer.toString();
		this.size = buffer.length();
		built = true;
	}
	
	public double[] onehot(char c) {
		if(!built) return null;
		if(!bitset.get(c)) return null;
		int index = dict.indexOf(c);
		double[] result = new double[size];
		result[index] = 1;
		return result;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Char2Vec [dict=");
		builder.append(dict);
		builder.append(", size=");
		builder.append(size);
		builder.append(", built=");
		builder.append(built);
		builder.append("]");
		return builder.toString();
	}
	public char get(int index) {
		return dict.charAt(index);
	}
	public int size() {
		return size;
	}
	
}
