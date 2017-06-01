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
