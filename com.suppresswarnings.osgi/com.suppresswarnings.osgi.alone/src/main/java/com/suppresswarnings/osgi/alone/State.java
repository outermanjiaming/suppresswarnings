package com.suppresswarnings.osgi.alone;

import java.io.Serializable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * FSM design
 * State<T> means the state of which holds the content T, which contains by the Context<T>
 * <br/>
 * It consumes the input String and the Context<T>, save some information or change some within content T
 * <br/>
 * It transfers its state via BiFunction's apply method, it may use the context to decide
 * <br/>
 * the name is for identity purpose
 * <br/>
 * <code>void accept(T t, U u)</code> get input and use u.println to output
 * <br/>
 * <code>R apply(T t, U u)</code> to transfer state
 * @author lijiaming
 *
 * @param <T>
 */
public interface State<T> extends Serializable, BiConsumer<String, T>, BiFunction<String, T, State<T>> {
	public String name();
	public boolean finish();
}
