package fopas;

import fopas.basics.FOElement;
import fopas.basics.FOFunction;
import fopas.basics.FOStructure;

abstract public class FOFunctionByRecursionImpl implements FOFunction
{
	final protected String mName;
	final protected FOFunction mFun;
	
	FOFunctionByRecursionImpl(String name, FOFunction fun)
	{
		mName = name;
		mFun = fun;
	}

	@Override
	public String getName()
	{
		return mName;
	}
}
