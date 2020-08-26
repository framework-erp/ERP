package test.arp.core;

public class TestEntity {

	private int id;
	private int iValue;
	private String sValue;
	private TestItfCh itf;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getiValue() {
		return iValue;
	}

	public void setiValue(int iValue) {
		this.iValue = iValue;
	}

	public String getsValue() {
		return sValue;
	}

	public void setsValue(String sValue) {
		this.sValue = sValue;
	}

	public TestItfCh getItf() {
		return itf;
	}

	public void setItf(TestItfCh itf) {
		this.itf = itf;
	}

	@Override
	public String toString() {
		return "TestEntity [id=" + id + ", iValue=" + iValue + ", sValue=" + sValue + ", itf=" + itf + "]";
	}

}
