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

import java.lang.ref.WeakReference;
import java.util.Iterator;

public class WeakIterator<T> implements Iterator<T>
{
	Iterator<WeakReference<T>> mIt;
	boolean mHasNext;
	T mNext;
	
	public WeakIterator(Iterable<WeakReference<T>> it)
	{
		mIt = it.iterator();
		peekNext();
	}
	
	public boolean peekNext()
	{
		mNext = null;
		mHasNext = false;
		while(mIt.hasNext())
		{
			mNext = mIt.next().get();
			
			if(mNext == null)
				mIt.remove();
			else
				break;
		}
		return mHasNext;
	}
	
	@Override
	public boolean hasNext() {
		return mHasNext || peekNext();
	}

	@Override
	public T next() {
		T next = mNext;
		peekNext();
		return next;
	}

	@Override
	public void remove() {
		mIt.remove();
	}			
}