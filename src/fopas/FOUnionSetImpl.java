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
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOTerm;
import fopas.basics.KnownIterable;
import fopas.basics.FOUnionSet;

public class FOUnionSetImpl implements FOUnionSet
{
	protected Map<String, FOSet<? extends FOElement>> mSubsets;
	
	FOUnionSetImpl(FOSet<FOElement> defaultSet)
	{
		mSubsets = new HashMap<>(2);
		mSubsets.put(defaultSet.getName(), defaultSet);
	}

	FOUnionSetImpl(Set<FOSet<? extends FOElement>> subsets)
	{
		mSubsets = new LinkedHashMap<>();
		for(FOSet<? extends FOElement> foset : subsets)
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
			if(foset.size() == -1)
				return -1; //infinite set
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
}
