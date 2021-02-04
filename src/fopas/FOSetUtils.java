package fopas;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.FluentIterable;

import fopas.basics.FOElement;
import fopas.basics.FOEnumerableSet;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOTerm;

public class FOSetUtils
{
	static class EmptySet<T extends FOElement> implements FOEnumerableSet<T>
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
		public FOEnumerableSet<T> constrainToRange(FOElement start, FOElement end) {
			// TODO Auto-generated method stub
			return null;
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
		final FOSet<T> mRelativeSet;

		ComplementedSingleElementSet(String singleElementSetName, T singleElement, FOSet<T> relativeSet)
		{
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
		public FOEnumerableSet<T> constrainToRange(FOElement start, FOElement end) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getConstrainedSize(FORelation<T> relation, List<FOTerm> terms) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public FOSet<T> complement(FOSet<T> relativeSet)
		{
			if(mRelativeSet.equals(relativeSet))
				return relativeSet;
			throw new FORuntimeException("Unimplemented complement operation.");
		}
	}
	
	static class SingleElementSet<T extends FOElement> implements FOEnumerableSet<T>
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
		protected final String mName;
		SingleElementSet(String name, T element)
		{
			mElement = element;
			mName = name;
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
			return mName;
		}

		@Override
		public boolean contains(Object o)
		{
			return mElement.equals(o);
		}

		@Override
		public FOSet<T> complement(FOSet<T> relativeSet)
		{
			return new ComplementedSingleElementSet<T>(mName, mElement, relativeSet);
		}

		@Override
		public FOEnumerableSet<T> constrainToRange(FOElement start, FOElement end) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getConstrainedSize(FORelation<T> relation, List<FOTerm> terms) {
			// TODO Auto-generated method stub
			return 0;
		}
	}
}
