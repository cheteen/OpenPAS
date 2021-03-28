//    Copyright (c) 2017, 2021 Burak Cetin
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

package openpas.utils;

import java.util.Iterator;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Range;

public class SimpleRange {

	public static FluentIterable<Integer> range(int rangeSize) {
		return FluentIterable.from(ContiguousSet.create(Range.closedOpen(0, rangeSize), DiscreteDomain.integers()));
	}
	public static FluentIterable<Integer> range(int rangeStart, int rangeEnd) {
		return FluentIterable.from(ContiguousSet.create(Range.closedOpen(rangeStart, rangeEnd), DiscreteDomain.integers()));
	}
	
	/**
	 * This is an aux class that's mean primarily for the FOPAS use case to process the set $\mathbb{N}$.
	 * @author serbet
	 *
	 */
	public static class SimplePosIntRange implements Iterable<Integer>
	{
		private int mStart;
		private int mEnd;
		
		/**
		 * Create a range for positive integers between 0 and +infinity.
		 * Will correct inconsistencies w/o warning.
		 * @param start : Lowest value is 0
		 * @param end : Can be -1 to specify infinite.
		 */
		public SimplePosIntRange(int start, int end)
		{
			mStart = start;
			if(mStart < 0)
				mStart = 0;
			
			mEnd = end;
			if(mEnd < mStart)
				mEnd = mStart;
		}

		/**
		 * Allows range to be made smaller by lowering the end.
		 * @param end
		 */
		public void setNoHigherUpper(int end)
		{
			if(end < mEnd || end == -1)
				mEnd = end;
		}
		
		/**
		 * Allows range to be made smaller by increasing the start.
		 * @param start
		 */
		public void setNoLowerDown(int start)
		{
			if(start > mStart)
				mStart = start;
		}
		
		public int getStart()
		{
			return mStart;
		}
		
		public int getEnd()
		{
			return mEnd;
		}
		
		public static class SPRIterator implements Iterator<Integer>
		{
			private int mIndex;
			private int mEnd;
			
			private SPRIterator(SimplePosIntRange range)
			{
				mIndex = range.getStart();
				mEnd = range.getEnd();
			}

			@Override
			public boolean hasNext()
			{
				return mIndex < mEnd;
			}

			@Override
			public Integer next()
			{
				return mIndex++;
			}
			
		}

		@Override
		public Iterator<Integer> iterator()
		{
			return new SPRIterator(this);
		}
	}

}
