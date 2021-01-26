package fopas;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;

import fopas.FORelationImpl.FORelationCompare;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOEnumerableSet;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOElement.Type;
import openpas.utils.SimpleRange;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOTerm;

// Infinite set for \mathbb{N}.
public class FOSetRangedNaturals implements FOEnumerableSet<FOInteger>
{
	protected int mRangeFirst;
	protected int mRangeLast;
	
	FOSetRangedNaturals(int rangeFirst, int rangeLast)
	{
		mRangeFirst = rangeFirst;
		mRangeLast = rangeLast;
	}
	
	FOSetRangedNaturals()
	{
		this(0, Integer.MAX_VALUE); // we sacrifice MAX_VALUE and MIN_VALUE to mean infinity.
	}

	@Override
	public Iterator<FOInteger> iterator()
	{
		if(mRangeFirst == Integer.MIN_VALUE)
			throw new FORuntimeException("Integer overflow - infinite iteration attempted.");

		int rangeEnd;
		if(mRangeLast == Integer.MAX_VALUE)
			rangeEnd = mRangeLast;
		else
			rangeEnd = mRangeLast + 1;
		
		return new FOIntRange(mRangeFirst, rangeEnd);
	}
	
	// TODO: Change this class to use first/last instead.
	protected static class FOIntRange implements Iterator<FOInteger>
	{
		int mRangeStart;
		int mRangeStop;
		int mIx;
		
		FOIntRange(int rangeStart, int rangeEnd)
		{
			mRangeStart = rangeStart;
			mRangeStop = rangeEnd - 1;
			mIx = 0;
		}

		@Override
		public boolean hasNext()
		{
			boolean rangeHasNext =  mIx < mRangeStop;
			
			if(rangeHasNext && mIx == Integer.MAX_VALUE)
				throw new FORuntimeException("Integer overflow - infinite iteration attempted.");
			
			return rangeHasNext;
		}

		@Override
		public FOInteger next()
		{
			FOInteger val = new FOElementImpl.FOIntImpl(mIx);
			mIx++;
			return val;
		}
	}

	/***
	 * Returns -1 to signify infinity.
	 */
	@Override
	public int size()
	{
		if(mRangeFirst == Integer.MIN_VALUE)
			return -1;
		if(mRangeLast == Integer.MAX_VALUE)
			return -1;
		return mRangeLast - mRangeFirst;
	}

	@Override
	public String getName()
	{
		if(mRangeFirst == 0 && mRangeLast == Integer.MAX_VALUE)
			return "N";
		
		StringBuilder sb = new StringBuilder();
		if(mRangeFirst < 0)
		{
			sb.append("Z ");
			if(mRangeFirst == Integer.MIN_VALUE)
				sb.append("(-inf, ");
			else
			{
				sb.append("(");
				sb.append(mRangeFirst);
				sb.append(", ");
			}
		}
		else
			sb.append("N [0, ");
		
		if(mRangeLast == Integer.MAX_VALUE)
			sb.append("inf)");
		else
		{
			sb.append(mRangeLast);
			sb.append("]");
		}
		return sb.toString();
	}

	@Override
	public boolean contains(Object o)
	{
		if(o == null || !o.getClass().isInstance(FOInteger.class))
			throw new FORuntimeException("Unexpected object: " + o);
		
		int check = ((FOInteger)o).getInteger();
		return check >= mRangeFirst && check <= mRangeLast;
	}
		
	@Override
	public FOSet<FOInteger> complement(FOSet<FOInteger> relativeSet)
	{
		// It'd be easy to generalise this to FOEnumerableSet since we use the generic interface FOEnumerableSet to do all the constraining operations which are the key.
		if(relativeSet instanceof FOSetRangedNaturals)
		{
			FOEnumerableSet<FOInteger> relativeEnumSet = (FOEnumerableSet<FOInteger>) relativeSet;
			FOEnumerableSet<FOInteger> fosetNat1 = null;
			FOEnumerableSet<FOInteger> fosetNat2 = null;
			
			int rsFirst = ((FOInteger) relativeEnumSet.getFirstElement()).getInteger();
			int rsLast = ((FOInteger) relativeEnumSet.getLastElement()).getInteger();
			if(rsFirst < mRangeFirst)
			{
				int firstComplementLast = Math.min(mRangeFirst -1, rsLast);				
				fosetNat1 = relativeEnumSet.constrainToRange(relativeEnumSet.getFirstElement(), new FOElementImpl.FOIntImpl(firstComplementLast));
			}
			if(rsLast > mRangeLast)
			{
				int secondComplementFirst = Math.max(mRangeLast + 1, rsFirst);
				fosetNat2 = relativeEnumSet.constrainToRange(new FOElementImpl.FOIntImpl(secondComplementFirst), relativeEnumSet.getLastElement());
			}
			
			if(fosetNat1 == null)
				return fosetNat2;
			else if(fosetNat2 == null)
				return fosetNat1;
			else
				return new FOSetSequenceOfRanges(relativeSet.getName() + "\\" + getName(),
						Arrays.asList((FOSetRangedNaturals) fosetNat1, (FOSetRangedNaturals) fosetNat2));
		}
		return null;
	}

	@Override
	public FOEnumerableSet<FOInteger> constrainToRange(FOInteger first, FOInteger last)
	{
		if(first.getType() != Type.Integer || last.getType() != Type.Integer)
			throw new FORuntimeException("Unexpected element type found.");
		
		int firstInt = ((FOInteger) first).getInteger();
		int lastInt = ((FOInteger) last).getInteger();
		// Is there any scenario the following won't work? Need to unit test this stuff.
		// Small sanity check at start.
		if(firstInt <= lastInt) // equality is probably not needed - need to unittest this etc.
		{
			if(mRangeFirst != firstInt && mRangeLast != lastInt)
			{
				int newFirst = Math.max(firstInt, mRangeFirst);
				int newLast = Math.max(lastInt, mRangeLast);
				return new FOSetRangedNaturals(newFirst, newLast);				
			}
		}
		return this;
	}

	@Override
	public int getConstrainedSize(FORelation<FOInteger> relation, List<FOTerm> terms) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public FOInteger getFirstElement()
	{
		return new FOElementImpl.FOIntImpl(mRangeFirst);
	}

	@Override
	public FOInteger getLastElement()
	{
		return new FOElementImpl.FOIntImpl(mRangeLast);
	}
}
