package arp.process.result;

public class PairProcessResult<A, B> {

	private A a;
	private B b;

	public static <A, B> PairProcessResult<A, B> instance(A a, B b) {
		PairProcessResult<A, B> rslt = new PairProcessResult<>();
		rslt.setA(a);
		rslt.setB(b);
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

}
