package com.suppresswarnings.osgi.data;

import java.util.stream.Stream;

public class ExampleTest {
	public static void main(String[] args) {
		ExampleShell shell = new ExampleShell();
		ExampleContent content = new ExampleContent();
		State<ExampleContent> init = ExampleState.S0;
		ExampleContext context = new ExampleContext(content, init);
		boolean x = Stream.generate(shell).anyMatch(context);
		System.out.println(x);
		System.out.println(context);
	}
}
