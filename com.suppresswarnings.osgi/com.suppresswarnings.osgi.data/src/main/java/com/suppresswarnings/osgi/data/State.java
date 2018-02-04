package com.suppresswarnings.osgi.data;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * FSM design
 * State<T> means the state of which holds the content T, which contains by the Context<T>
 * It consumes the input String and the Context<T>, save some information or change some within content T
 * It transfers its state via BiFunction's apply method, it may use the context to decide
 * the name is for identity purpose
 * @author lijiaming
 *
 * @param <T>
 */
public interface State<T> extends BiConsumer<String, T>, BiFunction<String, T, State<T>> {
	public String name();
}
