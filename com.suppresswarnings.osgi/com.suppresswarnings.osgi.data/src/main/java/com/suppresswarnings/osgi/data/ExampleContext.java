package com.suppresswarnings.osgi.data;

public class ExampleContext extends Context<ExampleContent> {

	public ExampleContext(ExampleContent content, State<Context<ExampleContent>> s) {
		super(content, s);
	}
	
	
	@Override
	public boolean test(String t) {
		boolean x = super.test(t);
		if(!x) {
			System.out.println(output());
		}
		return x;
	}


	@Override
	public String toString() {
		return "ExampleContext [content=" + content + ", state=" + state.name() + "]";
	}
}
