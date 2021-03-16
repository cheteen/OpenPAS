package fopas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Iterables;

import fopas.FOElementImpl.FOIntImpl;
import fopas.FOElementImpl.FOIntImpl.FOIntComparator;
import fopas.basics.FOElement;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOOrderedEnumerableSet;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOTerm;

public class FOSetSequenceOfRanges implements FOOrderedEnumerableSet<FOInteger>
{
	final protected String mName;
	final protected List<FOSetRangedNaturals> mRanges;
	

	FOSetSequenceOfRanges(Iterable<FOSetRangedNaturals> ranges)
	{
		this(null, ranges);
	}

	FOSetSequenceOfRanges(Iterable<FOSetRangedNaturals> ranges, boolean allowContiguous)
	{
		this(null, ranges, false);
	}

	FOSetSequenceOfRanges(String name, Iterable<FOSetRangedNaturals> ranges)
	{
		this(name, ranges, true);
	}

	FOSetSequenceOfRanges(String name, Iterable<FOSetRangedNaturals> ranges, boolean allowContiguous)
	{
		mName = name;
		mRanges = new ArrayList<>();
		
		int rangesSize = 0;
		
		Integer prevRangeEndOrInfNext = null;
		for(FOSetRangedNaturals range : ranges)
		{
			rangesSize++;
			Integer rangeStartOrInf = range.getStartOrInfinite(true).getInteger();
			if(prevRangeEndOrInfNext != null)
			{
				if(rangeStartOrInf <= prevRangeEndOrInfNext)
				{
					if(rangeStartOrInf.equals(prevRangeEndOrInfNext))
					{
						if(!allowContiguous)
							throw new FORuntimeException("Contiguous sequence of ranges creation where not allowed.");
					}
					else
						throw new FORuntimeException("Incorrectly ordered or overlapping invalid range given during creation.");						
				}
			}
			mRanges.add(range);
			
			prevRangeEndOrInfNext = range.getEndOrInfinite(true).getInteger();
			if(prevRangeEndOrInfNext != Integer.MAX_VALUE)
			{
				assert prevRangeEndOrInfNext != Integer.MIN_VALUE;
				prevRangeEndOrInfNext = prevRangeEndOrInfNext + 1;
			}
		}
		
		if(rangesSize < 2)
			throw new FORuntimeException("Invalid sequence ranges creation - need at least 2 ranges.");
	}
	
	@Override
	public int size()
	{
		int totalSize = 0;
		for(FOSetRangedNaturals range : mRanges)
		{
			int subRangeSize = range.size();
			if(subRangeSize == Integer.MAX_VALUE)
				return Integer.MAX_VALUE;
			totalSize += range.size();
		}
		return totalSize;
	}

	@Override
	public String getName()
	{
		if(mName != null)
			return mName;
		StringBuilder sb = new StringBuilder();
		final int MAX_LEN = 100;
		String setName;
		if(mRanges.get(0).getStartOrInfinite(true).getInteger() < 0)
			setName = "Z";
		else
			setName = "N";
		sb.append(setName);
		sb.append(" ");

		for(FOSetRangedNaturals range : mRanges)
		{
			if(sb.length() > 2)
				sb.append(" U ");
			sb.append(range.getName().substring(2)); // not the most elegant. skips set name to get the range only.
			if(sb.length() + 3 > MAX_LEN)
			{
				sb.append("...");
				break;
			}
		}
		return sb.toString();
	}

	@Override
	public boolean contains(Object o)
	{
		// TODO: This should really be a binary search.
		for(FOSetRangedNaturals range : mRanges)
			if(range.contains(o))
				return true;
		return false;
	}

	@Override
	public FOSet<FOInteger> complement(FOSet<FOInteger> relativeSet)
	{
		// Complement of self is empty set.
		if(relativeSet == this)
			return new FOSetUtils.EmptySet<>(FOInteger.class);
		
		// It'd be easy to generalise this to FOEnumerableSet since we use the generic interface FOEnumerableSet to do all the constraining operations which are the key.
		if(relativeSet instanceof FOSetRangedNaturals)
		{
			// TODO: Would be best to introduce an FOSorted interface here so we get things like getLowest() getHighest()
			FOSetRangedNaturals relativeEnumSet = (FOSetRangedNaturals) relativeSet;
			
			int rsStartOrInf = relativeEnumSet.getStartOrInfinite(true).getInteger();
			int rsEndOrInf = relativeEnumSet.getEndOrInfinite(true).getInteger();
			int rsCursor = rsStartOrInf;
			List<FOSetRangedNaturals> newRanges = new ArrayList<>();

			// TODO: This should really be a binary search.
			for(FOSetRangedNaturals range : mRanges)
			{
				int rangeFirstOrInf = range.getStartOrInfinite(true).getInteger();
				int rangeLastOrInf = range.getEndOrInfinite(true).getInteger();

				if(rsCursor < rangeFirstOrInf)
				{
					int complementEnd = rangeFirstOrInf == Integer.MIN_VALUE ? Integer.MIN_VALUE : rangeFirstOrInf - 1;
					if(rsCursor <= complementEnd && !(complementEnd == Integer.MIN_VALUE && rsCursor == Integer.MIN_VALUE))
					{
						newRanges.add((FOSetRangedNaturals)
								relativeEnumSet.constrainToRange(new FOElementImpl.FOIntImpl(rsCursor), new FOElementImpl.FOIntImpl(complementEnd)));
					}
				}
				
				if(rsCursor <= rangeLastOrInf)
					rsCursor = rangeLastOrInf == Integer.MAX_VALUE ? Integer.MAX_VALUE : rangeLastOrInf + 1;
				
				if(rsCursor > rsEndOrInf)
					break;
			}
			
			if(rsCursor <= rsEndOrInf && !(rsCursor == Integer.MAX_VALUE && rsEndOrInf == Integer.MAX_VALUE))
			{
				newRanges.add((FOSetRangedNaturals)
						relativeEnumSet.constrainToRange(new FOElementImpl.FOIntImpl(rsCursor), relativeEnumSet.getEndOrInfinite(true)));//rsEndOrInf
			}
			
			if(newRanges.size() == 1)
				return newRanges.get(0);
			else if(newRanges.size() == 0) // empty set (possible if effectively complemented to self)
				return new FOSetUtils.EmptySet<FOElement.FOInteger>(FOInteger.class);
			else
			{
				if(mName != null)
					return new FOSetSequenceOfRanges(relativeSet.getName() + " \\ " + mName, newRanges);
				else
					return new FOSetSequenceOfRanges(newRanges);
			}
		}
		throw new FORuntimeException("Unsupported complement operation.");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<FOInteger> iterator()
	{
		return (Iterator<FOInteger>) Iterables.concat(mRanges).iterator();
	}

	@Override
	public FOOrderedEnumerableSet<FOInteger> constrainToRange(FOInteger first, FOInteger last)
	{
		// This is fairly wasteful and suboptimal but can live with it for now.
		
		int intFirstOrInf = first.getInteger();
		int intLastOrInf = last.getInteger();
		
		if(intFirstOrInf <= mRanges.get(0).getStartOrInfInternal(true) && intLastOrInf >= mRanges.get(mRanges.size() - 1).getEndOrInfInternal(true))
			return this;
		
		List<FOSetRangedNaturals> newRanges = new ArrayList<>();

		// TODO: This should really be a binary search.
		for(FOSetRangedNaturals range : mRanges)
		{
			int rangeFirstOrInf = range.getStartOrInfinite(true).getInteger();
			int rangeLastOrInf = range.getEndOrInfinite(true).getInteger();

			if(intFirstOrInf <= rangeFirstOrInf && intLastOrInf >= rangeLastOrInf)
				newRanges.add(range);
			else if(intFirstOrInf > rangeFirstOrInf || intLastOrInf < rangeLastOrInf)
			{
				int newRangeFirstOrInf = Integer.max(rangeFirstOrInf, intFirstOrInf);
				int newRangeLastOrInf = Integer.min(rangeLastOrInf, intLastOrInf);
				
				if(newRangeFirstOrInf <= newRangeLastOrInf)
					newRanges.add(new FOSetRangedNaturals(newRangeFirstOrInf, newRangeLastOrInf));
			}
		}

		if(newRanges.size() == 1)
			return newRanges.get(0);
		else if(newRanges.size() == 0) // empty set (possible if effectively complemented to self)
			return new FOSetUtils.EmptySet<FOElement.FOInteger>(FOInteger.class);
		else
		{
			if(mName != null)
			{
				String startB = intFirstOrInf == Integer.MIN_VALUE ? "(" : "[";
				String endB = intFirstOrInf == Integer.MAX_VALUE ? ")" : "]";
				return new FOSetSequenceOfRanges(String.format("%s %s%d, %d%s", mName, startB, intFirstOrInf, intLastOrInf, endB), newRanges);
			}
			else
				return new FOSetSequenceOfRanges(newRanges);
		}
	}

	@Override
	public int getConstrainedSize(FORelation<FOInteger> relation, List<FOTerm> terms) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString()
	{
		return "FOSetSequenceOfRanges [" + getName() + "]";
	}

	@Override
	public Comparator<FOElement> getOrder()
	{
		return FOElementImpl.FOIntImpl.DEFAULT_COMPARATOR;
	}

	@Override
	public FOInteger getFirstOrInfinite()
	{
		return mRanges.get(0).getFirstOrInfinite();
	}

	@Override
	public FOInteger getLastOrInfinite()
	{
		return mRanges.get(mRanges.size() - 1).getLastOrInfinite();
	}

	@Override
	public FOInteger getNextOrNull(FOInteger element)
	{
		// TODO: This should really be a binary search.
		Iterator<FOSetRangedNaturals> it = mRanges.iterator();
		FOSetRangedNaturals range = null;
		int eltInt = element.getInteger(); 
		while(it.hasNext())
		{
			range = it.next();
			if(eltInt < range.getFirstOrInfinite().getInteger())
				return range.getFirstOrInfinite();
			else if(range.contains(element))
			{
				FOInteger next = range.getNextOrNull(element);
				if(next == null && it.hasNext())
					return it.next().getFirstOrInfinite();
				return next;
			}
		}
		return range.getNextOrNull(element);
	}

	@Override
	public FOInteger getPreviousOrNull(FOInteger element)
	{
		// TODO: This should really be a binary search.
		Iterator<FOSetRangedNaturals> it = mRanges.iterator();
		FOSetRangedNaturals range = it.next();
		FOSetRangedNaturals prevRange = null;
		int eltInt = element.getInteger();
		
		if(eltInt <= range.getLastOrInfinite().getInteger())
			return range.getPreviousOrNull(element);
		while(it.hasNext())
		{
			prevRange = range;
			range = it.next();
			if(eltInt <= range.getLastOrInfinite().getInteger())
			{
				FOInteger prev = range.getPreviousOrNull(element);
				if(prev == null)
					return prevRange.getLastOrInfinite();
				return prev;
			}
		}
		return range.getPreviousOrNull(element);
	}
	
	public Iterable<FOSetRangedNaturals> getRanges()
	{
		return mRanges;
	}
	
	public static FOOrderedEnumerableSet<FOInteger> createUnion(List<FOSetRangedNaturals> ranges)
	{
		// Sort from small to large on the first element on the constituent ranges.
		Collections.sort(ranges ,
				new Comparator<FOSetRangedNaturals>() {
					@Override
					public int compare(FOSetRangedNaturals arg0, FOSetRangedNaturals arg1)
					{
						return FOIntImpl.DEFAULT_COMPARATOR.compare(
								arg1.getFirstOrInfinite(), arg0.getFirstOrInfinite());
					}
		});
		
		List<FOSetRangedNaturals> unionised = new ArrayList<>();
		unionised.add(ranges.get(0));
		for(int i = 1; i < ranges.size(); i++)
		{
			FOSetRangedNaturals current = unionised.get(unionised.size() - 1);
			FOSetRangedNaturals next = ranges.get(i);
			int currentFirst = current.getFirstOrInfinite().getInteger();
			int currentLast = current.getLastOrInfinite().getInteger();
			int nextFirst = next.getFirstOrInfinite().getInteger();
			int nextLast = next.getLastOrInfinite().getInteger();
			
			if(nextFirst <= currentLast)
			{
				if(nextLast > currentLast)
				{
					unionised.set(unionised.size() - 1,
						new FOSetRangedNaturals(
								currentFirst, currentFirst == Integer.MIN_VALUE ? false : true,
								nextLast, nextLast == Integer.MAX_VALUE ? false : true
								)
						);
				}
				// else ignore it since it's wholly contained in the existing rangef
			}
			else
				unionised.add(next);			
		}
		
		if(unionised.size() == 1)
			return unionised.get(0);
		else
			return new FOSetSequenceOfRanges(unionised);
	}
	
	@Override
	public Class<FOInteger> getType() { return FOInteger.class;}
}
