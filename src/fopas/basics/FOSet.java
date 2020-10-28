package fopas.basics;

// We're only interested in enumarable sets in computations, so let's assume a set to be iterable up front.
public interface FOSet<T extends FOElement> extends Iterable<T>
{
	int size();
	
	/**
	 * Sacrifice this set to create a subset using this relation. Can return this set after possible modifying self.
	 */
	FOSet<T> sacrificeForSubset(FORelation<T> relation);
}
