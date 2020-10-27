package fopas;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import fopas.basics.FOElement;
import fopas.basics.FOSet;

public class FOTreeSet<T extends FOElement> implements FOSet<T> {

	protected TreeSet<T> mSet;
	
	FOTreeSet()
	{
		mSet = new TreeSet<>();
	}
	
	@Override
	public Iterator<T> iterator() {
		return mSet.iterator();
	}

	@Override
	public int size() {
		return mSet.size();
	}

}
