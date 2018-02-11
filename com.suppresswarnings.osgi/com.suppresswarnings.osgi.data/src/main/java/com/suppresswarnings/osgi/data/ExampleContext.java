package com.suppresswarnings.osgi.data;

import com.suppresswarnings.osgi.alone.Context;

public class ExampleContext extends Context<ExampleContent> {

	public ExampleContext(ExampleContent content) {
		super(content);
	}

	@Override
	public void log(String msg) {
	}
}
