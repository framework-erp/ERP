package test.arp.core;

public class TestEntityCh implements TestItfCh {
	private TestEntityChCh oValue;
	private Integer iBoxValue;
	private Boolean bBoxValue;

	public TestEntityChCh getoValue() {
		return oValue;
	}

	public void setoValue(TestEntityChCh oValue) {
		this.oValue = oValue;
	}

	public Integer getiBoxValue() {
		return iBoxValue;
	}

	public void setiBoxValue(Integer iBoxValue) {
		this.iBoxValue = iBoxValue;
	}

	public Boolean getbBoxValue() {
		return bBoxValue;
	}

	public void setbBoxValue(Boolean bBoxValue) {
		this.bBoxValue = bBoxValue;
	}

	@Override
	public String toString() {
		return "TestEntityCh [oValue=" + oValue + ", iBoxValue=" + iBoxValue + ", bBoxValue=" + bBoxValue + "]";
	}

}
