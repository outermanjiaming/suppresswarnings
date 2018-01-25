package com.suppresswarnings.osgi.wx;

import java.util.Scanner;
import java.util.function.Supplier;

public class Shell implements Supplier<String> {
	Scanner scan = new Scanner(System.in);

	@Override
	public String get() {
		return scan.nextLine();
	}

}
