package com.suppresswarnings.osgi.data;

public class ExampleContext extends Context<ExampleContent> {

	public ExampleContext(ExampleContent content, State<Context<ExampleContent>> s) {
		super(content, s);
	}

	@Override
	public boolean test(String t) {
		state = state.apply(t, this);
		state.accept(t, this);
		return content.auth();
	}

	@Override
	public String toString() {
		return "ExampleContext [content=" + content + ", state=" + state.name() + "]";
	}
	
}
