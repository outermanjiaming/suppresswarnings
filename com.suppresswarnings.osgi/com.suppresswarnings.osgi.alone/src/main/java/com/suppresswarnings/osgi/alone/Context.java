package com.suppresswarnings.osgi.alone;

import java.util.Random;
import java.util.function.Predicate;

/**
 * Context with state, holds a content to make decision
 * @author lijiaming
 *
 * @param <T>
 */
public abstract class Context<T> implements Predicate<String> {
	T content;
	State<Context<T>> state;
	String output;
	String time;
	String rand;
	public Context(T ctx) {
		this.content = ctx;
		this.time = "" + System.currentTimeMillis();
		this.rand = "" + new Random().nextInt(1000);
	}
	public void init(State<Context<T>> s) {
		this.state = s;
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
		return output;
	}
	public void println(String string) {
		this.output = string;
	}
	public State<Context<T>> state(){
		return state;
	}
	public void update(){
		this.time = "" + System.currentTimeMillis();
		this.rand = "" + new Random().nextInt(1000);
	}
	public abstract void log(String msg);
	@Override
	public boolean test(String t) {
		state = state.apply(t, this);
		state.accept(t, this);
		return state.finish();
	}
	@Override
	public String toString() {
		return "Context [content=" + content + ", state=" + state + ", output=" + output + ", time=" + time + ", rand=" + rand + "]";
	}
	
}
