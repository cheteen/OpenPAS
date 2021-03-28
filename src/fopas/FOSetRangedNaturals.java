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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;

import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOOrderedEnumerableSet;
import fopas.basics.FORange;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOElement.Type;
import openpas.utils.SimpleRange;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOTerm;

// Infinite set for \mathbb{N} amd \mathbb{Z}.
public class FOSetRangedNaturals implements FOOrderedEnumerableSet<FOInteger>, FORange<FOInteger>
{
	protected final int mRangeFirst;
	protected final boolean mIncStart;
	protected final int mRangeLast;
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
		int rangeStart = mRangeFirst;
		int rangeEnd = mRangeLast;
		
		int dir = getDir();
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

	protected int getDir()
	{
		int dir;
		if(mRangeLast < 0) // if this is all a negative range we iterate backwards.
			dir = -1;
		else if(mRangeFirst < 0 && mRangeLast == 0)
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
		if(mRangeFirst == Integer.MIN_VALUE)
			return Integer.MAX_VALUE;
		if(mRangeLast == Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return (mRangeLast - mRangeFirst + 1);
	}

	@Override
	public String getName()
	{
		if(mRangeFirst == 0 && mRangeLast == Integer.MAX_VALUE)
			return "N";
		
		String setName;
		StringBuilder sb = new StringBuilder();
		if(mRangeFirst < 0)
			setName = "Z";
		else
			setName = "N";

		if(mRangeFirst == Integer.MIN_VALUE)
		{
			if(mRangeLast == Integer.MAX_VALUE)
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
		if(o == null || !(o instanceof FOInteger))
			throw new FORuntimeException("Unexpected object: " + o);
		
		int check = ((FOInteger)o).getInteger();
		return check >= mRangeFirst && check <= mRangeLast;
	}
		
	@Override
	public FOSet<FOInteger> complement(FOSet<FOInteger> relativeSet)
	{
		// TODO: Consider moving this (and constrain below) to a common abstract implementation, and perhaps do a concrete implementation here for efficiency (avoiding boxing).
		// It'd be easy to generalise this to FOEnumerableSet since we use the generic interface FOEnumerableSet to do all the constraining operations which are the key.
		if(relativeSet instanceof FOSetRangedNaturals)
		{
			FOSetRangedNaturals relativeEnumSet = (FOSetRangedNaturals) relativeSet;
			FOOrderedEnumerableSet<FOInteger> fosetNat1 = null;
			FOOrderedEnumerableSet<FOInteger> fosetNat2 = null;
			
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
			{
				if(fosetNat2 == null)
					return new FOSetUtils.EmptySet<FOElement.FOInteger>(FOInteger.class	);
				return fosetNat2;
			}
			else if(fosetNat2 == null)
				return fosetNat1;
			else
				return new FOSetSequenceOfRanges(relativeSet.getName() + " \\ " + getName(),
						Arrays.asList((FOSetRangedNaturals) fosetNat1, (FOSetRangedNaturals) fosetNat2));
		}
		return null;
	}

	@Override
	public FOOrderedEnumerableSet<FOInteger> constrainToRange(FOInteger first, FOInteger last)
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
			//TODO: Return a single item set here if needed, also do I need a contrain that allows returning an empty set?
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
			start = mRangeFirst;
		else
		{
			if(mRangeFirst == Integer.MIN_VALUE)
				start = Integer.MIN_VALUE;
			else
				start = mRangeFirst - 1;
		}
		return start;
	}

	protected int getEndOrInfInternal(boolean includeEnd)
	{
		int end;
		if(includeEnd)
			end = mRangeLast;
		else
		{
			if(mRangeLast == Integer.MAX_VALUE)
				end = Integer.MAX_VALUE;
			else
				end = mRangeLast + 1;
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
		return new FOElementImpl.FOIntImpl(getEndOrInfInternal(mIncEnd));
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

	// TODO: Remove this to replace it with getFirstOrInf
	@Override
	public FOInteger getStartOrInfinite(boolean includeStart)
	{
		return new FOElementImpl.FOIntImpl(getStartOrInfInternal(includeStart));
	}

	// TODO: Remove this to replace it with getLastOrInf
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

	@Override
	public Comparator<FOElement> getOrder()
	{
		// Range always operates on the "natural" order of integers.
		return FOElementImpl.FOIntImpl.DEFAULT_COMPARATOR;
	}

	@Override
	public FOInteger getFirstOrInfinite()
	{
		return getStartOrInfinite(true);
	}

	@Override
	public FOInteger getLastOrInfinite()
	{
		return getEndOrInfinite(true);
	}

	@Override
	public FOInteger getNextOrNull(FOInteger element)
	{
		int eltInt = element.getInteger();
		if(eltInt > mRangeLast)
			return null;
		else if(eltInt == mRangeLast)
		{
			if(eltInt == Integer.MAX_VALUE)
				return element; // MAX_VALUE
			else
				return null;
		}
		else if(eltInt == mRangeFirst && eltInt == Integer.MIN_VALUE)
			return element; // MIN_VALUE
		else if(eltInt < mRangeFirst)
			return getFirstOrInfinite();

		return new FOElementImpl.FOIntImpl(eltInt + 1);
	}

	@Override
	public FOInteger getPreviousOrNull(FOInteger element)
	{
		int eltInt = element.getInteger();
		if(eltInt < mRangeFirst)
			return null;
		else if(eltInt == mRangeFirst)
		{
			if(eltInt == Integer.MIN_VALUE)
				return element; // MIN_VALUE
			else
				return null;
		}
		else if(eltInt == mRangeLast && eltInt == Integer.MAX_VALUE)
			return element; // MAX_VALUE
		else if (eltInt > mRangeLast)
			return getLastOrInfinite();
		
		return new FOElementImpl.FOIntImpl(eltInt - 1);
	}

	@Override
	public Class<FOInteger> getType() { return FOInteger.class;}
}
