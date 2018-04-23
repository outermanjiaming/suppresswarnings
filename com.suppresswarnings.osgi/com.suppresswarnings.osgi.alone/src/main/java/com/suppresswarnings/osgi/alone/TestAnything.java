package com.suppresswarnings.osgi.alone;

public class TestAnything {

	public static void main(String[] args) {
		A a = new A();
		A.change2();
		a.change();
	}
}

class A {
	public static String staticA = "A";
	static {
		staticA = "C";
		System.out.println("1:"+staticA);
	}
	A () {
		staticA = "B";
		System.out.println("2:"+staticA);
	}
	public void change(){
		staticA = "D";
		System.out.println("3:"+staticA);
	}
	public static void change2(){
		staticA = "E";
		System.out.println("4:"+staticA);
	}
}