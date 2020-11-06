package fopas;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import fopas.basics.FOElement;
import fopas.basics.FOFiniteSet;
import fopas.basics.FORelation;
import fopas.basics.FOSet;

public class FOBridgeSet<T extends FOElement> implements FOFiniteSet<T> {

	protected Set<T> mSet;
	
	FOBridgeSet(Set<T> sourceSet)
	{
		mSet = sourceSet;
	}
	
	//-------------------------------------------------------------------------------------------
	// Set bridge functions
	//-------------------------------------------------------------------------------------------
	@Override
	public Iterator<T> iterator() {
		return mSet.iterator();
	}

	@Override
	public int size() {
		return mSet.size();
	}

	@Override
	public boolean isEmpty() {
		return mSet.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return mSet.contains(o);
	}

	@Override
	public Object[] toArray() {
		return mSet.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return mSet.toArray(a);
	}

	@Override
	public boolean add(T e) {
		return mSet.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return mSet.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return mSet.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return mSet.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return mSet.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return mSet.removeAll(c);
	}

	@Override
	public void clear() {
		mSet.clear();
	}
	//-------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------

	@Override
	public FOSet<T> sacrificeForSubset(FORelation<T> relation) {
		// TODO Auto-generated method stub
		return null;
	}
}
