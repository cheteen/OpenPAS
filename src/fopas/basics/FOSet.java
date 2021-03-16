package fopas.basics;

import java.util.List;
import java.util.Set;

// We're only interested in enumarable sets in computations, so let's assume a set to be iterable up front - though this will change.
public interface FOSet<T extends FOElement>
{
	public int size(); // shared with Set, may return infinite (Integer.MAX_VALUE).
	
	public String getName();
	
	public boolean contains(Object o);
	
	/**
	 * This is to give the complement set: relativeSet \ this
	 * @param relativeSet
	 * @return A set containing elements that are in the relativeSet but not in this set.
	 */
	public FOSet<T> complement(FOSet<T> relativeSet);
	public default FOSet<T> complement(FOSet<T> relativeSet, boolean isComplement)
	{
		if(isComplement)
			return complement(relativeSet);
		else
			return this;
	}
	
	Class<T> getType();
}
