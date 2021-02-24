package fopas;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.FluentIterable;

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
		EmptySet()
		{
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
		public FOSet<T> complement(FOSet<T> relativeSet)
		{
			return relativeSet;
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
			mName = String.format("%s\\(%s)", relativeSet.getName(), singleElementSetName);
			mRelativeSet = relativeSet;
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
		public FOSet<T> complement(FOSet<T> relativeSet)
		{
			if(mRelativeSet.equals(relativeSet))
				return relativeSet;
			throw new FORuntimeException("Unimplemented complement operation.");
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
		protected final Comparator<FOElement> mComparator;
		SingleElementSet(T element)
		{
			this(element, element.getDefaultComparator());
		}
		SingleElementSet(T element, Comparator<FOElement> comparator)
		{
			mElement = element;
			mComparator = comparator;
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
		public FOSet<T> complement(FOSet<T> relativeSet)
		{
			if(!relativeSet.contains(mElement))
				return relativeSet;
			
			if(relativeSet instanceof FOEnumerableSet)
				// We need an enumerable set that has everything but this element:
				return new ComplementedSingleElementSet<T>(getName(), mElement, (FOEnumerableSet<T>) relativeSet);
			
			throw new FORuntimeException("Unsupported complement of single element set.");
			
			// This can be implemented for a non-enumerable range when we need it. 
		}

		@Override
		public FOOrderedEnumerableSet<T> constrainToRange(FOElement start, FOElement end)
		{
			if(mComparator.compare(mElement, start) <= 0
					&& mComparator.compare(mElement, end) >= 0)
				return this;
			else
				return new EmptySet<>();
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
	}
}
