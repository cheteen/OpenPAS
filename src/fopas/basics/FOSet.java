package fopas.basics;

import java.util.Set;

// We're only interested in enumarable sets in computations, so let's assume a set to be iterable up front.
public interface FOSet<T extends FOElement> extends Iterable<T>
{
	public int size(); // shared with Set
	
	public String getName();
	
	public boolean contains(Object o);
	
	/**
	 * @return Can return null if no good way to create a subset.
	 */
	public FOSet<T> createSubset(FORelation<T> relation);
	
	/**
	 * Find the size of the subset if this set was constrained to have members that satisified {@code rel}.
	 * @return Can return (-1) if size would be unkown, natural number otherwise.
	 */
	public int getSubsetSize(FORelation<T> relation);
}
