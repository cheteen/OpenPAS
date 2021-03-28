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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;

import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOEnumerableSet;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOTerm;
import fopas.basics.KnownIterable;
import fopas.basics.FOUnionSet;

//TODO: Bring some order to this set impl.
public class FOEnumerableUnionSetImpl implements FOUnionSet
{
	protected Map<String, FOEnumerableSet<? extends FOElement>> mSubsets;
	
	// We could have proper support for <T extends FOElement> type in the future if needed.
	FOEnumerableUnionSetImpl(FOEnumerableSet<? extends FOElement> defaultSet)
	{
		mSubsets = new HashMap<>(2);
		mSubsets.put(defaultSet.getName(), defaultSet);
	}

	FOEnumerableUnionSetImpl(Set<FOEnumerableSet<? extends FOElement>> subsets)
	{
		mSubsets = new LinkedHashMap<>();
		for(FOEnumerableSet<? extends FOElement> foset : subsets)
			mSubsets.put(foset.getName(), foset);
	}

	@Override
	public Iterator<FOElement> iterator()
	{
		return Iterables.concat(mSubsets.values()).iterator();
	}

	@Override
	public int size()
	{
		int size = 0;
		for(FOSet<? extends FOElement> foset : mSubsets.values())
			if(foset.size() == Integer.MAX_VALUE)
				return Integer.MAX_VALUE; //infinite set
			else
				size += foset.size();
		return size;
	}

	//TODO: No, this set needs a name.
	@Override
	public String getName()
	{
		return "A"; // is there a point to have different names for the universe?
	}

	@Override
	public boolean contains(Object o)
	{
		for(FOSet<? extends FOElement> foset : mSubsets.values())
			if(foset.contains(o))
				return true;
		return false;
	}

	//TODO: Remove this, no need.
	@Override
	public FOSet<? extends FOElement> getOriginalSubset(String name)
	{
		return mSubsets.get(name);
	}

	@Override
	public FOSet complement(FOSet relativeSet)
	{
		// TODO Implement this.
		return null;
	}

	@Override
	public Class getType() { return FOElement.class; }
}
