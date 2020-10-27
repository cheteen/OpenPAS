package fopas.basics;

// We're only interested in enumarable sets in computations, so let's assume a set to be iterable up front.
public interface FOSet<T extends FOElement> extends Iterable<T> {

	int size();
	
}
