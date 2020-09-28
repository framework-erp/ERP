package arp.repository.compare;

public interface FieldComparator {

	public <T> boolean equals(T one, T another);

}
