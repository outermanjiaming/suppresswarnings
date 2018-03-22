/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.osgi.alone;

import java.text.DecimalFormat;

public class Complex {
	DecimalFormat format = new DecimalFormat("0.00");
	double cos,sin;
	public Complex(double cos, double sin) {
		this.cos = cos;
		this.sin = sin;
	}
	public Complex times(Complex times) {
		return new Complex(this.cos *times.cos-this.sin * times.sin, this.cos*times.sin+this.sin*times.cos);
	}
	public Complex plus(Complex plus) {
		return new Complex(this.cos + plus.cos, this.sin + plus.sin);
	}
	public Complex minus(Complex minus) {
		return new Complex(this.cos - minus.cos, this.sin - minus.sin);
	}
	public Complex conjugate() {
		return new Complex(this.cos, -this.sin);
	}
	public Complex scale(double d) {
		return new Complex(d*this.cos, d*this.sin);
	}
	@Override
	public String toString() {
		return "(" + format.format(cos) + ", " + format.format(sin) + "i)";
	}

}
