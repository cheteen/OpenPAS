package fopas;

import java.util.Arrays;

import fopas.FOElementImpl.FOIntImpl;
import fopas.basics.FOElement;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOFunction;
import fopas.basics.FORuntimeException;
import fopas.basics.FOStructure;

abstract class FOInternalIntFunctions implements FOFunction 
{
	static class FOInternalSumModulus extends FOInternalIntFunctions
	{
		protected final int mModulus;
		
		FOInternalSumModulus(int modulus)
		{
			mModulus = modulus;
		}
		
		@Override
		public FOElement eval(FOStructure structure, FOElement... args)
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
	}

}
