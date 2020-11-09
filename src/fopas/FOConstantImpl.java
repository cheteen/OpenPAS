package fopas;

import fopas.basics.FOConstant;

class FOConstantImpl implements FOConstant
{
	final protected String mName;
	
	FOConstantImpl(String name)
	{
		mName = name;
	}

	@Override
	public String getName()
	{
		return mName;
	}
}
