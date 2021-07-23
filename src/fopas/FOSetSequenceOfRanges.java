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
	
	/**
	 * This exception class is created when a sequence that contains a single range is created. This may be
	 * not the intention if contiguous multiple ranges are given, in which case they're merged to a single
	 * range. The idea here is to enforce the simplest representation of data. We merge contiguous ranges,
	 * and we refuse to create a sequence of ranges with only one item in it. This is so, automatic data
	 * manipulation which relies on the class types/shapes works in the optimal manner.
	 */
	public static class FOInvalidSingleRangeSequence extends Exception
	{
		protected FOSetRangedNaturals mRange;
		FOInvalidSingleRangeSequence(FOSetRangedNaturals range)
		{
			mRange = range;
		}
		private static final long serialVersionUID = 1L;
		/**
		 * A sequence of ranges resulted in a single range, therefore shouldn't be represented as a range sequence. 
		 * @return That single contigous range is returned inside this exception for use by the caller.
		 */
		public FOSetRangedNaturals getRange() { return mRange; }
	}
	
	FOSetSequenceOfRanges(Iterable<FOSetRangedNaturals> ranges) throws FOInvalidSingleRangeSequence
	{
		this(null, ranges);
	}

	FOSetSequenceOfRanges(Iterable<FOSetRangedNaturals> ranges, boolean transformContiguous) throws FOInvalidSingleRangeSequence
	{
		this(null, ranges, false);
	}

	FOSetSequenceOfRanges(String name, Iterable<FOSetRangedNaturals> ranges) throws FOInvalidSingleRangeSequence
	{
		this(name, ranges, true);
	}

	FOSetSequenceOfRanges(String name, Iterable<FOSetRangedNaturals> ranges, boolean transformContiguous) throws FOInvalidSingleRangeSequence
	{
		mName = name;
		mRanges = new ArrayList<>();
		
		Integer prevRangeEndOrInfNext = null;
		for(FOSetRangedNaturals range : ranges)
		{
			Integer rangeStartOrInf = range.getStartOrInfinite(true).getInteger();
			if(prevRangeEndOrInfNext != null)
			{
				if(rangeStartOrInf <= prevRangeEndOrInfNext)
				{
					if(rangeStartOrInf.equals(prevRangeEndOrInfNext))
					{
						if(transformContiguous)
						{
							FOSetRangedNaturals lastRange = mRanges.get(mRanges.size() - 1);
							FOSetRangedNaturals trfRange = new FOSetRangedNaturals(lastRange.getStart().getInteger(), lastRange.getIncludeStart(), 
																					range.getEnd().getInteger(), range.getIncludeEnd()); 
							mRanges.set(mRanges.size() - 1, trfRange);
						}
						else
							throw new FORuntimeException("Contiguous sequence of ranges creation where not allowed.");
					}
					else
						throw new FORuntimeException("Incorrectly ordered or overlapping invalid range given during creation.");						
				}
				else
					mRanges.add(range);
			}
			else
				mRanges.add(range); // not ideal that this is repeated few lines above.
			
			prevRangeEndOrInfNext = range.getEndOrInfinite(true).getInteger();
			if(prevRangeEndOrInfNext != Integer.MAX_VALUE)
			{
				assert prevRangeEndOrInfNext != Integer.MIN_VALUE;
				prevRangeEndOrInfNext = prevRangeEndOrInfNext + 1;
			}
		}
		
		if(mRanges.size() < 2)
		{
			if(mRanges.size() == 1)
				throw new FOInvalidSingleRangeSequence(mRanges.get(0)); // don't allow a sequence range of size 1 to be created, throw exception instead. But preserve the newly created range.
			else
				throw new FORuntimeException("Trying to create an empty range.");
		}
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
	public FOSet<FOInteger> complementOut(FOSet<FOInteger> relativeSet)
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
				try
				{
					if(mName != null)
							return new FOSetSequenceOfRanges(relativeSet.getName() + " \\ " + mName, newRanges);
					else
						return new FOSetSequenceOfRanges(newRanges);
				}
				catch (FOInvalidSingleRangeSequence e)
				{
					return e.getRange(); // don't think there's every a valid use case for this
				}
			}
		}
		
		return null;
	}

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
			try
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
			catch (FOInvalidSingleRangeSequence e)
			{
				return e.getRange(); // don't think there's every a valid use case for this
			}
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
		{
			try 
			{
				return new FOSetSequenceOfRanges(unionised);
			}
			catch (FOInvalidSingleRangeSequence e)
			{
				return e.getRange();
			}
		}
	}
	
	@Override
	public Class<FOInteger> getType() { return FOInteger.class;}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mRanges == null) ? 0 : mRanges.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FOSetSequenceOfRanges other = (FOSetSequenceOfRanges) obj;
		if (mRanges == null) {
			if (other.mRanges != null)
				return false;
		} else if (!mRanges.equals(other.mRanges))
			return false;
		return true;
	}
}
