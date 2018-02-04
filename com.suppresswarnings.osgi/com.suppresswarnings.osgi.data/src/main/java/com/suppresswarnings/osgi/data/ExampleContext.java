package com.suppresswarnings.osgi.data;

public class ExampleContext extends Context<ExampleContent> {

	public ExampleContext(ExampleContent ctx, State<ExampleContent> s) {
		super(ctx, s);
	}

	@Override
	public boolean test(String t) {
		state = state.apply(t, content);
		state.accept(t, content);
		return content.auth();
	}

	@Override
	public String toString() {
		return "ExampleContext [content=" + content + ", state=" + state.name() + "]";
	}
	
}
