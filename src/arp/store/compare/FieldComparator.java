package arp.store.compare;

public interface FieldComparator {

	public <T> boolean equals(T one, T another);

}
