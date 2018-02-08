package com.suppresswarnings.osgi.data;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;

public class ExampleContext extends Context<ExampleContent> {

	public ExampleContext(ExampleContent content, State<Context<ExampleContent>> s) {
		super(content, s);
	}
}
