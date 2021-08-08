//    Copyright (c) 2021 Burak Cetin
//
//    This file is part of OpenPAS.
//
//    OpenPAS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    OpenPAS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with OpenPAS.  If not, see <https://www.gnu.org/licenses/>.

package fopas;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.FluentIterable;

import fopas.basics.FOElement;
import fopas.basics.FOFiniteSet;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOTerm;
import fopas.basics.KnownIterable;

public class FOBridgeSet<T extends FOElement> implements FOFiniteSet<T>, Set<T>
{
	protected final String mName;
	protected final Set<T> mSet;
	protected final Class<T> mElementClass;
	
	FOBridgeSet(String name, Set<T> sourceSet, Class<T> elementClass)
	{
		mName = name;
		mSet = sourceSet;
		mElementClass = elementClass;
	}

	//------ Set incompleteness functionality ---------------------------------------------------
	protected boolean mIsIncompleteSuperset;
	@Override
	public boolean isIncompleteSuperset() { return mIsIncompleteSuperset; }
	@Override
	public void setIncompleteSuperset(boolean isIncomplete) { mIsIncompleteSuperset = isIncomplete; }	
	//-------------------------------------------------------------------------------------------

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
	public String getName()
	{
		return mName;
	}

	@Override
	public Class<T> getType() { return mElementClass;}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mSet == null) ? 0 : mSet.hashCode());
		return result;
	}

	/**
	 * This only relies on the bridged sets being equal - it ignores any given names since names are decorational for logical ops.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes") // it's appropriate to suppress this since typing is handled in set equality below.
		FOBridgeSet other = (FOBridgeSet) obj;
		if (mSet == null) {
			if (other.mSet != null)
				return false;
		} else if (!mSet.equals(other.mSet))
			return false;
		return true;
	}	
}
