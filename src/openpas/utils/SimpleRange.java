// OpenPAS
//
// Copyright (c) 2017 Burak Cetin
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

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
