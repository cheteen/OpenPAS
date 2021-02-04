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
import fopas.basics.FORange;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOElement.Type;
import openpas.utils.SimpleRange;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOTerm;

// Infinite set for \mathbb{N} amd \mathbb{Z}.
public class FOSetRangedNaturals implements FOEnumerableSet<FOInteger>, FORange<FOInteger>
{
	protected final int mRangeLeft;
	protected final boolean mIncStart;
	protected final int mRangeRight;
	protected final boolean mIncEnd;

	// This enforces a different constructor/parameters and checks when creating an infinite range.
	// This should help with treating these more carefully.
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
				mRangeLeft = rangeStart; // We break contract here in our internal representation since infinity can't be the start of a range, but we deal with internally.
			else
				mRangeLeft = rangeStart + 1;
		}
		else
			mRangeLeft = rangeStart;
		
		if(!incEnd)
		{
			if(rangeEnd == Integer.MAX_VALUE)
				mRangeRight = rangeEnd;
			else
				mRangeRight = rangeEnd - 1;
		}
		else
			mRangeRight = rangeEnd;
		
		if(mRangeLeft > mRangeRight)
			throw new FORuntimeException("Invalid range parameters.");
	}
	
	/**
	 * This constructor start and end are inclusive.
	 * @param rangeStart
	 * @param rangeEnd
	 */
	FOSetRangedNaturals(int rangeStart, int rangeEnd)
	{
		this(rangeStart, true, rangeEnd, true);
	}

	FOSetRangedNaturals()
	{
		this(0, true, Integer.MAX_VALUE, false); // we sacrifice MAX_VALUE and MIN_VALUE to mean infinity.
	}

	@Override
	public Iterator<FOInteger> iterator()
	{
		int rangeStart = mRangeLeft;
		int rangeEnd = mRangeRight;
		
		int dir = getDir();
		if(dir > 0)
		{
			if(mRangeRight == Integer.MAX_VALUE && mRangeLeft == Integer.MIN_VALUE)
				rangeStart = 0; // iterate the positive half for Z - no good other default behaviour I can see

			return new FOIntRange(rangeStart, rangeEnd, dir);
		}
		else
		{
			return new FOIntRange(rangeEnd, rangeStart, dir);
		}
	}

	protected int getDir()
	{
		int dir;
		if(mRangeRight < 0) // if this is all a negative range we iterate backwards.
			dir = -1;
		else if(mRangeLeft < 0 && mRangeRight == 0)
			dir = -1;
		else
			dir = 1;
		return dir;
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
		if(mRangeLeft == Integer.MIN_VALUE)
			return -1;
		if(mRangeRight == Integer.MAX_VALUE)
			return -1;
		return (mRangeRight - mRangeLeft + 1);
	}

	@Override
	public String getName()
	{
		if(mRangeLeft == 0 && mRangeRight == Integer.MAX_VALUE)
			return "N";
		
		String setName;
		StringBuilder sb = new StringBuilder();
		if(mRangeLeft < 0)
			setName = "Z";
		else
			setName = "N";

		if(mRangeLeft == Integer.MIN_VALUE)
		{
			if(mRangeRight == Integer.MAX_VALUE)
				return "Z";
			assert !mIncStart;
			sb.append("Z (-inf, ");
		}
		else
		{
			sb.append(setName);
			sb.append(' ');
			if(mIncStart)
			{
				sb.append("[");
				sb.append(mRangeLeft);
			}
			else
			{
				sb.append("(");
				sb.append(mRangeLeft - 1);
			}
			sb.append(", ");
		}

		if(mRangeRight == Integer.MAX_VALUE)
		{
			assert !mIncEnd;
			sb.append("inf)");
		}
		else
		{
			if(mIncEnd)
			{
				sb.append(mRangeRight);
				sb.append("]");
			}
			else
			{
				sb.append(mRangeRight + 1);
				sb.append(")");
			}
		}
		
		return sb.toString();
	}

	@Override
	public boolean contains(Object o)
	{
		if(o == null || !(o instanceof FOInteger))
			throw new FORuntimeException("Unexpected object: " + o);
		
		int check = ((FOInteger)o).getInteger();
		return check >= mRangeLeft && check <= mRangeRight;
	}
		
	@Override
	public FOSet<FOInteger> complement(FOSet<FOInteger> relativeSet)
	{
		// It'd be easy to generalise this to FOEnumerableSet since we use the generic interface FOEnumerableSet to do all the constraining operations which are the key.
		if(relativeSet instanceof FOSetRangedNaturals)
		{
			FOSetRangedNaturals relativeEnumSet = (FOSetRangedNaturals) relativeSet;
			FOEnumerableSet<FOInteger> fosetNat1 = null;
			FOEnumerableSet<FOInteger> fosetNat2 = null;
			
			int rsFirst = ((FOInteger) relativeEnumSet.getStart()).getInteger();
			int rsLast = ((FOInteger) relativeEnumSet.getEnd()).getInteger();
			if(rsFirst < mRangeLeft)
			{
				int firstComplementLast = Math.min(mRangeLeft -1, rsLast);				
				fosetNat1 = relativeEnumSet.constrainToRange(relativeEnumSet.getStart(), new FOElementImpl.FOIntImpl(firstComplementLast));
			}
			if(rsLast > mRangeRight)
			{
				int secondComplementFirst = Math.max(mRangeRight + 1, rsFirst);
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

		if(mRangeLeft != firstInt || mRangeRight != lastInt)
		{
			int newFirst = Math.max(firstInt, mRangeLeft);
			boolean incFirst = newFirst == Integer.MIN_VALUE ? false : true;
			int newLast = Math.min(lastInt, mRangeRight);
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
	public String toString()
	{
		return "FOSetRangedNaturals [" + getName() + "]";		
	}

	protected int getStartOrInfInternal(boolean includeStart)
	{
		int start;
		if(includeStart)
			start = mRangeLeft;
		else
		{
			if(mRangeLeft == Integer.MIN_VALUE)
				start = Integer.MIN_VALUE;
			else
				start = mRangeLeft - 1;
		}
		return start;
	}

	protected int getEndOrInfInternal(boolean includeEnd)
	{
		int end;
		if(includeEnd)
			end = mRangeRight;
		else
		{
			if(mRangeRight == Integer.MAX_VALUE)
				end = Integer.MAX_VALUE;
			else
				end = mRangeRight + 1;
		}
		return end;
	}
	
	@Override
	public FOInteger getStart()
	{
		return new FOElementImpl.FOIntImpl(getStartOrInfInternal(mIncStart));
	}

	@Override
	public FOInteger getEnd()
	{
		int end = getEndOrInfInternal(mIncEnd);
		return new FOElementImpl.FOIntImpl(end);
	}

	@Override
	public boolean getIncludeStart()
	{
		return mIncStart;
	}

	@Override
	public boolean getIncludeEnd()
	{
		return mIncEnd;
	}

	@Override
	public FOInteger getStartOrInfinite(boolean includeStart)
	{
		return new FOElementImpl.FOIntImpl(getStartOrInfInternal(includeStart));
	}

	@Override
	public FOInteger getEndOrInfinite(boolean includeEnd)
	{
		return new FOElementImpl.FOIntImpl(getEndOrInfInternal(includeEnd));
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getStartOrInfInternal(true);
		result = prime * result + getEndOrInfInternal(true);
		return result;
	}

	/**
	 * It's not obvious whether inclusion should determine equality. For example is this true?: Z [x1, x2] = Z [x1, x2 + 1)
	 * The decision here was to treat start and end inclusion as cosmetic properties unless they matter for results mathematically.
	 * Therefore we declare, say, [1, 9] and [1, 10) to be not only equivalent, but equal from a Java point of view since it's
	 * the mathematical reality we care about.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FOSetRangedNaturals other = (FOSetRangedNaturals) obj;
		if (getStartOrInfInternal(true) != other.getStartOrInfInternal(true))
			return false;
		if (getEndOrInfInternal(true) != other.getEndOrInfInternal(true))
			return false;
		return true;
	}
}
