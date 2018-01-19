package com.suppresswarnings.osgi.nn.fsm;

import java.util.stream.Stream;

public class Test {

	public static void main(String[] args) {
		Context context = new Context();
		Shell shell = new Shell();
//		
//		while(true) {
//			String in = shell.get();
//			context.accept(in);
//			if(context.authentic()) break;
//		}
//		System.out.println(context);
//		
		boolean x = Stream.generate(shell).anyMatch(context);
		System.out.println(x);
		System.out.println(context);
	}
}
