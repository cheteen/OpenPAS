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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.FluentIterable;
import com.sun.org.apache.bcel.internal.util.Class2HTML;

import fopas.FOElementImpl.FOIntImpl.FOIntComparator;
import fopas.basics.FOEnumerableSet;
import fopas.basics.FOElement;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOOrderedEnumerableSet;
import fopas.basics.FOFiniteSet;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOTerm;

public class FOSetUtils
{
	static class EmptySet<T extends FOElement> implements FOOrderedEnumerableSet<T>
	{
		protected final Class<T> mSetType;
		EmptySet(Class<T> setType)
		{
			mSetType = setType;
		}

		@Override
		public Iterator<T> iterator()
		{
			return Collections.emptyIterator();
		}

		@Override
		public int size()
		{
			return 0;
		}

		@Override
		public String getName()
		{
			return "(Empty)";
		}

		@Override
		public boolean contains(Object o)
		{
			return false;
		}

		@Override
		public FOOrderedEnumerableSet<T> constrainToRange(FOElement start, FOElement end)
		{
			return this;
		}

		@Override
		public int getConstrainedSize(FORelation<T> relation, List<FOTerm> terms) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public FOSet<T> complementOut(FOSet<T> relativeSet)
		{
			return relativeSet;
		}

		@Override
		public FOSet<T> complementIn(FOSet<T> relativeSet)
		{
			return this;
		}

		@Override
		public Comparator<FOElement> getOrder()
		{
			return new Comparator<FOElement>() {
				@Override
				public int compare(FOElement arg0, FOElement arg1)
				{
					return 0;
				}
			};
		}

		@Override
		public T getFirstOrInfinite()
		{
			throw new FORuntimeException("Empty set has no first element.");
		}

		@Override
		public T getLastOrInfinite()
		{
			throw new FORuntimeException("Empty set has no last element.");
		}

		@Override
		public T getNextOrNull(T element)
		{
			throw new FORuntimeException("Empty set has no next element.");
		}

		@Override
		public T getPreviousOrNull(T element)
		{
			throw new FORuntimeException("Empty set has no previous element.");
		}

		@Override
		public Class<T> getType() { return mSetType; }
	}

	static class ComplementedSingleElementSet<T extends FOElement> implements FOEnumerableSet<T>
	{
		protected static class ComplementedSingleElementSetIterator<T> implements Iterator<T>
		{
			protected final T mElement;
			protected final Iterator<T> mIt;
			protected T mNext;
			
			ComplementedSingleElementSetIterator(T element, Iterator<T> it)
			{
				mElement = element;
				mIt = it;
				getNextInternal();
			}
			
			@Override
			public boolean hasNext()
			{
				return mNext != null;
			}

			@Override
			public T next()
			{
				if(mNext == null)
				{
					mIt.next(); // throws an exception
					return null; // should never execute
				}
				else
				{
					T next = mNext;
					getNextInternal();

					return next;
				}
			}

			private void getNextInternal()
			{
				if(mIt.hasNext())
				{
					mNext = mIt.next();
					
					// Skip my one element.
					if(mNext.equals(mElement))
					{
						if(mIt.hasNext())
							mNext = mIt.next();
						else
							mNext = null;
					}
				}
				else
					mNext = null;
			}
		}
		
		final protected T mElement;
		final protected String mName;
		final FOEnumerableSet<T> mRelativeSet;

		ComplementedSingleElementSet(String singleElementSetName, T singleElement, FOEnumerableSet<T> relativeSet)
		{
			assert relativeSet.contains(singleElement); // no point in using this set otherwise
			mElement = singleElement;
			mName = String.format("%s\\(%s)", relativeSet.getName(), singleElementSetName); // TODO: Remove this.
			mRelativeSet = relativeSet; // TODO: This is not needed, remove it when there's time.
		}

		@Override
		public Iterator<T> iterator()
		{
			return new ComplementedSingleElementSetIterator<T>(mElement, mRelativeSet.iterator());
		}

		@Override
		public int size()
		{
			int uniSize = mRelativeSet.size(); 
			if(uniSize == -1)
				return -1;
			return uniSize - 1;
		}

		@Override
		public String getName()
		{
			return mName;
		}

		@Override
		public boolean contains(Object o)
		{
			if(mRelativeSet.contains(o) && !mElement.equals(o))
				return true;
			return false;
		}

		@Override
		public FOSet<T> complementOut(FOSet<T> relativeSet)
		{
			// This is the only efficient operation we can do in this direction.
			if(mRelativeSet.equals(relativeSet))
				return new SingleElementSet<T>(mElement, getType());
			return null;
		}
		
		@Override
		public FOSet<T> complementIn(FOSet<T> relativeSet)
		{
			FOSet<T> relComp = relativeSet.complement(mRelativeSet);
			if(relComp != null)
			{
				// relComp has to be a subset of mRelativeSet - it may or may not contain mElement.
				// This may be a more specialised set than mRelativeSet
				if(!relComp.contains(mElement))
					return relComp;
				else
				{
					// Since relComp is a subset of mRelative set (which is enumerable) it's impossible for this set to non-enumerable.
					assert relComp instanceof FOEnumerableSet;
					if(!(relComp instanceof FOEnumerableSet))
						return null; //should never happen.
					
					FOEnumerableSet<T> relEnumComp = (FOEnumerableSet<T>) relComp;

					return new ComplementedSingleElementSet<T>(getName(), mElement, relEnumComp);
				}
			}
			return null;
		}

		public Class<T> getType() { return mRelativeSet.getType();}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((mElement == null) ? 0 : mElement.hashCode());
			result = prime * result + ((mRelativeSet == null) ? 0 : mRelativeSet.hashCode());
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
			@SuppressWarnings("rawtypes") // type is reified ComplementedSingleElementSet, so can be ignored here.
			ComplementedSingleElementSet other = (ComplementedSingleElementSet) obj;
			if (mElement == null) {
				if (other.mElement != null)
					return false;
			} else if (!mElement.equals(other.mElement))
				return false;
			if (mRelativeSet == null) {
				if (other.mRelativeSet != null)
					return false;
			} else if (!mRelativeSet.equals(other.mRelativeSet))
				return false;
			return true;
		}
	}
	
	static class SingleElementSet<T extends FOElement> implements FOOrderedEnumerableSet<T>
	{	
		protected static class SingleElementIterator<T> implements Iterator<T>
		{
			protected final T mElement;
			protected boolean mHasNext;
			SingleElementIterator(T element)
			{
				mElement = element;
				mHasNext = true;
			}
			
			@Override
			public boolean hasNext()
			{
				return mHasNext;
			}

			@Override
			public T next()
			{
				mHasNext = false;
				return mElement;
			}
		}
		
		protected final T mElement;
		protected final Class<T> mType;
		protected final Comparator<FOElement> mComparator;

		SingleElementSet(T element, Class<T> type)
		{
			this(element, type, element.getDefaultComparator());
		}
		SingleElementSet(T element, Class<T> type, Comparator<FOElement> comparator)
		{
			mElement = element;
			mComparator = comparator;
			mType = type;
		}
		
		@Override
		public Iterator<T> iterator()
		{
			return new SingleElementIterator<T>(mElement);
		}

		@Override
		public int size()
		{
			return 1;
		}

		@Override
		public String getName()
		{
			return String.format("{%s}", mElement.getElement());
		}

		@Override
		public boolean contains(Object o)
		{
			return mElement.equals(o);
		}

		@Override
		public FOSet<T> complementOut(FOSet<T> relativeSet)
		{
			//TODO: Unit test this!
			if(!relativeSet.contains(mElement))
				return relativeSet;
			
			if(relativeSet instanceof FOEnumerableSet)
				// We need an enumerable set that has everything but this element:
				return new ComplementedSingleElementSet<T>(getName(), mElement, (FOEnumerableSet<T>) relativeSet);
			
			return null;				
		}

		@Override
		public FOSet<T> complementIn(FOSet<T> relativeSet)
		{
			if(!relativeSet.contains(mElement))
				return this;
			
			return new FOSetUtils.EmptySet<T>(getType());
		}

		@Override
		public FOOrderedEnumerableSet<T> constrainToRange(FOElement start, FOElement end)
		{
			if(mComparator.compare(mElement, start) <= 0
					&& mComparator.compare(mElement, end) >= 0)
				return this;
			else
				return new EmptySet<>(getType());
		}

		@Override
		public int getConstrainedSize(FORelation<T> relation, List<FOTerm> terms) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Comparator<FOElement> getOrder()
		{
			return mComparator;
		}

		@Override
		public T getFirstOrInfinite() 
		{
			return mElement;
		}

		@Override
		public T getLastOrInfinite()
		{
			return mElement;
		}
		@Override
		public T getNextOrNull(T element)
		{
			if(mElement.equals(element))
				return null;
			throw new FORuntimeException("Can't get next of element not in the set.");
		}
		@Override
		public T getPreviousOrNull(T element)
		{
			if(mElement.equals(element))
				return null;
			throw new FORuntimeException("Can't get previous of element not in the set.");
		}
		
		@Override
		public Class<T> getType() { return mType; }

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((mComparator == null) ? 0 : mComparator.hashCode());
			result = prime * result + ((mElement == null) ? 0 : mElement.hashCode());
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
			@SuppressWarnings("rawtypes")
			SingleElementSet other = (SingleElementSet) obj;
			if (mComparator == null) {
				if (other.mComparator != null)
					return false;
			} else if (!mComparator.equals(other.mComparator))
				return false;
			if (mElement == null) {
				if (other.mElement != null)
					return false;
			} else if (!mElement.equals(other.mElement))
				return false;
			return true;
		}
	}
}
