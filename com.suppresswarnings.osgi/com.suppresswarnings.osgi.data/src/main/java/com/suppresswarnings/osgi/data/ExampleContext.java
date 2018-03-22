package com.suppresswarnings.osgi.data;

import com.suppresswarnings.osgi.alone.Context;

public class ExampleContext extends Context<ExampleContent> {

	public ExampleContext(ExampleContent content) {
		super(content);
	}

	
	@Override
	public void output(String string) {
		super.output(string);
		System.err.println("[Example] " + string);
	}


	@Override
	public void log(String msg) {
		System.out.println("[Example] " + msg);
	}
}
