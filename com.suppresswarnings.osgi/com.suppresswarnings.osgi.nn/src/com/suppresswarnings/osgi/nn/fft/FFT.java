package com.suppresswarnings.osgi.nn.fft;

public class FFT {
	public static Complex[] fft(Complex[] x) {
		int N = x.length;
		if(N == 1) return new Complex[] {x[0]};
		if(N % 2 != 0) throw new RuntimeException("N must be power of 2");
		int half = N / 2;
		Complex[] even = new Complex[half];
		for(int i=0; i<half; i++) {
			even[i] = x[2 * i];
		}
		Complex[] q = fft(even);
		Complex[] odd = new Complex[N / 2];
		for(int i=0; i<half; i++) {
			odd[i] = x[2 * i + 1];
		}
		Complex[] r = fft(odd);
		Complex[] y = new Complex[N];
		for(int i=0;i<half;i++) {
			double ith = -2 * i * Math.PI / N;
			Complex wi = new Complex(Math.cos(ith), Math.sin(ith));
			Complex times = wi.times(r[i]);
			y[i]      = q[i].plus(times);
			y[i+half] = q[i].minus(times);
		}
		return y;
	}
	
	public static Complex[] ifft(Complex[] x){
		int N = x.length;
		Complex[] y = new Complex[N];
		
		for(int i=0;i<N;i++) {
			y[i] = x[i].conjugate();
		}
		y = fft(y);
		
		for(int i=0;i<N;i++) {
			y[i] = y[i].conjugate();
		}
		double scale = 1.0/N;
		for(int i=0;i<N;i++) {
			y[i] = y[i].times(scale);
		}
		return y;
	}
	public static void show(Complex[] x, String title) {
        System.out.println(title);
        System.out.println("-------------------");
        for (int i = 0; i < x.length; i++) {
            System.out.println(x[i]);
        }
        System.out.println();
        Show show = new Show(title);
        show.init(x);
    }
	public static void main(String[] args) {
		int N = 64;
        Complex[] x = new Complex[N];

        double step = 2 * Math.PI / N; 
        // original data
        for(int i=0;i<N;i++) {
        	x[i] = new Complex(Math.cos(i*step) + 0.7*Math.cos(i*7*step), 0);
        }
        show(x, "x");

        // FFT of original data
        Complex[] y = fft(x);
        show(y, "y = fft(x)");

        // take inverse FFT
        Complex[] z = ifft(y);
        show(z, "z = ifft(y)");
	}
}
