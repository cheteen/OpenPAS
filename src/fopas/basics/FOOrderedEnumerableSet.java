package fopas.basics;

import java.util.Comparator;
import java.util.List;

public interface FOOrderedEnumerableSet<T extends FOElement> extends FOEnumerableSet<T>
{
	public FOOrderedEnumerableSet<T> constrainToRange(T first, T last);
	public int getConstrainedSize(FORelation<T> relation, List<FOTerm> terms);
	
	public Comparator<FOElement> getOrder();
	public T getFirstOrInfinite();
	public T getLastOrInfinite();
	
	public T getNextOrNull(T element);
	public T getPreviousOrNull(T element);
}
