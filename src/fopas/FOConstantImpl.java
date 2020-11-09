package fopas;

import fopas.basics.FOConstant;

class FOContantImpl implements FOConstant
{
	final protected String mName;
	
	FOContantImpl(String name)
	{
		mName = name;
	}

	@Override
	public String getName()
	{
		return mName;
	}
}
