package fopas;

import fopas.basics.FOVariable;

public class FOVariableImpl implements FOVariable
{
	protected String mName;
	FOVariableImpl(String name)
	{
		mName = name;
	}

	@Override
	public String getName()
	{
		return mName;
	}
}
