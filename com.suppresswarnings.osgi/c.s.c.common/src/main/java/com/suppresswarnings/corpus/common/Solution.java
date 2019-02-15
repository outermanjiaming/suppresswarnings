/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.common;

import java.util.ArrayList;
import java.util.List;

public class Solution {
	class Dot {
		Integer value;
		List<Integer> follow;
		Dot parent;
		List<Dot> children;
		public Dot(Dot parent) {
			this.parent = parent;
			this.children = new ArrayList<>();
		}
		public void add(Dot e) {
			this.children.add(e);
			permutation(e, e.follow);
		}
		public void print(List<List<Integer>> results){
			if(children.size() == 0) {
				List<Integer> result = new ArrayList<>();
				Dot parent = this.parent;
				while(parent.parent != null) {
					result.add(parent.value);
					parent = parent.parent;
				}
				result.add(value);
				results.add(result);
			}
			for(Dot dot : children) {
				dot.print(results);
			}
		} 
		@Override
		public String toString() {
			return "["+ value+ "(" + children.size()+")]";
		}
	}
	  public void permutation(Dot parent, List<Integer> input) {
		    for(int index = 0;index < input.size();index++) {
		    	List<Integer> todo = new ArrayList<>();
		    	todo.addAll(input);
		    	Dot e = new Dot(parent);
		    	int v = todo.remove(index);
		    	List<Integer> follow = new ArrayList<>();
		    	follow.addAll(todo);
		    	e.value = v;
		    	e.follow = follow;
		    	parent.add(e);
		    }
	  }

	  private List<List<Integer>> permutations(List<Integer> input) {
	    List<List<Integer>> results = new ArrayList<List<Integer>>();
	    Dot dot = new Dot(null);
	    permutation(dot, input);
	    dot.print(results);
	    return results;
	  }

	  public static void main(String[] args) {
	     List<Integer> input = new ArrayList<Integer>();
	     input.add(1);
	     input.add(2);
	     input.add(3);
	     input.add(4);
	     input.add(5);
	     Solution solution = new Solution();
	     
	     List<List<Integer>> results = solution.permutations(input);
	     // (1, 2, 3) (1, 3, 2) (2, 1, 3) (2, 3, 1) (3, 1, 2) (3, 2, 1)
	     System.out.println(results); 
	     System.out.println(results.size());
	  }
	}
