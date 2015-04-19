package utils;

import data.Complex;

public class FFT {

	public static Complex[] fft(Complex[] x) {
		
		int N = x.length;

		if (N == 1) return new Complex[] { x[0] };

		// Radix 2 Cooley-Tukey FFT
		if (N % 2 != 0) { throw new RuntimeException("N is not a power of 2"); }

		// FFT of even terms
		Complex[] even = new Complex[N/2];
		for (int k = 0; k < N/2; k++) {
			even[k] = x[2*k];
		}
		
		Complex[] q = fft(even);

		// FFT of odd terms
		Complex[] odd  = even;  // reuse the array
		for (int k = 0; k < N/2; k++) {
			odd[k] = x[2*k + 1];
		}
		
		Complex[] r = fft(odd);

		// Combine
		Complex[] y = new Complex[N];
		for (int k = 0; k < N/2; k++) {
			double kth = -2 * k * Math.PI / N;
			Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
			y[k]       = q[k].plus(wk.times(r[k]));
			y[k + N/2] = q[k].minus(wk.times(r[k]));
		}
		
    return y;
  }

}