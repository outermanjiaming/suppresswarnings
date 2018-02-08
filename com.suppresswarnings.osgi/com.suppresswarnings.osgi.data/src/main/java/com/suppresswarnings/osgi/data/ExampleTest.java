package com.suppresswarnings.osgi.data;

import java.util.stream.Stream;

import com.suppresswarnings.osgi.alone.Context;

public class ExampleTest {
	public static void main(String[] args) throws Exception {
		ExampleShell shell         = new ExampleShell();
		ExampleContent content     = new ExampleContent();
		Context<?> context     = new ExampleContext(content, ExampleState.S0);
		boolean x = Stream.generate(shell).anyMatch(context);
		System.out.println(x + " ==> " + context.output());
	}
}
