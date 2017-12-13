package com.suppresswarnings.osgi.nn.cnn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Data {

	List<Row> rows = new ArrayList<Row>();
	public void put(Row row) {
		rows.add(row);
	}
	public Row get(int i) {
		return rows.get(i);
	}
	public int size(){
		return rows.size();
	}
	public void shuffle(){
		Collections.shuffle(rows);
	}
	public void clear(){
		rows.clear();
	}
}
