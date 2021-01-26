package fopas.basics;

import java.util.Comparator;
import java.util.List;

public interface FOEnumerableSet<T extends FOElement> extends FOSet<T>
{
	public FOEnumerableSet<T> constrainToRange(T first, T last);
	public int getConstrainedSize(FORelation<T> relation, List<FOTerm> terms);
	public T getFirstElement();
	/**
	 * This may return null if this is an infinite set.
	 * @return the last element in this set according to the enumeration order.
	 */
	public T getLastElement();
}
