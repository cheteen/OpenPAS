package fopas;

import fopas.basics.FOElement;
import fopas.basics.FOFunction;
import fopas.basics.FOStructure;

abstract public class FOFunctionByRecursionImpl implements FOFunction
{
	final protected String mName;
	
	FOFunctionByRecursionImpl(String name)
	{
		mName = name;
	}

	@Override
	public String getName()
	{
		return mName;
	}
}
