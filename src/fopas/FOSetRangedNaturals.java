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
	protected final int mRangeFirst;
	protected final boolean mIncStart;
	protected final int mRangeLast;
	protected final boolean mIncEnd;

	FOSetRangedNaturals(int rangeStart, boolean incStart, int rangeEnd, boolean incEnd)
	{
		if(rangeStart == Integer.MAX_VALUE || rangeEnd == Integer.MIN_VALUE)
			throw new FORuntimeException("Invalid range parameters.");
		if((incStart && rangeStart == Integer.MIN_VALUE) || (incEnd && rangeEnd == Integer.MAX_VALUE))
			throw new FORuntimeException("Range can't be inclusive of infinity.");
			
		mIncStart = incStart;
		mIncEnd = incEnd;

		if(!incStart)
		{
			if(rangeStart == Integer.MIN_VALUE)
				mRangeFirst = rangeStart; // We break contract here in our internal representation since infinity can't be the start of a range, but we deal with internally.
			else
				mRangeFirst = rangeStart + 1;
		}
		else
			mRangeFirst = rangeStart;
		
		if(!incEnd)
		{
			if(rangeEnd == Integer.MAX_VALUE)
				mRangeLast = rangeEnd;
			else
				mRangeLast = rangeEnd - 1;
		}
		else
			mRangeLast = rangeEnd;
		
		if(mRangeFirst > mRangeLast)
			throw new FORuntimeException("Invalid range parameters.");
	}
	
	FOSetRangedNaturals(int rangeFirst, int rangeLast)
	{
		this(rangeFirst, true, rangeLast, true);
	}

	FOSetRangedNaturals()
	{
		this(0, true, Integer.MAX_VALUE, false); // we sacrifice MAX_VALUE and MIN_VALUE to mean infinity.
	}

	@Override
	public Iterator<FOInteger> iterator()
	{
		int rangeStart = mRangeFirst;
		int rangeEnd = mRangeLast;
		
		int dir;
		if(rangeEnd < 0) // if this is all a negative range we iterate backwards.
			dir = -1;
		else if(rangeStart < 0 && rangeEnd == 0)
			dir = -1;
		else
			dir = 1;
		
		if(dir > 0)
		{
			if(mRangeLast == Integer.MAX_VALUE && mRangeFirst == Integer.MIN_VALUE)
				rangeStart = 0; // iterate the positive half for Z - no good other default behaviour I can see

			return new FOIntRange(rangeStart, rangeEnd, dir);
		}
		else
		{
			return new FOIntRange(rangeEnd, rangeStart, dir);
		}
	}
	
	// TODO: Change this class to use first/last instead.
	protected static class FOIntRange implements Iterator<FOInteger>
	{
		final int mRangeLast;
		final int mDir;
		int mIx;
		
		FOIntRange(int rangeFirst, int rangeLast, int dir)
		{
			mRangeLast = rangeLast;
			mIx = rangeFirst;
			mDir = dir;
		}

		@Override
		public boolean hasNext()
		{
			if(mIx == Integer.MAX_VALUE || mIx == Integer.MIN_VALUE)
				throw new FORuntimeException("Integer overflow - infinite iteration attempted.");

			if(mDir > 0)
				return mIx <= mRangeLast;
			else
				return mIx >= mRangeLast;
		}

		@Override
		public FOInteger next()
		{
			FOInteger val = new FOElementImpl.FOIntImpl(mIx);
			mIx += mDir;
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
		return (mRangeLast - mRangeFirst + 1);
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
			{
				if(mRangeLast == Integer.MAX_VALUE)
					return "Z";
				assert !mIncStart;
				sb.append("(-inf, ");
			}
			else
			{
				if(mIncStart)
				{
					sb.append("[");
					sb.append(mRangeFirst);
				}
				else
				{
					sb.append("(");
					sb.append(mRangeFirst - 1);
				}
				sb.append(", ");
			}
		}
		else
		{
			sb.append("N ");
			if(mIncStart)
			{
				sb.append("[");
				sb.append(mRangeFirst);
			}
			else
			{
				sb.append("(");
				sb.append(mRangeFirst - 1);
			}
			sb.append(", ");
		}
		
		if(mRangeLast == Integer.MAX_VALUE)
		{
			assert !mIncEnd;
			sb.append("inf)");
		}
		else
		{
			if(mIncEnd)
			{
				sb.append(mRangeLast);
				sb.append("]");
			}
			else
			{
				sb.append(mRangeLast + 1);
				sb.append(")");
			}
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
			
			int rsFirst = ((FOInteger) relativeEnumSet.getStart()).getInteger();
			int rsLast = ((FOInteger) relativeEnumSet.getEnd()).getInteger();
			if(rsFirst < mRangeFirst)
			{
				int firstComplementLast = Math.min(mRangeFirst -1, rsLast);				
				fosetNat1 = relativeEnumSet.constrainToRange(relativeEnumSet.getStart(), new FOElementImpl.FOIntImpl(firstComplementLast));
			}
			if(rsLast > mRangeLast)
			{
				int secondComplementFirst = Math.max(mRangeLast + 1, rsFirst);
				fosetNat2 = relativeEnumSet.constrainToRange(new FOElementImpl.FOIntImpl(secondComplementFirst), relativeEnumSet.getEnd());
			}
			
			if(fosetNat1 == null)
				return fosetNat2;
			else if(fosetNat2 == null)
				return fosetNat1;
			else
				return new FOSetSequenceOfRanges(relativeSet.getName() + " \\ " + getName(),
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

		if(mRangeFirst != firstInt || mRangeLast != lastInt)
		{
			int newFirst = Math.max(firstInt, mRangeFirst);
			boolean incFirst = newFirst == Integer.MIN_VALUE ? false : true;
			int newLast = Math.min(lastInt, mRangeLast);
			boolean incLast = newLast == Integer.MAX_VALUE ? false : true;
			return new FOSetRangedNaturals(newFirst, incFirst, newLast, incLast);
		}
		else
			return this;
	}

	@Override
	public int getConstrainedSize(FORelation<FOInteger> relation, List<FOTerm> terms) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public FOInteger getStart()
	{
		// TODO: This should throw if returning infinite - or perhaps the finite set should subclass the enumarable one and only then offer getlast/first
		// Or perhaps I should leave it alone - not sure what's the best here.
		// Right -> this shoudl be getRangeEnd(), but internally we should do first/last!
		return new FOElementImpl.FOIntImpl(mRangeFirst);
	}

	@Override
	public FOInteger getEnd()
	{
		// TODO: This should throw if returning infinite.
		return new FOElementImpl.FOIntImpl(mRangeLast);
	}
	
	@Override
	public String toString()
	{
		return "FOSetRangedNaturals " + getName();		
	}
}
