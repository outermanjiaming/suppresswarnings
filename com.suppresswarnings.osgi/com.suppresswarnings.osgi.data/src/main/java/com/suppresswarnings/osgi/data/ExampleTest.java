package com.suppresswarnings.osgi.data;

import java.util.stream.Stream;

public class ExampleTest {
	public static void main(String[] args) throws Exception {
		ExampleShell shell         = new ExampleShell();
		ExampleContent content     = new ExampleContent();
		State<Context<ExampleContent>> init = new State<Context<ExampleContent>>() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 8838543767339719170L;

			@Override
			public State<Context<ExampleContent>> apply(String t, Context<ExampleContent> u) {
				if("final".equals(t)) 
					return ExampleState.Final;
				if("S0".equals(t))
					return ExampleState.S0;
				return this;
			}
			
			@Override
			public void accept(String t, Context<ExampleContent> u) {
				u.println(t + " = " + u.content.username);
			}
			
			@Override
			public String name() {
				return "hahaha this what";
			}

			@Override
			public boolean finish() {
				return false;
			}
		};
		
		Context<?> context     = new ExampleContext(content, init);
		boolean x = Stream.generate(shell).anyMatch(context);
		System.out.println(x + " ==> " + context.output());
	}
}
