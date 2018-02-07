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
	public Context(T ctx, State<Context<T>> s) {
		this.content = ctx;
		this.state = s;
	}
	public T getContent() {
		return content;
	}
}
