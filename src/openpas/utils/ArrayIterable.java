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

// I wrote this class before I realised I could use Arrays.asList for all cases where I used this,
// but it's used in a lot of places in the test code to rip out easily.
public class ArrayIterable<T> implements Iterable<T>
{
	protected T[] mArray;
	public ArrayIterable(T[] array)
	{
		mArray = array;
	}
	
	public static class ArrayIterator<T> implements Iterator<T>
	{
		protected T[] mArray;
		protected int mIndex = 0;
		public ArrayIterator(ArrayIterable<T> iterable)
		{
			mArray = iterable.mArray;
		}
		
		@Override
		public boolean hasNext() {
			return mIndex < mArray.length;
		}

		@Override
		public T next() {
			return mArray[mIndex++];
		}

		@Override
		public void remove() {
			mArray[mIndex] = null; // Can't really remove it.
		}
	}
	
	@Override
	public Iterator<T> iterator() {
		return new ArrayIterator<T>(this);
	}
}
