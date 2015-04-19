package data;

public class Complex {
	 private final double re;
	 private final double im;   
	 
	 public Complex(double real, double imag) {
	        re = real;
	        im = imag;
	 }
	 
	 public double abs()   { return Math.hypot(re, im); }
	 public double phase() { return Math.atan2(im, re); }  

	 public Complex plus(Complex b) {      
	        double real = this.re + b.re;
	        double imag = this.im + b.im;
	        return new Complex(real, imag);
	 }
	 
	 public Complex minus(Complex b) {
	        double real = this.re - b.re;
	        double imag = this.im - b.im;
	        return new Complex(real, imag);
	 }
	 

	 public Complex times(Complex b) {
	        double real = this.re * b.re - this.im * b.im;
	        double imag = this.re * b.im + this.im * b.re;
	        return new Complex(real, imag);
	 }

	 public Complex times(double alpha) {
	        return new Complex(alpha * re, alpha * im);
	 }
	 
	 public Complex conjugate() {  return new Complex(re, -im); }

	 public Complex reciprocal() {
	        double scale = re*re + im*im;
	        return new Complex(re / scale, -im / scale);
	 }
	 
	 public double re() { return re; }
	 public double im() { return im; }
	 
	 public Complex divides(Complex b) {
	        Complex a = this;
	        return a.times(b.reciprocal());
	 }

	 public Complex exp() {
	        return new Complex(Math.exp(re) * Math.cos(im), Math.exp(re) * Math.sin(im));
	 }

	 public Complex sin() {
	        return new Complex(Math.sin(re) * Math.cosh(im), Math.cos(re) * Math.sinh(im));
	 }

	 public Complex cos() {
	        return new Complex(Math.cos(re) * Math.cosh(im), -Math.sin(re) * Math.sinh(im));
	 }

	 public Complex tan() {
	        return sin().divides(cos());
	 }
	    
	 public static Complex plus(Complex a, Complex b) {
	        double real = a.re + b.re;
	        double imag = a.im + b.im;
	        Complex sum = new Complex(real, imag);
	        return sum;
	 }
}
