package com.suppresswarnings.osgi.data;

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
	public Context(T ctx, State<Context<T>> s) {
		this.content = ctx;
		this.state = s;
	}
	public T content() {
		return content;
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
	@Override
	public boolean test(String t) {
		state = state.apply(t, this);
		state.accept(t, this);
		return state.finish();
	}
}
