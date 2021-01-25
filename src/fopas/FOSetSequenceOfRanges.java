package fopas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Iterables;

import fopas.basics.FOElement;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOEnumerableSet;
import fopas.basics.FORelation;
import fopas.basics.FOSet;
import fopas.basics.FOTerm;

public class FOSetSequenceOfRanges implements FOEnumerableSet<FOInteger>
{
	final protected String mName;
	final protected List<FOSetRangedNaturals> mRanges;
	
	FOSetSequenceOfRanges(String name, Iterable<FOSetRangedNaturals> ranges)
	{
		mName = name;
		mRanges = new LinkedList<>();
		Iterables.addAll(mRanges, ranges);
	}
	
	@Override
	public int size()
	{
		int totalSize = 0;
		for(FOSetRangedNaturals range : mRanges)
			totalSize += range.size();
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
			FOEnumerableSet<FOInteger> relativeEnumSet = (FOEnumerableSet<FOInteger>) relativeSet;
			
			int rsFirst = relativeEnumSet.getFirstElement().getInteger();
			int rsLast = relativeEnumSet.getLastElement().getInteger();

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
				
				int rangeFirst = range.getFirstElement().getInteger();
				int rangeLast = range.getLastElement().getInteger();
				int nextRangeFirst = nextRange.getFirstElement().getInteger();
				
				if(!gotFirstPartial)
				{
					if(rsFirst < rangeFirst)
					{
						int firstComplementLast = Math.min(rangeFirst - 1, rsLast);				
						newRanges.add((FOSetRangedNaturals)
								relativeEnumSet.constrainToRange(relativeEnumSet.getFirstElement(), true, new FOElementImpl.FOIntImpl(firstComplementLast), true));
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
							relativeEnumSet.constrainToRange(new FOElementImpl.FOIntImpl(secondComplementFirst), true, relativeEnumSet.getLastElement(), true));
					gotSecondPartial = true;
					break;
				}
				
				newRanges.add(new FOSetRangedNaturals(rangeLast + 1, nextRangeFirst - 1));
			}
			
			if(!gotSecondPartial)
			{
				int rangesLast = getLastElement().getInteger();
				int secondComplementFirst = Math.max(rangesLast + 1, rsFirst);
				newRanges.add((FOSetRangedNaturals)
						relativeEnumSet.constrainToRange(new FOElementImpl.FOIntImpl(secondComplementFirst), true, relativeEnumSet.getLastElement(), true));
			}
			
			if(newRanges.size() == 1)
				return newRanges.get(0);
			else return new FOSetSequenceOfRanges(relativeSet.getName() + "\\" + getName(), newRanges);
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
	public FOEnumerableSet<FOInteger> constrainToRange(FOElement start, boolean includeStart, FOElement end, boolean includeEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getConstrainedSize(FORelation<FOInteger> relation, List<FOTerm> terms) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public FOInteger getFirstElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FOInteger getLastElement() {
		// TODO Auto-generated method stub
		return null;
	}
}
