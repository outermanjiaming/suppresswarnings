package com.suppresswarnings.osgi.alone;

import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class Context<T> implements Predicate<String>, Consumer<String> {
	public T context;
	public State state;
	public Context(State init, T ctx){
		this.state = init;
		this.context = ctx;
	}
	@Override
	public boolean test(String in) {
		state = state.to(in, this);
		state.accept(in, this);
		return state == State.Final;
	}
	
}
