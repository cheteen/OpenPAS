package fopas;

import fopas.basics.FOVariable;

public class FOVariableImpl implements FOVariable
{
	final protected String mName;
	
	FOVariableImpl(String name)
	{
		//TODO: Validate name here.
		mName = name;
	}

	@Override
	public String getName()
	{
		return mName;
	}
}
