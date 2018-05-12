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

import java.util.Random;
import java.util.function.Predicate;

import org.slf4j.LoggerFactory;

/**
 * Context with state, holds a content to make decision
 * @author lijiaming
 *
 * @param <T>
 */
public abstract class Context<T> implements Predicate<String> {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	T content;
	protected State<Context<T>> state;
	StringBuffer output = new StringBuffer();
	String time;
	String rand;
	public Context(T ctx) {
		this.content = ctx;
		this.time = "" + System.currentTimeMillis();
		this.rand = "" + new Random().nextInt(1000);
	}
	public T content() {
		return content;
	}
	public String time(){
		return time;
	}
	public String random() {
		return rand;
	}
	public String output() {
		String out = this.output.toString();
		this.output.setLength(0);
		return out;
	}
	public void output(String string) {
		this.output.setLength(0);
		this.output.append(string);
	}
	public Context<T> appendLine(String line) {
		if(this.output.length() > 0) {
			this.output.append("\n");
		}
		this.output.append(line);
		return this;
	}
	public State<Context<T>> state(){
		return state;
	}
	public void update(){
		this.time = "" + System.currentTimeMillis();
		this.rand = "" + new Random().nextInt(1000);
	}
	
	@Override
	public boolean test(String t) {
		
		try {
			logger.info(random() + "<-state:" + state);
			state = state.apply(t, this);
			logger.info(random() + "->state:" + state);
			state.accept(t, this);
		} catch (Exception e) {
			this.appendLine("没事，处理数据出了点问题: " + e.getMessage());
		}
		
		return state.finish();
	}
	@Override
	public String toString() {
		return "Context [content=" + content + ", state=" + state + ", output=" + output + ", time=" + time + ", rand=" + rand + "]";
	}
	
}
