package arp.process.result;

public class TripletProcessResult<A, B, C> {

	private A a;
	private B b;
	private C c;

	public static <A, B, C> TripletProcessResult<A, B, C> instance(A a, B b, C c) {
		TripletProcessResult<A, B, C> rslt = new TripletProcessResult<>();
		rslt.setA(a);
		rslt.setB(b);
		rslt.setC(c);
		return rslt;
	}

	public A getA() {
		return a;
	}

	public void setA(A a) {
		this.a = a;
	}

	public B getB() {
		return b;
	}

	public void setB(B b) {
		this.b = b;
	}

	public C getC() {
		return c;
	}

	public void setC(C c) {
		this.c = c;
	}

}
