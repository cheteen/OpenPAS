package fopas;

import fopas.basics.FOElement;

// Generic element - I don't think this is really ever useful for something, I may need to remove it.
abstract class FOElementImpl implements FOElement
{
	final Object mElt;
	FOElementImpl(Object elt)
	{
		mElt = elt;
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

	static class FOStringImpl extends FOElementImpl implements FOString
	{
		final String mStrElt; // this is realy a short-hand reference to the same object but without needing to cast it.
		FOStringImpl(String elt)
		{
			super(elt);
			mStrElt = elt;
		}
		
		@Override
		public Type getType() {
			return Type.String;
		}
	}

	static class FOSymbolImpl extends FOElementImpl implements FOSymbol
	{
		final String mSymElt;
		// We get the name of the symbol as a parameter, of course it's only conceptually different to String.
		FOSymbolImpl(String elt)
		{
			super(elt);
			mSymElt = elt;
		}

		@Override
		public Type getType() {
			return Type.Symbol;
		}
	}

	// This is no different to Integer in that it boxes an int.
	static class FOIntImpl extends FOElementImpl implements FOInteger
	{
		final Integer mIntElt;
		FOIntImpl(int elt)
		{
			super(elt);
			mIntElt = elt;
		}
		
		@Override
		public Type getType() {
			return Type.Integer;
		}

		@Override
		public int getInteger() 
		{
			return mIntElt;
		}
	}
}
