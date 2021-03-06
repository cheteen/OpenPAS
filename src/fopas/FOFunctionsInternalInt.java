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
			return 3005;
		}

		@Override
		public FOFunction inversePresentInfix()
		{
			return new FOInternalSumModulus(mModulus, !mPresentInfix);
		}
	}
	
	static class FOInternalSubtractModulus extends FOFunctionsInternalInt
	{
		protected final int mModulus;

		FOInternalSubtractModulus()
		{
			this(-1);
		}

		FOInternalSubtractModulus(int modulus)
		{
			this(modulus, true);
		}
		
		FOInternalSubtractModulus(int modulus, boolean presentInfix)
		{
			//TODO: Validate name here.
			super(presentInfix);
			mModulus = modulus;
		}
		
		@Override
		public FOElement eval(FOElement... args)
		{
			int result = ((FOInteger) args[0]).getInteger() - ((FOInteger) args[1]).getInteger();
			if(mModulus > 0)
			{
				if(result < 0)
					result += mModulus;
				result %= mModulus;
			}
			return new FOElementImpl.FOIntImpl(result);
		}

		@Override
		public String getName()
		{
			return "subtract";
		}

		@Override
		public int getCardinality()
		{
			return 2;
		}

		@Override
		public String getInfix()
		{
			return "-";
		}

		@Override
		public int getPrecedence()
		{
			// Technically this should have the same precedence as addition, and then it's from left to right.
			// But let's not complicate things for now, and give subtractions slightly lower precedence.
			return 3000;
		}

		@Override
		public FOFunction inversePresentInfix()
		{
			return new FOInternalSubtractModulus(mModulus, !mPresentInfix);
		}
	}	
}
