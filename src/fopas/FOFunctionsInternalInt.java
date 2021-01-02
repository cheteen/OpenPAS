package fopas;

import java.util.Arrays;

import fopas.FOElementImpl.FOIntImpl;
import fopas.basics.FOElement;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOFunction;
import fopas.basics.FORuntimeException;
import fopas.basics.FOStructure;

abstract class FOFunctionsInternalInt extends FOFunctionImpl
{
	protected final boolean mPresentInfix;
	FOFunctionsInternalInt(boolean presentInfix)
	{
		mPresentInfix = presentInfix;		
	}
	
	boolean presentInfix() { return mPresentInfix; }
	
	static class FOInternalSumModulus extends FOFunctionsInternalInt
	{
		protected final int mModulus;

		FOInternalSumModulus()
		{
			this(-1);
		}

		FOInternalSumModulus(int modulus)
		{
			this(modulus, true);
		}
		
		FOInternalSumModulus(int modulus, boolean presentInfix)
		{
			//TODO: Validate name here.
			super(presentInfix);
			mModulus = modulus;
		}
		
		@Override
		public FOElement eval(FOElement... args)
		{
			int sum = 0;
			for(FOElement arg : args)
			{
				sum += ((FOInteger) arg).getInteger();
			}
			if(mModulus > 0)
				sum %= mModulus;
			return new FOElementImpl.FOIntImpl(sum);
		}

		@Override
		public String getName()
		{
			return "sum";
		}

		@Override
		public int getCardinality()
		{
			return -1;
		}

		@Override
		public String getInfix()
		{
			return "+";
		}

		@Override
		public int getPrecedence()
		{
			return 3000;
		}

		@Override
		public FOFunction inversePresentInfix()
		{
			return new FOInternalSumModulus(mModulus, !mPresentInfix);
		}
	}

}
