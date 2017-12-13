package com.suppresswarnings.osgi.nn.cnn;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class Clock {
	class Record {
		String which;
		long time;
		public Record(String which, long time) {
			this.which = which;
			this.time = time;
		}
		public String getWhich() {
			return which;
		}
		public void setWhich(String which) {
			this.which = which;
		}
		public long getTime() {
			return time;
		}
		public void setTime(long time) {
			this.time = time;
		}
		
	}
	Deque<Record> stack = new ArrayDeque<Record>();
	Set<String> whiches = new HashSet<String>();
	public void start(String which){
		stack.push(new Record(which, System.currentTimeMillis()));
	}
	public void end(String which){
		stack.push(new Record(which, System.currentTimeMillis()));
		whiches.add(which);
	}
	public long get(String which) {
		Deque<Record> temp = new ArrayDeque<Record>();
		long end = 0;
		long start = 0;
		while(!stack.isEmpty()) {
			Record r = stack.pop();
			if(which.equals(r.which)) {
				end = r.time;
				break;
			} else {
				temp.push(r);
			}
		}
		while(!stack.isEmpty()) {
			Record r = stack.pop();
			if(which.equals(r.which)) {
				start = r.time;
				break;
			} else {
				temp.push(r);
			}
		}
		
		while(!temp.isEmpty()) {
			stack.push(temp.pop());
		}
		
		return end - start;
	}
	
	public void listAll() {
		for(String which : whiches) {
			long time = get(which);
			System.out.println("Cost " + time + "ms \tby " + which);
		}
	}
}
