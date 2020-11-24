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
import fopas.basics.KnownIterable;
import fopas.basics.FOCombinedSet;

public class FOUnionSetImpl implements FOCombinedSet
{
	protected FOSet<FOElement> mDefault;
	protected Map<String, FOSet<FOElement>> mSubsets;
	
	FOUnionSetImpl(FOSet<FOElement> defaultSet)
	{
		mDefault = defaultSet;
		mSubsets = new HashMap<>(2);
		mSubsets.put(defaultSet.getName(), defaultSet);
	}

	FOUnionSetImpl(FOSet<FOElement> defaultSet, Set<FOSet<FOElement>> subsets)
	{
		mDefault = defaultSet;
		mSubsets = new LinkedHashMap<>();
		mSubsets.put(mDefault.getName(), mDefault); // default set is a subset as well.
		for(FOSet<FOElement> foset : subsets)
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
		for(FOSet<FOElement> foset : mSubsets.values())
			if(foset.size() == -1)
				return -1; //infinite set
			else
				size += foset.size();
		return size;
	}

	@Override
	public String getName()
	{
		return "A"; // is there a point to have different names for the universe?
	}

	@Override
	public boolean contains(Object o)
	{
		for(FOSet<FOElement> foset : mSubsets.values())
			if(foset.contains(o))
				return true;
		return false;
	}

	@Override
	public FOSet<FOElement> getOriginalSubset(String name)
	{
		return mSubsets.get(name);
	}

	@Override
	public int getSubsetSize(FORelation rel) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public FOSet createSubset(FORelation relation) {
		// TODO Auto-generated method stub
		return null;
	}

	static class FOUnionSetSubsetImpl implements FOSet<FOElement>
	{

		@Override
		public Iterator<FOElement> iterator() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean contains(Object o) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public FOSet<FOElement> createSubset(FORelation<FOElement> rel) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getSubsetSize(FORelation<FOElement> rel) {
			// TODO Auto-generated method stub
			return 0;
		}
	}
}
