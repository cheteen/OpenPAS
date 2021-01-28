package fopas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Iterables;

import fopas.basics.FOElement;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOEnumerableSet;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOTerm;

public class FOSetSequenceOfRanges implements FOEnumerableSet<FOInteger>
{
	final protected String mName;
	final protected List<FOSetRangedNaturals> mRanges;
	
	FOSetSequenceOfRanges(String name, Iterable<FOSetRangedNaturals> ranges)
	{
		mName = name;
		mRanges = new ArrayList<>();
		Iterables.addAll(mRanges, ranges);
	}
	
	@Override
	public int size()
	{
		int totalSize = 0;
		for(FOSetRangedNaturals range : mRanges)
		{
			int subRangeSize = range.size();
			if(subRangeSize == -1)
				return -1;
			totalSize += range.size();
		}
		return totalSize;
	}

	@Override
	public String getName()
	{
		return mName;
	}

	@Override
	public boolean contains(Object o)
	{
		for(FOSetRangedNaturals range : mRanges)
			if(range.contains(o))
				return true;
		return false;
	}

	@Override
	public FOSet<FOInteger> complement(FOSet<FOInteger> relativeSet)
	{
		// It'd be easy to generalise this to FOEnumerableSet since we use the generic interface FOEnumerableSet to do all the constraining operations which are the key.
		if(relativeSet instanceof FOSetRangedNaturals)
		{
			// There's probably a far more elegant algorithm than the one here to do this,
			// but it seems to work, and it's not inefficient.
			FOEnumerableSet<FOInteger> relativeEnumSet = (FOEnumerableSet<FOInteger>) relativeSet;
			
			int rsFirst = relativeEnumSet.getStart().getInteger();
			int rsLast = relativeEnumSet.getEnd().getInteger();

			List<FOSetRangedNaturals> newRanges = new ArrayList<>();
			assert mRanges.size() > 1;
			FOSetRangedNaturals range;
			FOSetRangedNaturals nextRange;
			Iterator<FOSetRangedNaturals> itRange = mRanges.iterator();
			nextRange = itRange.next();
			boolean gotFirstPartial = false;
			boolean gotSecondPartial = false;
			while(itRange.hasNext())
			{
				range = nextRange;
				nextRange = itRange.next();
				
				int rangeFirst = range.getStart().getInteger();
				int rangeLast = range.getEnd().getInteger();
				int nextRangeFirst = nextRange.getStart().getInteger();
				
				if(!gotFirstPartial)
				{
					if(rsFirst < rangeFirst)
					{
						int firstComplementLast = Math.min(rangeFirst - 1, rsLast);				
						newRanges.add((FOSetRangedNaturals)
								relativeEnumSet.constrainToRange(relativeEnumSet.getStart(), new FOElementImpl.FOIntImpl(firstComplementLast)));
						gotFirstPartial = true;
						if(firstComplementLast == rsLast)
						{
							gotSecondPartial = true;
							break;
						}
					}
				}
				
				if(rsLast > rangeLast && rsLast < nextRangeFirst)
				{
					int secondComplementFirst = Math.max(rangeLast + 1, rsFirst);
					newRanges.add((FOSetRangedNaturals)
							relativeEnumSet.constrainToRange(new FOElementImpl.FOIntImpl(secondComplementFirst), relativeEnumSet.getEnd()));
					gotSecondPartial = true;
					break;
				}
				
				newRanges.add(new FOSetRangedNaturals(rangeLast + 1, nextRangeFirst - 1));
			}
			
			if(!gotSecondPartial)
			{
				int rangesLast = getEnd().getInteger();
				int secondComplementFirst = Math.max(rangesLast + 1, rsFirst);
				newRanges.add((FOSetRangedNaturals)
						relativeEnumSet.constrainToRange(new FOElementImpl.FOIntImpl(secondComplementFirst), relativeEnumSet.getEnd()));
			}
			
			if(newRanges.size() == 1)
				return newRanges.get(0);
			else
				return new FOSetSequenceOfRanges(relativeSet.getName() + " \\ " + getName(), newRanges);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<FOInteger> iterator()
	{
		return (Iterator<FOInteger>) Iterables.concat(mRanges).iterator();
	}

	@Override
	public FOEnumerableSet<FOInteger> constrainToRange(FOInteger first, FOInteger last)
	{
		int myFirstInt = getStart().getInteger();
		int myLastInt = getEnd().getInteger();
		int firstInt = first.getInteger();
		if(firstInt < myFirstInt)
			firstInt = myFirstInt;
		int lastInt = last.getInteger();
		if(lastInt > myLastInt)
			lastInt = myLastInt;

		if(firstInt == myFirstInt && lastInt == myLastInt)
			return this;
		
		List<FOSetRangedNaturals> newRanges = new ArrayList<>();
		boolean gotStart = false;
		boolean gotEnd = false;
		for(int ix = 0; ix < mRanges.size(); ix++)
		{
			FOSetRangedNaturals range = mRanges.get(ix);
			int rangeFirst = range.getStart().getInteger();
			int rangeLast = range.getEnd().getInteger();
			if(!gotStart)
			{
				if(firstInt >= rangeFirst)
				{
					int startRangeLast = Math.min(rangeLast, lastInt);
					if(startRangeLast <= rangeLast)
						gotEnd = true;
					newRanges.add(new FOSetRangedNaturals(first.getInteger(), startRangeLast));
					gotStart = true;
				}
			}
			if(!gotEnd)
			{
				if(lastInt >= rangeFirst && lastInt <= rangeLast)
				{
					newRanges.add(new FOSetRangedNaturals(rangeFirst, lastInt));
					gotEnd = true;
					break;
				}
			}
		}
		
		assert gotStart;
		assert gotEnd;

		if(newRanges.size() == 1)
			return newRanges.get(0);
		else
			return new FOSetSequenceOfRanges(String.format("%s [%d, %d]", mName, firstInt, lastInt), newRanges);
	}

	@Override
	public int getConstrainedSize(FORelation<FOInteger> relation, List<FOTerm> terms) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public FOInteger getStart()
	{
		assert mRanges.size() > 0;
		return mRanges.get(0).getStart();
	}

	@Override
	public FOInteger getEnd() 
	{
		assert mRanges.size() > 0;
		return mRanges.get(mRanges.size() - 1).getEnd();
	}

	@Override
	public boolean getIncludeStart()
	{
		assert mRanges.size() > 0;
		return mRanges.get(0).getIncludeStart();		
	}

	@Override
	public boolean getIncludeEnd()
	{
		assert mRanges.size() > 0;
		return mRanges.get(mRanges.size() - 1).getIncludeEnd();
	}
}
