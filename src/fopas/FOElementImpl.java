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

import java.util.Comparator;

import fopas.basics.FOElement;

// Generic element - I don't think this is really ever useful for something, I may need to remove it.
abstract class FOElementImpl implements FOElement
{
	final Object mElt;
	FOElementImpl(Object elt)
	{
		mElt = elt;
	}
	
	@Override
	public Object getElement()
	{
		return mElt;
	}

	// Eclipse auto-generated
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mElt == null) ? 0 : mElt.hashCode());
		return result;
	}

	// Eclipse auto-generated
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FOElementImpl other = (FOElementImpl) obj;
		if (mElt == null) {
			if (other.mElt != null)
				return false;
		} else if (!mElt.equals(other.mElt))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "FOElementImpl [" + mElt + "]";
	}

	static class FOStringImpl extends FOElementImpl implements FOString
	{
		static class FOStringComparator implements Comparator<FOElement>
		{
			@Override
			public int compare(FOElement arg0, FOElement arg1)
			{
				return ((FOString) arg1).getString().compareTo(((FOString)arg0).getString());
			}	
		}
		static final Comparator<FOElement> DEFAULT_COMPARATOR = new FOStringComparator();

		FOStringImpl(String elt)
		{
			super(elt);
		}
		
		@Override
		public Type getType() {
			return Type.String;
		}

		@Override
		public String getString()
		{
			return (String) mElt;
		}

		@Override
		public Comparator<FOElement> getDefaultComparator()
		{
			return DEFAULT_COMPARATOR;
		}
	}

	static class FOSymbolImpl extends FOElementImpl implements FOSymbol
	{
		static class FOSymComparator implements Comparator<FOElement>
		{
			@Override
			public int compare(FOElement arg0, FOElement arg1)
			{
				return ((FOSymbol)arg1).getName().compareTo(((FOSymbol)arg0).getName());
			}	
		}
		static final Comparator<FOElement> DEFAULT_COMPARATOR = new FOSymComparator();
		
		// We get the name of the symbol as a parameter, of course it's only conceptually different to String.
		FOSymbolImpl(String elt)
		{
			super(elt);
		}

		@Override
		public Type getType()
		{
			return Type.Symbol;
		}

		@Override
		public String getName()
		{
			return (String) mElt;
		}

		@Override
		public Comparator<FOElement> getDefaultComparator()
		{
			return DEFAULT_COMPARATOR;
		}
	}

	// This is no different to Integer in that it boxes an int.
	static class FOIntImpl extends FOElementImpl implements FOInteger
	{	
		static class FOIntComparator implements Comparator<FOElement>
		{
			@Override
			public int compare(FOElement arg0, FOElement arg1)
			{
				int intArg0 = ((FOInteger) arg0).getInteger();
				int intArg1 = ((FOInteger) arg1).getInteger();
				
				if(intArg1 == Integer.MAX_VALUE)
				{
					if(intArg0 < 0)
						return Integer.MAX_VALUE;
				}
				else if(intArg1 == Integer.MIN_VALUE)
				{
					if(intArg0 > 0)
						return Integer.MIN_VALUE;
				}
				
				return intArg1 - intArg0;
			}	
		}
		static final Comparator<FOElement> DEFAULT_COMPARATOR = new FOIntComparator();
		
		FOIntImpl(int elt)
		{
			super(elt);
		}
		
		@Override
		public Type getType() {
			return Type.Integer;
		}

		@Override
		public int getInteger() 
		{
			return (Integer) mElt;
		}

		@Override
		public int compareTo(FOInteger o)
		{
			return o.getInteger() - (Integer) mElt;
		}

		@Override
		public Comparator<FOElement> getDefaultComparator() { return DEFAULT_COMPARATOR; }
	}	
}

