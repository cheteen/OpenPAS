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
	
	static class FOString implements FOElement
	{
		String mElt;
		FOString(String elt)
		{
			mElt = elt;
		}
		
		@Override
		public Type getType() {
			return Type.String;
		}
	}

	static class FOSymbol implements FOElement
	{
		String mElt;
		// We get the name of the symbol as a parameter, of course it's only conceptually different to String.
		FOSymbol(String elt)
		{
			mElt = elt;
		}

		@Override
		public Type getType() {
			return Type.Symbol;
		}
	}

	// This is no different to Integer in that it boxes an int.
	static class FOInt implements FOElement
	{
		int mElt;
		FOInt(int elt)
		{
			mElt = elt;
		}
		
		@Override
		public Type getType() {
			return Type.Int;
		}
	}
}
