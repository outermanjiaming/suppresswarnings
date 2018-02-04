package com.suppresswarnings.osgi.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Reaper {
	
	List<TTL> ttl = new ArrayList<TTL>();
	public void add(TTL one) {
		ttl.add(one);
	}
	public void sort(){
		Collections.sort(ttl);
	}
}
