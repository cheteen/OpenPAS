package fopas;

import fopas.basics.FOElement;

// Generic element - I don't think this is really ever useful for something, I may need to remove it.
abstract class ElementImpl implements FOElement
{
	Object mElt;
	ElementImpl(Object elt)
	{
		mElt = elt;
	}
	
	static class FOStringImpl implements FOString
	{
		String mElt;
		FOStringImpl(String elt)
		{
			mElt = elt;
		}
		
		@Override
		public Type getType() {
			return Type.String;
		}
	}

	static class FOSymbolImpl implements FOSymbol
	{
		String mElt;
		// We get the name of the symbol as a parameter, of course it's only conceptually different to String.
		FOSymbolImpl(String elt)
		{
			mElt = elt;
		}

		@Override
		public Type getType() {
			return Type.Symbol;
		}
	}

	// This is no different to Integer in that it boxes an int.
	static class FOIntImpl implements FOInteger
	{
		int mElt;
		FOIntImpl(int elt)
		{
			mElt = elt;
		}
		
		@Override
		public Type getType() {
			return Type.Integer;
		}
	}
}
