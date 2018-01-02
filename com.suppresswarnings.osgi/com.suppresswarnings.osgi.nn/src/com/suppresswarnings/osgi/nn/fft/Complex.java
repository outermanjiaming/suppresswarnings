package com.suppresswarnings.osgi.nn.fft;

import java.text.DecimalFormat;

public class Complex {
	double r;
	double i;
	public Complex(double real, double image) {
		this.r = real;
		this.i = image;
	}
	public double abs(){
		return Math.hypot(r, i);
	}
	public double phase(){
		return Math.atan2(r, i);
	}
	public Complex plus(Complex other) {
		return new Complex(this.r + other.r, this.i + other.i);
	}
	public Complex minus(Complex other) {
		return new Complex(this.r - other.r, this.i - other.i);
	}
	public Complex times(Complex other) {
		return new Complex(this.r * other.r - this.i * other.i, this.r * other.i + this.i * other.r);
	}
	public Complex reciprocal(){
		double scale = r * r + i * i;
		return new Complex(r/scale, -i/scale);
	}
	public Complex divides(Complex other) {
		return this.times(other.reciprocal());
	}
	public Complex times(double alpha) {
		return new Complex(r * alpha, i * alpha);
	}
	public Complex conjugate(){
		return new Complex(r, -i);
	}
	public Complex exp(){
		return new Complex(Math.exp(r) * Math.cos(i), Math.exp(r) * Math.sin(i));
	}
	public Complex sin() {
		return new Complex(Math.sin(r) * Math.cosh(i), Math.cos(r) * Math.sinh(i));
	}
	public Complex cos(){
		return new Complex(Math.cos(r) * Math.cosh(i), -Math.sin(r) * Math.sinh(i));
	}
	public Complex tan(){
		return sin().divides(cos());
	}
	public static void main(String[] args) {
        Complex a = new Complex(5.0, 6.0);
        Complex b = new Complex(-3.0, 4.0);
        System.out.println("a            = " + a);
        System.out.println("b            = " + b);
        System.out.println("Real(a)       = " + a.r());
        System.out.println("Image(a)      = " + a.i());
        System.out.println("b + a        = " + b.plus(a));
        System.out.println("a - b        = " + a.minus(b));
        System.out.println("a * b        = " + a.times(b));
        System.out.println("b * a        = " + b.times(a));
        System.out.println("a / b        = " + a.divides(b));
        System.out.println("(a / b) * b  = " + a.divides(b).times(b));
        System.out.println("conj(a)      = " + a.conjugate());
        System.out.println("|a|          = " + a.abs());
        System.out.println("tan(a)       = " + a.tan());
//      a            = (5.0,6.0i)
//		b            = (-3.0,4.0i)
//		Real(a)       = 5.0
//		Image(a)      = 6.0
//		b + a        = (2.0,10.0i)
//		a - b        = (8.0,2.0i)
//		a * b        = (-39.0,2.0i)
//		b * a        = (-39.0,2.0i)
//		a / b        = (0.36,-1.52i)
//		(a / b) * b  = (5.0,6.0i)
//		conj(a)      = (5.0,-6.0i)
//		|a|          = 7.810249675906654
//		tan(a)       = (-6.685231390246571E-6,1.0000103108981198i)

    }
	public double i() {
		return i;
	}
	public double r() {
		return r;
	}
	DecimalFormat d = new DecimalFormat("#.####");
	@Override
	public String toString() {
		return "(" + d.format(r) + "," + d.format(i) + "i)";
	}
	
}
