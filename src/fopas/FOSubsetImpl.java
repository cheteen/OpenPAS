package fopas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

import fopas.FORelationImpl.FORelationInSet;
import fopas.basics.FOElement;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;

// This is a key class where runtime optimisation magic happens.
class FOSubsetImpl<T extends FOElement> implements FOSet<T>
{
	final FOSet<T> mSet;
	final Set<FORelation<T>> mSubsetRelations;
	Iterable<T> mOptimisedRunner;
	FOSet<T> mOptimisedSubset;
	
	FOSubsetImpl(FOSet<T> setToSubset, FORelation<T> relation)
	{
		this(setToSubset, new LinkedHashSet<>(Arrays.asList(relation)));
	}

	FOSubsetImpl(FOSet<T> setToSubset, Set<FORelation<T>> subsetRelations)
	{
		mSet = setToSubset;
		mSubsetRelations = subsetRelations;
	}

	@Override
	public Iterator<T> iterator()
	{
		if(mOptimisedRunner != null)
			return mOptimisedRunner.iterator();

		optimiseRun();
		
		return mOptimisedRunner.iterator();
	}

	protected void optimiseRun()
	{
		// This is a simple greedy alg that picks the first most constraining known relation (and therefore subset alg) and runs with it.
		// Could benefit from a multi-path search that allows for the non-greedy first pick to be considered.
		// Without heuristics the above is likely to be too expensive computationally though.
		FORelation<T> bestRel = null;
		int bestSize = Integer.MAX_VALUE;
		for(FORelation<T> relation : mSubsetRelations)
		{
			int subsetSize = mSet.getSubsetSize(relation); 
			if(subsetSize < bestSize)
			{
				bestRel = relation;
				bestSize = subsetSize;
			}	
		}
		
		if(bestRel != null)
		{
			mOptimisedSubset = mSet.createSubset(bestRel);
			mOptimisedRunner = mOptimisedSubset;
		}
		else
		{ 
			// No real optimised runner, let's fall back to grinding through all the relations that we have.
			List<FORelation<T>> subRelations = new ArrayList<FORelation<T>>(mSubsetRelations.size() - 1);
			for(FORelation<T> relation : mSubsetRelations)
			{
				if(relation == bestRel)
					continue;
				
				subRelations.add(relation);
			}
			mOptimisedRunner = FluentIterable.from(mSet).filter(elt -> Iterables.all(subRelations, rel -> rel.satisfies(elt)));
		}
	}

	@Override
	public int size()
	{
		if(mOptimisedRunner == null)
			optimiseRun();
		
		// If we have a subset that knows its size, give it a chance to report a size.
		if(mOptimisedSubset != null)
			return mOptimisedSubset.size();
		
		return -1;
	}

	@Override
	public String getName()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(mSet.getName());
		for(FORelation<T> relation : mSubsetRelations)
		{
			sb.append(".");
			sb.append(relation.getName());
		}
		return sb.toString();
	}

	/**
	 * This shouldn't really be used - it's bad implementation that answers it.
	 */
	@Override
	public boolean contains(Object o)
	{
		for(T elt : this)
			if(elt.equals(o))
				return true;
		return false;
	}

	/**
	 * Generic implementation that'll return default fallback impl.
	 */
	@Override
	public FOSet<T> createSubset(FORelation<T> rel)
	{
		if(mSubsetRelations.contains(rel))
			return this;
		Set<FORelation<T>> newRels = new LinkedHashSet<>(mSubsetRelations); //use linked to preserve order of relations.
		newRels.add(rel);
		return new FOSubsetImpl<>(mSet, newRels);
	}

	@Override
	public int getSubsetSize(FORelation<T> rel)
	{
		// This generic impl can't tell about hypothetical sizes.
		return -1;
	}
}
