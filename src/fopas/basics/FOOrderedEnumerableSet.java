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
	
	//public T getNextOrInfinite(T element);
	//public T getPreviousOrInfinite(T element);
}
