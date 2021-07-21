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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

import fopas.FOSetUtils.EmptySet;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOEnumerableSet;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOTerm;
import fopas.basics.KnownIterable;
import fopas.basics.FOUnionSet;

// Can split this in a regular set and this being an extension of it if needed.
public class FOEnumerableUnionSetImpl<T extends FOElement> implements FOUnionSet<T>, FOEnumerableSet<T> {
	// TODO: Implement this using a tree set, this as a natural order that can be
	// best represented as a tree (i.e the inheritance structure).
	// Need to implement an inheritance comparator for that.
	protected final Map<String, FOEnumerableSet<? extends T>> mSubsets;
	protected final Class<T> mEltType;
	protected final String mName;

	FOEnumerableUnionSetImpl(Collection<FOEnumerableSet<? extends T>> subsets, Class<T> eltType)
			throws FOConstructionException {
		this(subsets, eltType, null);
	}

	FOEnumerableUnionSetImpl(Collection<FOEnumerableSet<? extends T>> subsets, Class<T> eltType, String name)
			throws FOConstructionException {
		if (subsets.size() < 2)
			throw new FOConstructionException("Can't create union set with fewer than 2 sets");

		mEltType = eltType;
		mName = name;

		mSubsets = validateAndCreateNamedSubset(subsets);
	}

	protected FOEnumerableUnionSetImpl(Map<String, FOEnumerableSet<? extends T>> namedSubsets, Class<T> eltType,
			String name) throws FOConstructionException {
		assert !(namedSubsets.size() < 2);
		if (namedSubsets.size() < 2)
			throw new FOConstructionException("Can't create union set with fewer than 2 sets");

		mSubsets = namedSubsets; // this constructor is protected because we want this named subset to be
									// validated already (but don't want to write extra code to ensure that).
		mEltType = eltType;
		mName = name; // this can be null since this is an internal constructor
	}

	private Map<String, FOEnumerableSet<? extends T>> validateAndCreateNamedSubset(
			Iterable<FOEnumerableSet<? extends T>> subsets) throws FOConstructionException {
		Map<String, FOEnumerableSet<? extends T>> namedSubsets = new LinkedHashMap<>();
		for (FOEnumerableSet<? extends T> foset : subsets) {
			if (!mEltType.isAssignableFrom(foset.getType()))
			{
				assert false; // this should never happen
				throw new FOConstructionException("Union set type isn't an ancestor for subset: " + foset);
			}

			// Check that we don't already have a set that shares lineage with this one.
			// If we allowed that, then the constituents sets in this subset could possibly
			// share members.
			// This kind of member sharing should be handled within type specific sets
			// instead.
			for (FOEnumerableSet<? extends T> setin : namedSubsets.values())
				if (setin.getType().isAssignableFrom(foset.getType())
						|| foset.getType().isAssignableFrom(setin.getType()))
					throw new FOConstructionException(
							"Consitituent sets in a union set can't have related types: " + setin);

			if (namedSubsets.put(foset.getName(), foset) != null)
				throw new FOConstructionException("Name collision while creating union set for set: " + foset);
		}
		return namedSubsets;
	}

	@Override
	public Iterator<T> iterator() {
		return Iterables.concat(mSubsets.values()).iterator();
	}

	@Override
	public int size() {
		int size = 0;
		for (FOSet<? extends FOElement> foset : mSubsets.values())
			if (foset.size() == Integer.MAX_VALUE)
				return Integer.MAX_VALUE; // infinite set
			else
				size += foset.size();
		return size;
	}

	@Override
	public String getName() {
		if (mName != null)
			return mName;
		StringBuffer sb = new StringBuffer();
		Iterator<String> it = mSubsets.keySet().iterator();
		sb.append(it.next());
		while (it.hasNext()) {
			sb.append(" U ");
			sb.append(it.next());
		}

		return sb.toString();
	}

	@Override
	public boolean contains(Object o) {
		for (FOSet<? extends FOElement> foset : mSubsets.values()) {
			if (!foset.getType().isAssignableFrom(o.getClass()))
				continue; // this set won't contain an element of this type
			if (foset.contains(o))
				return true;
		}
		return false;
	}

	@Override
	public FOSet<? extends T> getOriginalSubset(String name) {
		return mSubsets.get(name);
	}

	@Override
	public FOSet<? extends T> complementExtendOut(FOSet<? extends T> relativeSet)
	{
		// We do a simple implementation enough here, mainly enough to take a complement of a subset of a union universet set with itself
		// ie. to get the other half of the set.
		// We can/should expand this in the future to support more complicated set interactions to be able to support fancier modes of operation.
		if(!(relativeSet instanceof FOEnumerableUnionSetImpl))
			return null;
		
		FOEnumerableUnionSetImpl<T> enumerableRelativeSet = (FOEnumerableUnionSetImpl<T>) relativeSet;
		Iterable<FOEnumerableSet<? extends T>> subsetsComplement = FluentIterable.from(enumerableRelativeSet.getSetIterable())
				.filter(relSubset -> 
				{
					FOEnumerableSet<? extends T> ownSubset = mSubsets.get(relSubset.getName());
					
					if(ownSubset == null)
						return true; // Relative set has it, but own set doesn't, so it's part of the complement.
					
					// This can happen at the moment as name uniqueness is wanted but not enforced.
					// If/when I find a good way to do it, it would be good to do this. Yet it's unexpected/unwanted.
					if(!ownSubset.equals(relSubset))
						throw new FORuntimeException("Unexpected set name clash - two unequal sets with the same name: " + ownSubset.getName());

					return false; // present in both own set and relative set - so not part of the complement.
				})
				.transform(relSubset -> (FOEnumerableSet<? extends T>) relSubset); // this is just for optics in compile time - not sure if this costs too many cycles.
		
		// If there's a single set that we have, no need for a union set, just return it.
		Iterator<FOEnumerableSet<? extends T>> it = subsetsComplement.iterator();
		if(it.hasNext())
		{
			FOEnumerableSet<? extends T> first = it.next();
			if(!it.hasNext())
				return first;
		}
		else // empty set
			return new FOSetUtils.EmptySet<>(relativeSet.getType());
					
		// Need to make sure the complement union set is type-consistent as well.
		Map<String, FOEnumerableSet<? extends T>> complementSubsets;
		try
		{
			complementSubsets = validateAndCreateNamedSubset(subsetsComplement);
		}
		catch (FOConstructionException e)
		{
			//TODO: This is not a deal breaker, only unsupported functionality. But add a warning count and possibly a warning to runtime in here since
			// we may want to support this if it's an important gap.
			return null;
		}
		
		try
		{
			return new FOEnumerableUnionSetImpl<>(complementSubsets, mEltType, null);
		}
		catch (FOConstructionException e)
		{
			assert false; // this should never happen.
			e.printStackTrace(System.err);
			throw new FORuntimeException("Unexpected exception while complementing set.", e);
		}			
	}

	@Override
	public FOSet<T> complementIn(FOSet<T> relativeSet)
	{
		String rsName = relativeSet.getName();
		FOEnumerableSet<? extends T> inset = mSubsets.get(rsName);
		if(inset == null)
		{
			// The relative set itself isn't in the union set, but let's see if its type is something that would allow the union set to
			// possibly contain the same items.
			for(FOEnumerableSet<? extends T> subset : mSubsets.values())
				if(relativeSet.getType().isAssignableFrom(subset.getType()))
					return null; // we can't handle this in an efficient way.
			return this;
		}
		else
		{
			if(!inset.equals(relativeSet))
				throw new FORuntimeException("Set name collision: " + relativeSet.getName()); //TODO: There has to be a better way to deal with this - actually get rid of names, and use a set of sets only,
			// then we can declare names as mainly for decoration / nice to have.
			Map<String, FOEnumerableSet<? extends T>> namedSubsets = new LinkedHashMap<>();
			for(FOEnumerableSet<? extends T> subset : mSubsets.values())
			{
				if(subset.getName().equals(rsName))
					continue;
				namedSubsets.put(subset.getName(), subset);
			}
			try
			{
				return new FOEnumerableUnionSetImpl<>(namedSubsets, mEltType, null);
			} catch (FOConstructionException e)
			{
				assert false; // this should never happen
				e.printStackTrace(System.err);
				throw new FORuntimeException("Unexpected exception while complementing set.", e);
			}
		}
	}
	
	@Override
	public Class<T> getType() {
		return mEltType;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Iterable<FOSet<? extends T>> getSetIterable() {
		// We know Iterator doesn't change behaviour between FOSet vs. FO EnumerableSet,
		// so what's below is safe.
		return (Iterable) mSubsets.values();
	}
}
