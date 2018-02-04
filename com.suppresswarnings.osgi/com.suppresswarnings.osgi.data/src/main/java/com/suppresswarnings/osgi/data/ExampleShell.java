package com.suppresswarnings.osgi.data;

import java.util.Scanner;
import java.util.function.Supplier;

public class ExampleShell implements Supplier<String> {
	Scanner scan = new Scanner(System.in);

	@Override
	public String get() {
		return scan.nextLine();
	}


}
