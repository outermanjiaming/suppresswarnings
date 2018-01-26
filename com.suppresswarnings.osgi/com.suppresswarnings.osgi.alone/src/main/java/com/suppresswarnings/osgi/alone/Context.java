package com.suppresswarnings.osgi.alone;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Context holds the information over the whole conversation.
 * It feeds with String and push transfer the State, 
 * And the new State consumes the String, it may call the Context to accept informations.
 * If the new State is Final, return true.
 * 
 * @author lijiaming
 *
 * @param <T>
 */
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
