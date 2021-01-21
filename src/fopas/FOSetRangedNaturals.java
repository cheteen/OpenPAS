package fopas;

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
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOElement.Type;
import openpas.utils.SimpleRange;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOTerm;

// Infinite set for \mathbb{N}.
public class FOSetRangedNaturals implements FOSet<FOInteger>
{
	protected int mRangeStart;
	protected int mRangeEnd;
	
	FOSetRangedNaturals(int rangeStart, int rangeEnd)
	{
		if(rangeStart < 0)
			throw new FORuntimeException("Invalid subset requested.");

		mRangeStart = rangeStart;
		mRangeEnd = rangeEnd;
	}
	
	FOSetRangedNaturals()
	{
		this(0, -1);
	}

	@Override
	public Iterator<FOInteger> iterator()
	{
		int rangeEnd;
		if(mRangeEnd < 0)
			rangeEnd = Integer.MAX_VALUE;
		else
			rangeEnd = mRangeEnd;
		
		return new FOIntRange(mRangeStart, rangeEnd);
	}
	
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
				throw new FORuntimeException("Integer overflow.");
			
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
		if(mRangeEnd < 0)
			return -1;
		return mRangeEnd - mRangeStart + 1;
	}

	@Override
	public String getName()
	{
		if(mRangeEnd < 0)
		{
			if(mRangeStart == 0)
				return "N";
			else
				return "N[" + mRangeStart + ",inf)";
		}
		else
			return "N[" + mRangeStart + "," + mRangeEnd + ")";
	}

	@Override
	public boolean contains(Object o)
	{
		if(o == null || !o.getClass().isInstance(FOInteger.class))
			throw new FORuntimeException("Unexpected object: " + o);
		
		int check = ((FOInteger)o).getInteger();
		return check >= mRangeStart && (mRangeEnd < 0 || check < mRangeEnd);
	}

	@Override
	public FOSet<FOInteger> constrain(FORelation<FOInteger> relation, List<FOTerm> terms)
	{
		if(relation.getClass() == FORelationCompare.class)
		{
			assert terms.size() == 2;
			
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getConstrainedSize(FORelation<FOInteger> relation, List<FOTerm> terms) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public FOSet<FOInteger> complement(FOSet<FOInteger> relativeSet) {
		// TODO Auto-generated method stub
		return null;
	}
}
