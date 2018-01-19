package com.suppresswarnings.osgi.nn.fsm;

public interface State {
	public void accept(String in, Context context);
	public S to(String in, Context context);
}
