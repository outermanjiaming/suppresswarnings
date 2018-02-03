package com.suppresswarnings.osgi.alone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Format {
	public Format(){}
	public Format(String[] rules) {
		for(String rule : rules) {
			compile(rule);
		}
	}
	boolean debug = false;
	Map<Integer,Node> normal = new HashMap<Integer,Node>();
	Map<Integer,Node> keyfirst = new HashMap<Integer,Node>();
	
	public static void main(String[] args) {
		Format f = new Format();
		f.compile("{Version}:{uid}:{Attr}:{Index}={value}");
		List<KeyValue> result = f.matches("V1:012311355a323c3dc:Data:Index=V1:Data:1514325634132:01252:-12435133");
		System.out.println(result);
	}
	
	public void compile(String template) {
		if(template == null || template.length() <= 2) return;
		int index = template.lastIndexOf(Node.end);
		index = Math.min(index+2, template.length());
		char[] ctemplate = template.substring(0, index).toCharArray();
		Map<Integer,Node> temp = ctemplate[0] == Node.start ? keyfirst : normal;
		Node node = null, last = null;
		boolean keywordStart = false, keywordEnd = false, keyFirst = false;
		
		for(char c : ctemplate) {
			if(debug) System.out.print(c);
			//for every cases if keyword started and not ended yet, gather the keywords.
			if(keywordStart && !keywordEnd && c != Node.end) {
				last.keyword(c);
				continue;
			}
			//for the case of starting with keyword
			if(last == null && c == Node.start) {
				last = new Node(c);
				keyFirst = keywordStart = true;
				continue;
			}
			
			node = temp.get((int)c);
			if(node != null) {
				temp = node.leaf;
				if(keywordEnd) keywordEnd = false;
			} else {
				node = new Node(c);
				if(node.keywords) {
					if(keywordEnd) {
						last.name(last.nameBuffer);
						last.nameBuffer = null;
						temp.put((int)Node.stop, last);
						keywordEnd = false;
						last = node;
					}
					keywordStart = true;
					last.keywords = true;
					//keyword is just started and of course not finished yet, so just pass it.
					continue;
				} else if(node.keyworde) {
					keywordEnd = true;
					keywordStart = false;
					if(last.keywords) {
						//passing on the nameBuffer from last to now node
						node.nameBuffer = last.nameBuffer;
						if(debug) System.out.println(last.nameBuffer.toString());
						last.nameBuffer = null;
					}
				}
				
				//if last one is end of keyword but now is not, so make this one to be the end of keyword, and switch the keywordEnd off.
				if(keywordEnd && !node.keyworde) {
					keywordEnd = false;
					node.keyworde = true;
					//the nameBuffer passed on from the first node to the '}' node and now pass to the node next to it.
					if(keyFirst && last.keyworde) {
						keyFirst = false;
					}
					
					if(last.nameBuffer != null){
						node.name(last.nameBuffer);
						last.nameBuffer = null;
					}
				}
				//after all movement has been done, check the two switches , if the node is out of the keyword's range, just put it into the tree.
				if(!keywordStart && !keywordEnd) {
					temp.put((int)c, node);
					temp = node.leaf;
				}
			}
			//keep the node as the last one
			last = node;
		}
		//if the last is keyworde , add an endNode
		if(keywordEnd && last.nameBuffer != null) {
			keywordEnd = false;
			temp.put((int)Node.stop, node);
			node.name(last.nameBuffer);
			last.nameBuffer = null;
		}
	}
	
	public List<KeyValue> matches(String sms) {
		List<KeyValue> result = matches(normal, sms);
		if(result.isEmpty()) result = matches(keyfirst, sms);
		return result;
	}
	
	public void list() {
		int depth = 0;
		list(normal,depth);
		list(keyfirst,depth);
	}
	
	public static void list(Map<Integer, Node> leaf, int depth) {
		for(Map.Entry<Integer, Node> entry : leaf.entrySet()) {
			traverse(entry.getValue(), depth);
		}
	}
	public static void traverse(Node node, int depth) {
		depth ++;
		System.out.println(get(depth) + node.toString());
		list(node.leaf, depth);
	}
	
	public static String get(int d) {
		StringBuffer sb = new StringBuffer();
		while(d -- >0) sb.append('-');
		sb.append('-');
		return sb.toString();
	}
	
	private static List<KeyValue> matches(Map<Integer,Node> root, String sms) {
		List<KeyValue> result = new ArrayList<KeyValue>();
		if(sms == null || sms.length() <= 2 || root == null || root.isEmpty()) return result;
		char[] ctext = sms.toCharArray();
		Map<Integer,Node> temp = root;
		Node node = null, last = null;
		boolean keyFirst = true;
		StringBuffer keyword = new StringBuffer(), keywordStart = new StringBuffer();
		
		for(char c : ctext) {
			node = temp.get((int)c);
			if(last != null && last.keywords) {
				keyFirst = false;
				if(node == null || !node.keyworde) {
					keyword.append(c);
				}
			}
			
			if(node != null) {
				if(node.keyworde) {
					if(keyFirst && keywordStart.length() > 0) {
						result.add(new KeyValue(node.name() , keywordStart.toString()));
						keywordStart.setLength(0);
						keyFirst = false;
					}
					if(keyword.length() >0) {
						result.add(new KeyValue(node.name() , keyword.toString()));
						keyword.setLength(0);
					}
				}
				temp = node.leaf;
				last = node;
			}
			if(keyFirst) keywordStart.append(c);
		}
		node = temp.get((int)Node.stop);
		//in case it is the last one
		if(node != null){
			last = node;
			if(keyword.length() > 0) {
				result.add(new KeyValue(last.name() , keyword.toString()));
				keyword.setLength(0);
			}
		}
		
		
		return result;
	}
	
	
	public static class KeyValue {
		String key;
		String value;
		public KeyValue(String key, String value) {
			this.key = key;
			this.value = value;
		}
		
		public String key() {
			return key;
		}

		public String value() {
			return value;
		}

		@Override
		public String toString() {
			return key + "=" + value;
		}
	}
	
	private static class Node {
		static char start = '{';
		static char end = '}';
		static char stop = '$';
		//keyword started
		boolean keywords = false;
		//keyword ended
		boolean keyworde = false;
		//the keyword nameBuffer
		StringBuffer nameBuffer;
		String name;
		//the char
		char c;
		//its leafs
		Map<Integer,Node> leaf;
		Node(char c){
			this.c = c;
			if(c == start) {
				keywords = true;
			} else if(c == end) {
				keyworde = true;
			}
			leaf = new HashMap<Integer,Node>();
		}
		
		String name(){
			return name;
		}
		void name(StringBuffer nameBuffer) {
			this.name = nameBuffer == null ? "null" : nameBuffer.toString();
		}
		
		void keyword(char c) {
			if(nameBuffer == null) nameBuffer = new StringBuffer();
			nameBuffer.append(c);
		}

		@Override
		public String toString() {
			return ((keywords || keyworde) ? ((name == null) ? "" : name) + ((keyworde) ? "结束)" : "") : "") + ((keywords) ? "(开始" : "") + "[" + (char) c + "]-->" + leaf.size();
		}
	}
}