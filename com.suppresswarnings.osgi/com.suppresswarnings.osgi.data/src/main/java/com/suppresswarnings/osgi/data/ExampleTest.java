package com.suppresswarnings.osgi.data;

import java.util.stream.Stream;

public class ExampleTest {
	public static void main(String[] args) throws Exception {
		ExampleShell shell         = new ExampleShell();
		ExampleContent content     = new ExampleContent();
		ExampleContext context     = new ExampleContext(content);
		context.init(ExampleState.S0);
		boolean x = Stream.generate(shell).anyMatch(context);
		System.out.println(x + " ==> " + context.output());
	}
}
